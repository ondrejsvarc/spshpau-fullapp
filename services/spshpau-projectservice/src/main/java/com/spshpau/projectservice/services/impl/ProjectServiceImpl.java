package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.ProjectCreateDto;
import com.spshpau.projectservice.dto.ProjectResponseDto;
import com.spshpau.projectservice.dto.ProjectUpdateDto;
import com.spshpau.projectservice.dto.UserSummaryDto;
import com.spshpau.projectservice.repositories.ProjectTaskRepository;
import com.spshpau.projectservice.services.exceptions.*;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.SimpleUser;
import com.spshpau.projectservice.otherservices.UserClient;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.services.ProjectService;
import com.spshpau.projectservice.services.SimpleUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final SimpleUserService simpleUserService;
    private final UserClient userClient;


    @Override
    @Transactional
    public ProjectResponseDto createProject(ProjectCreateDto projectDto, UUID ownerId, String ownerUsername, String ownerFirstName, String ownerLastName, String ownerLocation) {
        log.info("Creating project '{}' for owner ID: {}", projectDto.getTitle(), ownerId);
        SimpleUser owner = simpleUserService.getOrCreateSimpleUser(ownerId, ownerUsername, ownerFirstName, ownerLastName, ownerLocation);
        Project project = new Project();
        project.setTitle(projectDto.getTitle());
        project.setDescription(projectDto.getDescription());
        project.setOwner(owner);
        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getId());
        return ProjectResponseDto.fromEntity(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(UUID projectId, UUID currentUserId) {
        log.info("User {} attempting to retrieve project with ID: {}", currentUserId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found with ID: {} for user {}", projectId, currentUserId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });

        boolean isOwner = project.getOwner().getId().equals(currentUserId);
        boolean isCollaborator = project.getCollaborators().stream()
                .anyMatch(collaborator -> collaborator.getId().equals(currentUserId));

        if (!isOwner && !isCollaborator) {
            log.error("User {} is not authorized to access project {}", currentUserId, projectId);
            throw new UnauthorizedOperationException("User is not authorized to access this project.");
        }

        log.info("Project {} retrieved successfully by user {}", projectId, currentUserId);
        return ProjectResponseDto.fromEntity(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> getOwnedProjects(UUID ownerId, Pageable pageable) {
        log.info("Retrieving projects owned by user ID: {} with pageable: {}", ownerId, pageable);
        Page<Project> projectPage = projectRepository.findByOwnerId(ownerId, pageable);
        log.info("Found {} projects owned by user ID: {}", projectPage.getTotalElements(), ownerId);
        return projectPage.map(ProjectResponseDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> getCollaboratingProjects(UUID collaboratorId, Pageable pageable) {
        log.info("Retrieving projects where user ID: {} is a collaborator, with pageable: {}", collaboratorId, pageable);
        Page<Project> projectPage = projectRepository.findByCollaboratorsId(collaboratorId, pageable);
        log.info("Found {} projects where user ID: {} is a collaborator", projectPage.getTotalElements(), collaboratorId);
        return projectPage.map(ProjectResponseDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getProjectOwner(UUID projectId) {
        log.info("Retrieving owner for project ID: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found when trying to get owner for project ID: {}", projectId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });
        UserSummaryDto ownerDto = UserSummaryDto.fromEntity(project.getOwner());
        log.info("Owner for project ID {} is user ID {}", projectId, ownerDto.getId());
        return ownerDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDto> getProjectCollaborators(UUID projectId, Pageable pageable) {
        log.info("Retrieving collaborators for project ID: {} with pageable: {}", projectId, pageable);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found when trying to get collaborators for project ID: {}", projectId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });

        List<SimpleUser> collaboratorsList = new ArrayList<>(project.getCollaborators());
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<UserSummaryDto> dtoList;

        if (collaboratorsList.isEmpty() || startItem >= collaboratorsList.size()) {
            dtoList = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, collaboratorsList.size());
            dtoList = collaboratorsList.subList(startItem, toIndex).stream()
                    .map(UserSummaryDto::fromEntity)
                    .collect(Collectors.toList());
        }
        log.info("Found {} collaborators for project ID {}", dtoList.size(), projectId);
        return new PageImpl<>(dtoList, pageable, collaboratorsList.size());
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProject(UUID projectId, ProjectUpdateDto projectDto, UUID ownerId) {
        log.info("User {} attempting to update project ID: {}", ownerId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found with ID: {} for update by owner {}", projectId, ownerId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });

        if (!project.getOwner().getId().equals(ownerId)) {
            log.error("User {} is not the owner of project {}. Update denied.", ownerId, projectId);
            throw new UnauthorizedOperationException("User is not the owner of this project.");
        }

        if (projectDto.getTitle() != null) {
            project.setTitle(projectDto.getTitle());
        }
        if (projectDto.getDescription() != null) {
            project.setDescription(projectDto.getDescription());
        }
        Project updatedProject = projectRepository.save(project);
        log.info("Project {} updated successfully by owner {}", projectId, ownerId);
        return ProjectResponseDto.fromEntity(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(UUID projectId, UUID ownerId) {
        log.info("User {} attempting to delete project ID: {}", ownerId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found with ID: {} for deletion by owner {}", projectId, ownerId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });

        if (!project.getOwner().getId().equals(ownerId)) {
            log.error("User {} is not the owner of project {}. Deletion denied.", ownerId, projectId);
            throw new UnauthorizedOperationException("User is not the owner of this project.");
        }
        projectRepository.delete(project);
        log.info("Project {} deleted successfully by owner {}", projectId, ownerId);
    }

    @Override
    @Transactional
    public ProjectResponseDto addCollaborator(UUID projectId, UUID collaboratorUserId, UUID ownerId, String bearerToken) {
        log.info("Owner {} attempting to add collaborator {} to project {}", ownerId, collaboratorUserId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found (ID: {}) when owner {} trying to add collaborator {}", projectId, ownerId, collaboratorUserId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });

        if (!project.getOwner().getId().equals(ownerId)) {
            log.error("User {} is not the owner of project {}. Cannot add collaborator.", ownerId, projectId);
            throw new UnauthorizedOperationException("Only the project owner can add collaborators.");
        }

        if (project.getCollaborators().stream().anyMatch(c -> c.getId().equals(collaboratorUserId))) {
            log.warn("User {} is already a collaborator on project {}. Request by owner {}.", collaboratorUserId, projectId, ownerId);
            throw new CollaboratorAlreadyExistsException("User is already a collaborator on this project.");
        }
        if (project.getOwner().getId().equals(collaboratorUserId)) {
            log.error("Owner {} cannot add themselves as collaborator to project {}", ownerId, projectId);
            throw new IllegalArgumentException("Owner cannot be added as a collaborator to their own project.");
        }

        log.debug("Fetching owner's connections using UserClient for project {}, owner {}", projectId, ownerId);
        List<UserSummaryDto> connections = userClient.findConnectionsByJwt(bearerToken);
        UserSummaryDto collaboratorSummary = connections.stream()
                .filter(conn -> conn.getId().equals(collaboratorUserId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("User {} is not a connection of owner {} for project {}", collaboratorUserId, ownerId, projectId);
                    return new NotConnectedException("Collaborator must be one of the owner's connections.");
                });

        SimpleUser collaborator = simpleUserService.getOrCreateSimpleUser(collaboratorSummary);

        project.getCollaborators().add(collaborator);
        collaborator.getCollaboratingProjects().add(project);

        Project updatedProject = projectRepository.save(project);
        log.info("Collaborator {} added successfully to project {} by owner {}", collaboratorUserId, projectId, ownerId);
        return ProjectResponseDto.fromEntity(updatedProject);
    }

    @Override
    @Transactional
    public void removeCollaborator(UUID projectId, UUID collaboratorUserId, UUID ownerId) {
        log.info("Owner {} attempting to remove collaborator {} from project {}", ownerId, collaboratorUserId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found (ID: {}) when owner {} trying to remove collaborator {}", projectId, ownerId, collaboratorUserId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });

        if (!project.getOwner().getId().equals(ownerId)) {
            log.error("User {} is not the owner of project {}. Cannot remove collaborator.", ownerId, projectId);
            throw new UnauthorizedOperationException("Only the project owner can remove collaborators.");
        }

        SimpleUser collaborator = simpleUserService.findUserById(collaboratorUserId); // Throws UserNotFound if not exists

        if (!project.getCollaborators().remove(collaborator)) {
            log.warn("Collaborator {} not found on project {} for removal by owner {}.", collaboratorUserId, projectId, ownerId);
            throw new CollaboratorNotFoundException("Collaborator not found on this project.");
        }
        collaborator.getCollaboratingProjects().remove(project);

        log.info("Unassigning user {} from all tasks in project {} as part of collaborator removal", collaboratorUserId, projectId);
        projectTaskRepository.unassignUserFromTasksInProject(projectId, collaboratorUserId);

        projectRepository.save(project);
        log.info("Collaborator {} removed successfully from project {} by owner {}", collaboratorUserId, projectId, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyUserIsProjectMember(UUID projectId, UUID userId) {
        log.debug("Verifying if user {} is a member of project {}", userId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found: {} during membership verification for user {}", projectId, userId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });

        boolean isOwner = project.getOwner().getId().equals(userId);
        boolean isCollaborator = project.getCollaborators().stream()
                .anyMatch(collaborator -> collaborator.getId().equals(userId));

        if (!isOwner && !isCollaborator) {
            log.warn("User {} is not authorized for project {} operation.", userId, projectId);
            throw new UnauthorizedOperationException("User is not authorized for this project operation.");
        }
        log.debug("User {} verified as a member of project {}", userId, projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserOwnerOfProject(UUID projectId, UUID userId) {
        log.debug("Checking if user {} is owner of project {}", userId, projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found: {} when checking ownership for user {}", projectId, userId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });
        boolean isOwner = project.getOwner().getId().equals(userId);
        log.debug("User {} owner status for project {}: {}", userId, projectId, isOwner);
        return isOwner;
    }
}