package com.spshpau.projectservice.services;

import com.spshpau.projectservice.dto.ProjectCreateDto;
import com.spshpau.projectservice.dto.ProjectResponseDto;
import com.spshpau.projectservice.dto.ProjectUpdateDto;
import com.spshpau.projectservice.dto.UserSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProjectService {
    ProjectResponseDto createProject(ProjectCreateDto projectDto, UUID ownerId, String ownerUsername, String ownerFirstName, String ownerLastName, String ownerLocation);
    ProjectResponseDto getProjectById(UUID projectId, UUID currentUserId);
    Page<ProjectResponseDto> getOwnedProjects(UUID ownerId, Pageable pageable);
    Page<ProjectResponseDto> getCollaboratingProjects(UUID collaboratorId, Pageable pageable);
    UserSummaryDto getProjectOwner(UUID projectId);
    Page<UserSummaryDto> getProjectCollaborators(UUID projectId, Pageable pageable);
    ProjectResponseDto updateProject(UUID projectId, ProjectUpdateDto projectDto, UUID ownerId);
    void deleteProject(UUID projectId, UUID ownerId);
    ProjectResponseDto addCollaborator(UUID projectId, UUID collaboratorUserId, UUID ownerId, String bearerToken);
    void removeCollaborator(UUID projectId, UUID collaboratorUserId, UUID ownerId);
    void verifyUserIsProjectMember(UUID projectId, UUID userId);
    boolean isUserOwnerOfProject(UUID projectId, UUID userId);
}
