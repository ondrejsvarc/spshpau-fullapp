package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.MilestoneCreateDto;
import com.spshpau.projectservice.dto.MilestoneResponseDto;
import com.spshpau.projectservice.dto.MilestoneUpdateDto;
import com.spshpau.projectservice.services.exceptions.MilestoneNotFoundException;
import com.spshpau.projectservice.services.exceptions.ProjectNotFoundException;
import com.spshpau.projectservice.services.exceptions.UnauthorizedOperationException;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.ProjectMilestone;
import com.spshpau.projectservice.repositories.ProjectMilestoneRepository;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.services.ProjectMilestoneService;
import com.spshpau.projectservice.services.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectMilestoneServiceImpl implements ProjectMilestoneService {

    private final ProjectRepository projectRepository;
    private final ProjectMilestoneRepository projectMilestoneRepository;
    private final ProjectService projectService;

    @Override
    @Transactional
    public MilestoneResponseDto createMilestone(UUID projectId, MilestoneCreateDto milestoneDto, UUID currentUserId) {
        log.info("User {} creating milestone for project {}", currentUserId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found with ID: {} during milestone creation by user {}", projectId, currentUserId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });

        ProjectMilestone milestone = new ProjectMilestone();
        milestone.setProject(project);
        milestone.setTitle(milestoneDto.getTitle());
        milestone.setDescription(milestoneDto.getDescription());
        milestone.setDueDate(milestoneDto.getDueDate());

        ProjectMilestone savedMilestone = projectMilestoneRepository.save(milestone);
        log.info("Milestone {} created successfully for project {} by user {}", savedMilestone.getId(), projectId, currentUserId);
        return MilestoneResponseDto.fromEntity(savedMilestone);
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneResponseDto getMilestoneById(UUID projectId, UUID milestoneId, UUID currentUserId) {
        log.info("User {} attempting to get milestone {} for project {}", currentUserId, milestoneId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectMilestone milestone = projectMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> {
                    log.warn("Milestone not found with ID: {} for project {} by user {}", milestoneId, projectId, currentUserId);
                    return new MilestoneNotFoundException("Milestone not found with ID: " + milestoneId);
                });
        if (!milestone.getProject().getId().equals(projectId)) {
            log.error("Unauthorized attempt by user {} to access milestone {} not belonging to project {}", currentUserId, milestoneId, projectId);
            throw new UnauthorizedOperationException("Milestone does not belong to the specified project.");
        }
        log.info("Milestone {} retrieved successfully for project {} by user {}", milestoneId, projectId, currentUserId);
        return MilestoneResponseDto.fromEntity(milestone);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MilestoneResponseDto> getMilestonesForProject(UUID projectId, UUID currentUserId, Pageable pageable) {
        log.info("User {} listing milestones for project {} with pageable: {}", currentUserId, projectId, pageable);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        Page<ProjectMilestone> milestonePage = projectMilestoneRepository.findByProjectId(projectId, pageable);
        log.info("Found {} milestones for project {} for user {}", milestonePage.getTotalElements(), projectId, currentUserId);
        return milestonePage.map(MilestoneResponseDto::fromEntity);
    }

    @Override
    @Transactional
    public MilestoneResponseDto updateMilestone(UUID projectId, UUID milestoneId, MilestoneUpdateDto milestoneDto, UUID currentUserId) {
        log.info("User {} updating milestone {} for project {}", currentUserId, milestoneId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectMilestone milestone = projectMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> {
                    log.warn("Milestone not found with ID: {} during update by user {} for project {}", milestoneId, currentUserId, projectId);
                    return new MilestoneNotFoundException("Milestone not found with ID: " + milestoneId);
                });

        if (!milestone.getProject().getId().equals(projectId)) {
            log.error("Unauthorized attempt by user {} to update milestone {} not belonging to project {}", currentUserId, milestoneId, projectId);
            throw new UnauthorizedOperationException("Milestone does not belong to the specified project.");
        }

        if (milestoneDto.getTitle() != null) milestone.setTitle(milestoneDto.getTitle());
        if (milestoneDto.getDescription() != null) milestone.setDescription(milestoneDto.getDescription());
        if (milestoneDto.getDueDate() == null && milestone.getDueDate() != null) {
            milestone.setDueDate(null);
            log.debug("Milestone {} due date cleared for project {} by user {}", milestoneId, projectId, currentUserId);
        } else if (milestoneDto.getDueDate() != null) {
            milestone.setDueDate(milestoneDto.getDueDate());
        }


        ProjectMilestone updatedMilestone = projectMilestoneRepository.save(milestone);
        log.info("Milestone {} updated successfully for project {} by user {}", milestoneId, projectId, currentUserId);
        return MilestoneResponseDto.fromEntity(updatedMilestone);
    }

    @Override
    @Transactional
    public void deleteMilestone(UUID projectId, UUID milestoneId, UUID currentUserId) {
        log.info("User {} deleting milestone {} from project {}", currentUserId, milestoneId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectMilestone milestone = projectMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> {
                    log.warn("Milestone not found with ID: {} during deletion by user {} for project {}", milestoneId, currentUserId, projectId);
                    return new MilestoneNotFoundException("Milestone not found with ID: " + milestoneId);
                });
        if (!milestone.getProject().getId().equals(projectId)) {
            log.error("Unauthorized attempt by user {} to delete milestone {} not belonging to project {}", currentUserId, milestoneId, projectId);
            throw new UnauthorizedOperationException("Milestone does not belong to the specified project.");
        }
        projectMilestoneRepository.delete(milestone);
        log.info("Milestone {} deleted successfully from project {} by user {}", milestoneId, projectId, currentUserId);
    }
}