package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.FileDownloadDto;
import com.spshpau.projectservice.dto.ProjectFileResponseDto;
import com.spshpau.projectservice.services.filestorage.S3FileStorageService;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.ProjectFile;
import com.spshpau.projectservice.model.SimpleUser;
import com.spshpau.projectservice.repositories.ProjectFileRepository;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.services.ProjectFileService;
import com.spshpau.projectservice.services.ProjectService;
import com.spshpau.projectservice.services.SimpleUserService;
import com.spshpau.projectservice.services.exceptions.FileNotFoundException;
import com.spshpau.projectservice.services.exceptions.ProjectNotFoundException;
import com.spshpau.projectservice.services.exceptions.UnauthorizedOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectFileServiceImpl implements ProjectFileService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final SimpleUserService simpleUserService;
    private final ProjectService projectService;
    private final S3FileStorageService s3FileStorageService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "audio/mpeg", // mp3
            "audio/wav", "audio/x-wav", // wav
            "application/pdf" // pdf
    );
    private static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024;

    @Override
    @Transactional
    public ProjectFileResponseDto uploadProjectFile(UUID projectId, UUID uploaderUserId, String uploaderUsername,
                                                    MultipartFile file, String description) throws IOException {
        projectService.verifyUserIsProjectMember(projectId, uploaderUserId);

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types are MP3, WAV, PDF. Received: " + file.getContentType());
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds the limit of " + (MAX_FILE_SIZE_BYTES / (1024 * 1024)) + "MB.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));
        SimpleUser uploader = simpleUserService.findUserById(uploaderUserId);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String s3Key = "projects/" + projectId + "/files/" + originalFilename;

        String s3VersionId = s3FileStorageService.uploadFile(s3Key, file);
        if (s3VersionId == null) {
            log.warn("S3 Version ID was null for file {} in bucket {}. Check bucket versioning.", s3Key, bucketName);
            throw new IOException("Failed to get S3 version ID for uploaded file.");
        }


        ProjectFile projectFile = new ProjectFile();
        projectFile.setProject(project);
        projectFile.setUploadedBy(uploader);
        projectFile.setOriginalFilename(originalFilename);
        projectFile.setS3ObjectKey(s3Key);
        projectFile.setS3VersionId(s3VersionId);
        projectFile.setContentType(file.getContentType());
        projectFile.setFileSize(file.getSize());
        projectFile.setDescription(description);

        ProjectFile savedFile = projectFileRepository.save(projectFile);
        log.info("Saved ProjectFile metadata for {} (ID: {}), S3 Key: {}, S3 Version: {}",
                originalFilename, savedFile.getId(), s3Key, s3VersionId);

        return ProjectFileResponseDto.fromEntity(savedFile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectFileResponseDto> getProjectFiles(UUID projectId, UUID currentUserId) {
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        List<ProjectFile> latestFiles = projectFileRepository.findLatestVersionOfEachFileByProjectId(projectId);
        return latestFiles.stream()
                .map(ProjectFileResponseDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public ProjectFileResponseDto getProjectFileMetadata(UUID projectId, UUID fileId, UUID currentUserId) {
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectFile projectFile = projectFileRepository.findByIdAndProjectId(fileId, projectId)
                .orElseThrow(() -> new FileNotFoundException("File metadata not found with ID: " + fileId + " for project " + projectId));
        return ProjectFileResponseDto.fromEntity(projectFile);
    }


    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto generateDownloadUrl(UUID projectId, UUID fileId, UUID currentUserId) {
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectFile projectFile = projectFileRepository.findByIdAndProjectId(fileId, projectId)
                .orElseThrow(() -> new FileNotFoundException("File metadata not found with ID: " + fileId + " for project " + projectId));

        if (projectFile.getS3ObjectKey() == null || projectFile.getS3VersionId() == null) {
            log.error("File metadata for ID {} is missing S3 key or version ID.", fileId);
            throw new IllegalStateException("File cannot be downloaded due to missing S3 information.");
        }

        URL presignedUrl = s3FileStorageService.generatePresignedDownloadUrl(
                projectFile.getS3ObjectKey(),
                projectFile.getS3VersionId()
        );
        return new FileDownloadDto(presignedUrl.toString(), projectFile.getOriginalFilename());
    }

    @Override
    @Transactional
    public void deleteProjectFile(UUID projectId, UUID fileId, UUID currentUserId) {
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectFile projectFile = projectFileRepository.findByIdAndProjectId(fileId, projectId)
                .orElseThrow(() -> new FileNotFoundException("File metadata not found with ID: " + fileId + " for project " + projectId));

        try {
            s3FileStorageService.deleteFileVersion(projectFile.getS3ObjectKey(), projectFile.getS3VersionId());
        } catch (Exception e) {
            log.error("Failed to delete file version from S3: Key {}, VersionId {}. Error: {}",
                    projectFile.getS3ObjectKey(), projectFile.getS3VersionId(), e.getMessage(), e);
            throw new IllegalStateException("Deletion failed!");
        }

        projectFileRepository.delete(projectFile);
        log.info("Deleted ProjectFile metadata for ID: {}, Original Filename: {}", fileId, projectFile.getOriginalFilename());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectFileResponseDto> getAllVersionsOfFile(UUID projectId, String originalFilename, UUID currentUserId) {
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        return projectFileRepository.findByProjectIdAndOriginalFilenameOrderByUploadTimestampDesc(projectId, originalFilename)
                .stream()
                .map(ProjectFileResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
