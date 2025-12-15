package com.spshpau.projectservice.controller;

import com.spshpau.projectservice.dto.FileDownloadDto;
import com.spshpau.projectservice.dto.ProjectFileResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ProjectFileController {

    /**
     * Uploads a file to a specified project.
     *
     * @param projectId The ID of the project to which the file will be uploaded.
     * @param file The file to upload.
     * @param description An optional description for the file.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the ProjectFileResponseDto for the uploaded file and HTTP status.
     * @throws IOException If an I/O error occurs during file processing.
     * Example Response (201 Created):
     * <pre>{@code
     * {
     * "id": "f1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
     * "originalFilename": "document.pdf",
     * "contentType": "application/pdf",
     * "fileSize": 102400,
     * "uploadTimestamp": "2024-05-09T10:30:00.000+00:00",
     * "description": "Project proposal document",
     * "uploadedBy": {
     * "id": "user-uuid-uploader",
     * "username": "uploader_user",
     * "firstName": "Alice",
     * "lastName": "Wonderland",
     * "location": "Remote"
     * },
     * "s3ObjectKey": "projects/c1d2e3f4.../files/document.pdf",
     * "s3VersionId": "versionId123abc",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * }
     * }</pre>
     */
    ResponseEntity<ProjectFileResponseDto> uploadProjectFile(@PathVariable UUID projectId,
                                                             @RequestPart("file") MultipartFile file,
                                                             @RequestPart(value = "description", required = false) String description,
                                                             Jwt jwt) throws IOException;

    /**
     * Retrieves a list of files for a specified project.
     * This returns the latest version of each file.
     *
     * @param projectId The ID of the project for which to retrieve files.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing a List of ProjectFileResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * [
     * {
     * "id": "f1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
     * "originalFilename": "document.pdf",
     * "contentType": "application/pdf",
     * "fileSize": 102400,
     * "uploadTimestamp": "2024-05-09T10:30:00.000+00:00",
     * "description": "Project proposal document",
     * "uploadedBy": {"id": "user-uuid-uploader", ...},
     * "s3ObjectKey": "projects/c1d2e3f4.../files/document.pdf",
     * "s3VersionId": "versionId123abc",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * },
     * {
     * "id": "a2b3c4d5-e6f7-g8h9-i0j1-k2l3m4n5o6p7",
     * "originalFilename": "image.png",
     * "contentType": "image/png",
     * "fileSize": 51200,
     * "uploadTimestamp": "2024-05-08T15:00:00.000+00:00",
     * "description": "Project logo",
     * "uploadedBy": {"id": "user-uuid-uploader", ...},
     * "s3ObjectKey": "projects/c1d2e3f4.../files/image.png",
     * "s3VersionId": "versionId456def",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * }
     * ]
     * }</pre>
     */
    ResponseEntity<List<ProjectFileResponseDto>> getProjectFiles(@PathVariable UUID projectId,
                                                                 Jwt jwt);

    /**
     * Retrieves the metadata for a specific file within a project.
     *
     * @param projectId The ID of the project to which the file belongs.
     * @param fileId The ID of the file to retrieve metadata for.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the ProjectFileResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "id": "f1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
     * "originalFilename": "document.pdf",
     * "contentType": "application/pdf",
     * "fileSize": 102400,
     * "uploadTimestamp": "2024-05-09T10:30:00.000+00:00",
     * "description": "Project proposal document",
     * "uploadedBy": {"id": "user-uuid-uploader", ...},
     * "s3ObjectKey": "projects/c1d2e3f4.../files/document.pdf",
     * "s3VersionId": "versionId123abc",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * }
     * }</pre>
     */
    ResponseEntity<ProjectFileResponseDto> getProjectFileMetadata(@PathVariable UUID projectId,
                                                                  @PathVariable UUID fileId,
                                                                  Jwt jwt);

    /**
     * Generates a pre-signed download URL for a specific project file.
     *
     * @param projectId The ID of the project to which the file belongs.
     * @param fileId The ID of the file for which to generate the download URL.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the FileDownloadDto (which includes the URL) and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "downloadUrl": "https://s3.amazonaws.com/bucket/projects/c1d2e3f4.../files/document.pdf?AWSAccessKeyId=...",
     * "originalFilename": "document.pdf"
     * }
     * }</pre>
     */
    ResponseEntity<FileDownloadDto> getProjectFileDownloadUrl(@PathVariable UUID projectId,
                                                              @PathVariable UUID fileId,
                                                              Jwt jwt);

    /**
     * Deletes a specific file from a project.
     *
     * @param projectId The ID of the project from which to delete the file.
     * @param fileId The ID of the file to delete.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity with no content and HTTP status 204.
     */
    ResponseEntity<Void> deleteProjectFile(@PathVariable UUID projectId,
                                           @PathVariable UUID fileId,
                                           Jwt jwt);

    /**
     * Retrieves all versions of a specific file by its original filename within a project.
     *
     * @param projectId The ID of the project.
     * @param originalFilename The original filename to search for.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing a List of ProjectFileResponseDto representing all versions of the file, ordered by upload timestamp descending.
     * Example Response (200 OK):
     * <pre>{@code
     * [
     * {
     * "id": "f1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
     * "originalFilename": "document.pdf",
     * "contentType": "application/pdf",
     * "fileSize": 102400,
     * "uploadTimestamp": "2024-05-09T10:30:00.000+00:00",
     * "description": "Latest version",
     * "uploadedBy": {"id": "user-uuid-uploader", ...},
     * "s3ObjectKey": "projects/c1d2e3f4.../files/document.pdf",
     * "s3VersionId": "versionId123abc",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * },
     * {
     * "id": "k2l3m4n5-o6p7-q8r9-s0t1-u2v3w4x5y6z7",
     * "originalFilename": "document.pdf",
     * "contentType": "application/pdf",
     * "fileSize": 98765,
     * "uploadTimestamp": "2024-05-07T14:20:00.000+00:00",
     * "description": "Previous version",
     * "uploadedBy": {"id": "user-uuid-uploader", ...},
     * "s3ObjectKey": "projects/c1d2e3f4.../files/document.pdf",
     * "s3VersionId": "versionIdOldVersion",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * }
     * ]
     * }</pre>
     */
    ResponseEntity<List<ProjectFileResponseDto>> getAllVersionsOfFileByName(
            @PathVariable UUID projectId,
            @RequestParam("filename") String originalFilename,
            Jwt jwt
    );
}