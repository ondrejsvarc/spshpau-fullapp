package com.spshpau.projectservice.services;

import com.spshpau.projectservice.dto.FileDownloadDto;
import com.spshpau.projectservice.dto.ProjectFileResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ProjectFileService {
    ProjectFileResponseDto uploadProjectFile(UUID projectId, UUID uploaderUserId, String uploaderUsername,
                                             MultipartFile file, String description) throws IOException;

    List<ProjectFileResponseDto> getProjectFiles(UUID projectId, UUID currentUserId);

    ProjectFileResponseDto getProjectFileMetadata(UUID projectId, UUID fileId, UUID currentUserId);

    FileDownloadDto generateDownloadUrl(UUID projectId, UUID fileId, UUID currentUserId);

    void deleteProjectFile(UUID projectId, UUID fileId, UUID currentUserId);

    List<ProjectFileResponseDto> getAllVersionsOfFile(UUID projectId, String originalFilename, UUID currentUserId);
}
