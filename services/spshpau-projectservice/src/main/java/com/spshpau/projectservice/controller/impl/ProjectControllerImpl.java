package com.spshpau.projectservice.controller.impl;

import com.spshpau.projectservice.controller.ProjectController;
import com.spshpau.projectservice.dto.ProjectCreateDto;
import com.spshpau.projectservice.dto.ProjectResponseDto;
import com.spshpau.projectservice.dto.ProjectUpdateDto;
import com.spshpau.projectservice.dto.UserSummaryDto;
import com.spshpau.projectservice.services.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectControllerImpl implements ProjectController {
    private final ProjectService projectService;

    private UUID getUserIdFromJwt(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private String getUsernameFromJwt(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }
    private String getFirstNameFromJwt(Jwt jwt) {
        return jwt.getClaimAsString("given_name");
    }

    private String getLastNameFromJwt(Jwt jwt) {
        return jwt.getClaimAsString("family_name");
    }


    @Override
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectCreateDto projectDto, @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = getUserIdFromJwt(jwt);
        String ownerUsername = getUsernameFromJwt(jwt);
        String ownerFirstName = getFirstNameFromJwt(jwt);
        String ownerLastName = getLastNameFromJwt(jwt);

        ProjectResponseDto createdProject = projectService.createProject(projectDto, ownerId, ownerUsername, ownerFirstName, ownerLastName, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @Override
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable UUID projectId, @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        ProjectResponseDto project = projectService.getProjectById(projectId, currentUserId);
        return ResponseEntity.ok(project);
    }

    @Override
    @GetMapping("/owned")
    public ResponseEntity<Page<ProjectResponseDto>> getMyOwnedProjects(@AuthenticationPrincipal Jwt jwt,
                                                                       @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        UUID ownerId = getUserIdFromJwt(jwt);
        Page<ProjectResponseDto> projects = projectService.getOwnedProjects(ownerId, pageable);
        return ResponseEntity.ok(projects);
    }

    @Override
    @GetMapping("/collaborating")
    public ResponseEntity<Page<ProjectResponseDto>> getMyCollaboratingProjects(@AuthenticationPrincipal Jwt jwt,
                                                                               @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        UUID collaboratorId = getUserIdFromJwt(jwt);
        Page<ProjectResponseDto> projects = projectService.getCollaboratingProjects(collaboratorId, pageable);
        return ResponseEntity.ok(projects);
    }

    @Override
    @GetMapping("/{projectId}/owner")
    public ResponseEntity<UserSummaryDto> getProjectOwner(@PathVariable UUID projectId) {
        UserSummaryDto owner = projectService.getProjectOwner(projectId);
        return ResponseEntity.ok(owner);
    }

    @Override
    @GetMapping("/{projectId}/collaborators")
    public ResponseEntity<Page<UserSummaryDto>> getProjectCollaborators(@PathVariable UUID projectId,
                                                                        @PageableDefault(size = 10) Pageable pageable) {
        Page<UserSummaryDto> collaborators = projectService.getProjectCollaborators(projectId, pageable);
        return ResponseEntity.ok(collaborators);
    }

    @Override
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProjectInfo(@PathVariable UUID projectId,
                                                                @Valid @RequestBody ProjectUpdateDto projectDto,
                                                                @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = getUserIdFromJwt(jwt);
        ProjectResponseDto updatedProject = projectService.updateProject(projectId, projectDto, ownerId);
        return ResponseEntity.ok(updatedProject);
    }

    @Override
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID projectId, @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = getUserIdFromJwt(jwt);
        projectService.deleteProject(projectId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{projectId}/collaborators/{collaboratorId}")
    public ResponseEntity<ProjectResponseDto> addCollaborator(@PathVariable UUID projectId,
                                                              @PathVariable UUID collaboratorId,
                                                              @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = getUserIdFromJwt(jwt);
        String bearerToken = "Bearer " + jwt.getTokenValue();
        ProjectResponseDto project = projectService.addCollaborator(projectId, collaboratorId, ownerId, bearerToken);
        return ResponseEntity.ok(project);
    }

    @Override
    @DeleteMapping("/{projectId}/collaborators/{collaboratorId}")
    public ResponseEntity<Void> removeCollaborator(@PathVariable UUID projectId,
                                                   @PathVariable UUID collaboratorId,
                                                   @AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = getUserIdFromJwt(jwt);
        projectService.removeCollaborator(projectId, collaboratorId, ownerId);
        return ResponseEntity.noContent().build();
    }
}
