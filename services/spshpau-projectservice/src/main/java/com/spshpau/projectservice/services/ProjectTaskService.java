package com.spshpau.projectservice.services;

import com.spshpau.projectservice.dto.TaskCreateDto;
import com.spshpau.projectservice.dto.TaskResponseDto;
import com.spshpau.projectservice.dto.TaskUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectTaskService {
    TaskResponseDto createTask(UUID projectId, TaskCreateDto taskDto, UUID currentUserId);
    TaskResponseDto getTaskById(UUID projectId, UUID taskId, UUID currentUserId);
    Page<TaskResponseDto> getTasksForProject(UUID projectId, UUID currentUserId, Pageable pageable);
    TaskResponseDto updateTask(UUID projectId, UUID taskId, TaskUpdateDto taskDto, UUID currentUserId);
    void deleteTask(UUID projectId, UUID taskId, UUID currentUserId);
    TaskResponseDto assignUserToTask(UUID projectId, UUID taskId, UUID assigneeUserId, UUID currentUserId);
    TaskResponseDto removeUserFromTask(UUID projectId, UUID taskId, UUID currentUserId);
}
