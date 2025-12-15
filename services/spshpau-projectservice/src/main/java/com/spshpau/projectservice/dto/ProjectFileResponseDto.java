package com.spshpau.projectservice.dto;

import com.spshpau.projectservice.model.ProjectFile;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
public class ProjectFileResponseDto {
    private UUID id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Timestamp uploadTimestamp;
    private String description;
    private UserSummaryDto uploadedBy;
    private String s3ObjectKey;
    private String s3VersionId;
    private UUID projectId;

    public static ProjectFileResponseDto fromEntity(ProjectFile file) {
        if (file == null) return null;
        return ProjectFileResponseDto.builder()
                .id(file.getId())
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .uploadTimestamp(file.getUploadTimestamp())
                .description(file.getDescription())
                .uploadedBy(UserSummaryDto.fromEntity(file.getUploadedBy()))
                .s3ObjectKey(file.getS3ObjectKey())
                .s3VersionId(file.getS3VersionId())
                .projectId(file.getProject().getId())
                .build();
    }
}
