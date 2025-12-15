package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.TaskCreateDto;
import com.spshpau.projectservice.dto.TaskResponseDto;
import com.spshpau.projectservice.dto.TaskUpdateDto;
import com.spshpau.projectservice.services.exceptions.*;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.ProjectTask;
import com.spshpau.projectservice.model.SimpleUser;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.repositories.ProjectTaskRepository;
import com.spshpau.projectservice.services.ProjectService;
import com.spshpau.projectservice.services.ProjectTaskService;
import com.spshpau.projectservice.services.SimpleUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final SimpleUserService simpleUserService;
    private final ProjectService projectService;

    @Override
    @Transactional
    public TaskResponseDto createTask(UUID projectId, TaskCreateDto taskDto, UUID currentUserId) {
        log.info("User {} creating task for project {}", currentUserId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found with ID: {} during task creation by user {}", projectId, currentUserId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });

        ProjectTask task = new ProjectTask();
        task.setProject(project);
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setCreatedAt(Timestamp.from(Instant.now()));
        task.setDueDate(taskDto.getDueDate());
        task.setStatus(taskDto.getStatus());

        if (taskDto.getAssignedUserId() != null) {
            log.debug("Assigning task to user {} for project {}", taskDto.getAssignedUserId(), projectId);
            projectService.verifyUserIsProjectMember(projectId, taskDto.getAssignedUserId());
            SimpleUser assignedUser = simpleUserService.findUserById(taskDto.getAssignedUserId());
            task.setAssignedUser(assignedUser);
        }

        ProjectTask savedTask = projectTaskRepository.save(task);
        log.info("Task {} created successfully for project {} by user {}", savedTask.getId(), projectId, currentUserId);
        return TaskResponseDto.fromEntity(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(UUID projectId, UUID taskId, UUID currentUserId) {
        log.info("User {} attempting to get task {} for project {}", currentUserId, taskId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found with ID: {} for project {} by user {}", taskId, projectId, currentUserId);
                    return new TaskNotFoundException("Task not found with ID: " + taskId);
                });
        if (!task.getProject().getId().equals(projectId)) {
            log.error("Unauthorized attempt by user {} to access task {} not belonging to project {}", currentUserId, taskId, projectId);
            throw new UnauthorizedOperationException("Task does not belong to the specified project.");
        }
        log.info("Task {} retrieved successfully for project {} by user {}", taskId, projectId, currentUserId);
        return TaskResponseDto.fromEntity(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksForProject(UUID projectId, UUID currentUserId, Pageable pageable) {
        log.info("User {} listing tasks for project {} with pageable: {}", currentUserId, projectId, pageable);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        Page<ProjectTask> taskPage = projectTaskRepository.findByProjectId(projectId, pageable);
        log.info("Found {} tasks for project {} for user {}", taskPage.getTotalElements(), projectId, currentUserId);
        return taskPage.map(TaskResponseDto::fromEntity);
    }

    @Override
    @Transactional
    public TaskResponseDto updateTask(UUID projectId, UUID taskId, TaskUpdateDto taskDto, UUID currentUserId) {
        log.info("User {} updating task {} for project {}", currentUserId, taskId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found with ID: {} during update by user {} for project {}", taskId, currentUserId, projectId);
                    return new TaskNotFoundException("Task not found with ID: " + taskId);
                });

        if (!task.getProject().getId().equals(projectId)) {
            log.error("Unauthorized attempt by user {} to update task {} not belonging to project {}", currentUserId, taskId, projectId);
            throw new UnauthorizedOperationException("Task does not belong to the specified project.");
        }

        if (taskDto.getTitle() != null) task.setTitle(taskDto.getTitle());
        if (taskDto.getDescription() != null) task.setDescription(taskDto.getDescription());
        if (taskDto.getDueDate() != null) task.setDueDate(taskDto.getDueDate());
        if (taskDto.getStatus() != null) task.setStatus(taskDto.getStatus());

        if (taskDto.getAssignedUserId() != null) {
            log.debug("Re-assigning task {} to user {} for project {}", taskId, taskDto.getAssignedUserId(), projectId);
            projectService.verifyUserIsProjectMember(projectId, taskDto.getAssignedUserId());
            SimpleUser assignedUser = simpleUserService.findUserById(taskDto.getAssignedUserId());
            task.setAssignedUser(assignedUser);
        } else if (taskDto.getAssignedUserId() == null && task.getAssignedUser() != null) {
            log.debug("Un-assigning user from task {} for project {}", taskId, projectId);
            task.setAssignedUser(null);
        }


        ProjectTask updatedTask = projectTaskRepository.save(task);
        log.info("Task {} updated successfully for project {} by user {}", taskId, projectId, currentUserId);
        return TaskResponseDto.fromEntity(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(UUID projectId, UUID taskId, UUID currentUserId) {
        log.info("User {} deleting task {} from project {}", currentUserId, taskId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found with ID: {} during deletion by user {} for project {}", taskId, currentUserId, projectId);
                    return new TaskNotFoundException("Task not found with ID: " + taskId);
                });
        if (!task.getProject().getId().equals(projectId)) {
            log.error("Unauthorized attempt by user {} to delete task {} not belonging to project {}", currentUserId, taskId, projectId);
            throw new UnauthorizedOperationException("Task does not belong to the specified project.");
        }
        projectTaskRepository.delete(task);
        log.info("Task {} deleted successfully from project {} by user {}", taskId, projectId, currentUserId);
    }

    @Override
    @Transactional
    public TaskResponseDto assignUserToTask(UUID projectId, UUID taskId, UUID assigneeUserId, UUID currentUserId) {
        log.info("User {} assigning user {} to task {} for project {}", currentUserId, assigneeUserId, taskId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        projectService.verifyUserIsProjectMember(projectId, assigneeUserId);

        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found: {} for project {} when assigning user {} by user {}", taskId, projectId, assigneeUserId, currentUserId);
                    return new TaskNotFoundException("Task not found with ID: " + taskId);
                });
        if (!task.getProject().getId().equals(projectId)) {
            log.error("Unauthorized attempt by user {} to assign user {} to task {} not belonging to project {}", currentUserId, assigneeUserId, taskId, projectId);
            throw new UnauthorizedOperationException("Task does not belong to the specified project.");
        }

        SimpleUser userToAssign = simpleUserService.findUserById(assigneeUserId);
        task.setAssignedUser(userToAssign);
        ProjectTask updatedTask = projectTaskRepository.save(task);
        log.info("User {} successfully assigned to task {} for project {} by user {}", assigneeUserId, taskId, projectId, currentUserId);
        return TaskResponseDto.fromEntity(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponseDto removeUserFromTask(UUID projectId, UUID taskId, UUID currentUserId) {
        log.info("User {} removing assigned user from task {} for project {}", currentUserId, taskId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found: {} for project {} when removing user by user {}", taskId, projectId, currentUserId);
                    return new TaskNotFoundException("Task not found with ID: " + taskId);
                });
        if (!task.getProject().getId().equals(projectId)) {
            log.error("Unauthorized attempt by user {} to remove user from task {} not belonging to project {}", currentUserId, taskId, projectId);
            throw new UnauthorizedOperationException("Task does not belong to the specified project.");
        }
        if (task.getAssignedUser() == null) {
            log.info("Task {} on project {} had no assigned user to remove for user {}. Returning current state.", taskId, projectId, currentUserId);
        } else {
            log.info("Removing user {} from task {} on project {} by user {}", task.getAssignedUser().getId(), taskId, projectId, currentUserId);
            task.setAssignedUser(null);
        }
        ProjectTask updatedTask = projectTaskRepository.save(task);
        log.info("User assignment removed from task {} for project {} by user {}", taskId, projectId, currentUserId);
        return TaskResponseDto.fromEntity(updatedTask);
    }
}
