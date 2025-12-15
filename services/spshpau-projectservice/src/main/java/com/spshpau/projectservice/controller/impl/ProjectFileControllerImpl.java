package com.spshpau.projectservice.controller.impl;

import com.spshpau.projectservice.controller.ProjectFileController;
import com.spshpau.projectservice.dto.FileDownloadDto;
import com.spshpau.projectservice.dto.ProjectFileResponseDto;
import com.spshpau.projectservice.services.ProjectFileService;
import com.spshpau.projectservice.services.SimpleUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/files")
@RequiredArgsConstructor
public class ProjectFileControllerImpl implements ProjectFileController {

    private final ProjectFileService projectFileService;
    private final SimpleUserService simpleUserService;

    private UUID getUserIdFromJwt(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
    private String getUsernameFromJwt(Jwt jwt) { return jwt.getClaimAsString("preferred_username"); }


    @Override
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProjectFileResponseDto> uploadProjectFile(
            @PathVariable UUID projectId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        UUID uploaderUserId = getUserIdFromJwt(jwt);
        String uploaderUsername = getUsernameFromJwt(jwt);
        ProjectFileResponseDto responseDto = projectFileService.uploadProjectFile(
                projectId, uploaderUserId, uploaderUsername, file, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ProjectFileResponseDto>> getProjectFiles(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        List<ProjectFileResponseDto> files = projectFileService.getProjectFiles(projectId, currentUserId);
        return ResponseEntity.ok(files);
    }

    @Override
    @GetMapping("/{fileId}/metadata")
    public ResponseEntity<ProjectFileResponseDto> getProjectFileMetadata(
            @PathVariable UUID projectId,
            @PathVariable UUID fileId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        ProjectFileResponseDto metadata = projectFileService.getProjectFileMetadata(projectId, fileId, currentUserId);
        return ResponseEntity.ok(metadata);
    }

    @Override
    @GetMapping("/{fileId}/download-url")
    public ResponseEntity<FileDownloadDto> getProjectFileDownloadUrl(
            @PathVariable UUID projectId,
            @PathVariable UUID fileId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        FileDownloadDto downloadDto = projectFileService.generateDownloadUrl(projectId, fileId, currentUserId);
        return ResponseEntity.ok(downloadDto);
    }

    @Override
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteProjectFile(
            @PathVariable UUID projectId,
            @PathVariable UUID fileId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        projectFileService.deleteProjectFile(projectId, fileId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/versions")
    public ResponseEntity<List<ProjectFileResponseDto>> getAllVersionsOfFileByName(
            @PathVariable UUID projectId,
            @RequestParam("filename") String originalFilename,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        List<ProjectFileResponseDto> versions = projectFileService.getAllVersionsOfFile(projectId, originalFilename, currentUserId);
        return ResponseEntity.ok(versions);
    }
}
