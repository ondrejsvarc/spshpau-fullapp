package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.FileDownloadDto;
import com.spshpau.projectservice.dto.ProjectFileResponseDto;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.ProjectFile;
import com.spshpau.projectservice.model.SimpleUser;
import com.spshpau.projectservice.repositories.ProjectFileRepository;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.services.ProjectService;
import com.spshpau.projectservice.services.SimpleUserService;
import com.spshpau.projectservice.services.exceptions.FileNotFoundException;
import com.spshpau.projectservice.services.exceptions.ProjectNotFoundException;
import com.spshpau.projectservice.services.filestorage.S3FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectFileServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectFileRepository projectFileRepository;
    @Mock
    private SimpleUserService simpleUserService;
    @Mock
    private ProjectService projectService;
    @Mock
    private S3FileStorageService s3FileStorageService;
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ProjectFileServiceImpl projectFileService;

    private UUID projectId;
    private UUID fileId;
    private UUID uploaderUserId;
    private String uploaderUsername = "uploader";
    private String bucketName = "test-bucket";

    private Project project;
    private SimpleUser uploader;
    private ProjectFile projectFile;

    @BeforeEach
    void setUp() throws MalformedURLException {
        projectId = UUID.randomUUID();
        fileId = UUID.randomUUID();
        uploaderUserId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);

        uploader = new SimpleUser();
        uploader.setId(uploaderUserId);
        uploader.setUsername(uploaderUsername);

        projectFile = new ProjectFile();
        projectFile.setId(fileId);
        projectFile.setProject(project);
        projectFile.setUploadedBy(uploader);
        projectFile.setOriginalFilename("test.pdf");
        projectFile.setContentType("application/pdf");
        projectFile.setFileSize(1024L);
        projectFile.setS3ObjectKey("projects/" + projectId + "/files/test.pdf");
        projectFile.setS3VersionId("s3VersionId123");
        projectFile.setUploadTimestamp(Timestamp.from(Instant.now()));
        projectFile.setDescription("A test file");

        ReflectionTestUtils.setField(projectFileService, "bucketName", bucketName);

        doNothing().when(projectService).verifyUserIsProjectMember(projectId, uploaderUserId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(simpleUserService.findUserById(uploaderUserId)).thenReturn(uploader);
        when(projectFileRepository.findByIdAndProjectId(fileId, projectId)).thenReturn(Optional.of(projectFile));
        when(s3FileStorageService.generatePresignedDownloadUrl(anyString(), anyString())).thenReturn(new URL("http://example.com/download/test.pdf"));
    }

    // --- uploadProjectFile Tests ---
    @Test
    void uploadProjectFile_success() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
        when(s3FileStorageService.uploadFile(anyString(), any(MultipartFile.class))).thenReturn("s3VersionId123");
        when(projectFileRepository.save(any(ProjectFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectFileResponseDto response = projectFileService.uploadProjectFile(projectId, uploaderUserId, uploaderUsername, multipartFile, "Test description");

        assertNotNull(response);
        assertEquals("test.pdf", response.getOriginalFilename());
        assertEquals("s3VersionId123", response.getS3VersionId());
        verify(projectService).verifyUserIsProjectMember(projectId, uploaderUserId);
        verify(s3FileStorageService).uploadFile(eq("projects/" + projectId + "/files/test.pdf"), eq(multipartFile));
        verify(projectFileRepository).save(any(ProjectFile.class));
    }

    @Test
    void uploadProjectFile_fail_emptyFile() {
        when(multipartFile.isEmpty()).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> {
            projectFileService.uploadProjectFile(projectId, uploaderUserId, uploaderUsername, multipartFile, "Test");
        });
    }

    @Test
    void uploadProjectFile_fail_invalidContentType() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/gif");
        assertThrows(IllegalArgumentException.class, () -> {
            projectFileService.uploadProjectFile(projectId, uploaderUserId, uploaderUsername, multipartFile, "Test");
        });
    }

    @Test
    void uploadProjectFile_fail_fileTooLarge() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getSize()).thenReturn(60 * 1024 * 1024L);
        assertThrows(IllegalArgumentException.class, () -> {
            projectFileService.uploadProjectFile(projectId, uploaderUserId, uploaderUsername, multipartFile, "Test");
        });
    }

    @Test
    void uploadProjectFile_fail_projectNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getSize()).thenReturn(1024L);

        assertThrows(ProjectNotFoundException.class, () -> {
            projectFileService.uploadProjectFile(projectId, uploaderUserId, uploaderUsername, multipartFile, "Test description");
        });
        verify(projectService).verifyUserIsProjectMember(projectId, uploaderUserId);
    }


    @Test
    void uploadProjectFile_fail_s3UploadReturnsNullVersionId() throws IOException {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
        when(s3FileStorageService.uploadFile(anyString(), any(MultipartFile.class))).thenReturn(null);

        assertThrows(IOException.class, () -> {
            projectFileService.uploadProjectFile(projectId, uploaderUserId, uploaderUsername, multipartFile, "Test description");
        });
    }

    // --- getProjectFiles Tests ---
    @Test
    void getProjectFiles_success() {
        when(projectFileRepository.findLatestVersionOfEachFileByProjectId(projectId)).thenReturn(Collections.singletonList(projectFile));
        List<ProjectFileResponseDto> response = projectFileService.getProjectFiles(projectId, uploaderUserId);
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(projectFile.getOriginalFilename(), response.get(0).getOriginalFilename());
        verify(projectService).verifyUserIsProjectMember(projectId, uploaderUserId);
    }

    // --- getProjectFileMetadata Tests ---
    @Test
    void getProjectFileMetadata_success() {
        ProjectFileResponseDto response = projectFileService.getProjectFileMetadata(projectId, fileId, uploaderUserId);
        assertNotNull(response);
        assertEquals(projectFile.getOriginalFilename(), response.getOriginalFilename());
        verify(projectService).verifyUserIsProjectMember(projectId, uploaderUserId);
    }

    @Test
    void getProjectFileMetadata_fail_notFound() {
        when(projectFileRepository.findByIdAndProjectId(fileId, projectId)).thenReturn(Optional.empty());
        assertThrows(FileNotFoundException.class, () -> {
            projectFileService.getProjectFileMetadata(projectId, fileId, uploaderUserId);
        });
    }

    // --- generateDownloadUrl Tests ---
    @Test
    void generateDownloadUrl_success() throws MalformedURLException {
        FileDownloadDto response = projectFileService.generateDownloadUrl(projectId, fileId, uploaderUserId);
        assertNotNull(response);
        assertEquals(projectFile.getOriginalFilename(), response.getOriginalFilename());
        assertEquals("http://example.com/download/test.pdf", response.getDownloadUrl());
        verify(s3FileStorageService).generatePresignedDownloadUrl(projectFile.getS3ObjectKey(), projectFile.getS3VersionId());
    }

    @Test
    void generateDownloadUrl_fail_s3KeyMissing() {
        projectFile.setS3ObjectKey(null);
        when(projectFileRepository.findByIdAndProjectId(fileId, projectId)).thenReturn(Optional.of(projectFile));
        assertThrows(IllegalStateException.class, () -> {
            projectFileService.generateDownloadUrl(projectId, fileId, uploaderUserId);
        });
    }

    // --- deleteProjectFile Tests ---
    @Test
    void deleteProjectFile_success() {
        doNothing().when(s3FileStorageService).deleteFileVersion(projectFile.getS3ObjectKey(), projectFile.getS3VersionId());
        doNothing().when(projectFileRepository).delete(projectFile);

        projectFileService.deleteProjectFile(projectId, fileId, uploaderUserId);

        verify(s3FileStorageService).deleteFileVersion(projectFile.getS3ObjectKey(), projectFile.getS3VersionId());
        verify(projectFileRepository).delete(projectFile);
    }

    @Test
    void deleteProjectFile_fail_s3DeleteFails() {
        doThrow(new RuntimeException("S3 Error")).when(s3FileStorageService).deleteFileVersion(anyString(), anyString());
        assertThrows(IllegalStateException.class, () -> {
            projectFileService.deleteProjectFile(projectId, fileId, uploaderUserId);
        });
    }

    // --- getAllVersionsOfFile Tests ---
    @Test
    void getAllVersionsOfFile_success() {
        String originalFilename = "test.pdf";
        when(projectFileRepository.findByProjectIdAndOriginalFilenameOrderByUploadTimestampDesc(projectId, originalFilename))
                .thenReturn(Collections.singletonList(projectFile));

        List<ProjectFileResponseDto> response = projectFileService.getAllVersionsOfFile(projectId, originalFilename, uploaderUserId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(originalFilename, response.get(0).getOriginalFilename());
        verify(projectService).verifyUserIsProjectMember(projectId, uploaderUserId);
    }
}