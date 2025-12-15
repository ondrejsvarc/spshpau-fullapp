package com.spshpau.projectservice.controller.impl;

import com.spshpau.projectservice.controller.ProjectTaskController;
import com.spshpau.projectservice.dto.TaskCreateDto;
import com.spshpau.projectservice.dto.TaskResponseDto;
import com.spshpau.projectservice.dto.TaskUpdateDto;
import com.spshpau.projectservice.services.ProjectTaskService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class ProjectTaskControllerImpl implements ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    private UUID getUserIdFromJwt(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @Override
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@PathVariable UUID projectId,
                                                      @Valid @RequestBody TaskCreateDto taskDto,
                                                      @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        TaskResponseDto createdTask = projectTaskService.createTask(projectId, taskDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @Override
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable UUID projectId,
                                                       @PathVariable UUID taskId,
                                                       @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        TaskResponseDto task = projectTaskService.getTaskById(projectId, taskId, currentUserId);
        return ResponseEntity.ok(task);
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> getTasksForProject(@PathVariable UUID projectId,
                                                                    @AuthenticationPrincipal Jwt jwt,
                                                                    @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        Page<TaskResponseDto> tasks = projectTaskService.getTasksForProject(projectId, currentUserId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @Override
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable UUID projectId,
                                                      @PathVariable UUID taskId,
                                                      @Valid @RequestBody TaskUpdateDto taskDto,
                                                      @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        TaskResponseDto updatedTask = projectTaskService.updateTask(projectId, taskId, taskDto, currentUserId);
        return ResponseEntity.ok(updatedTask);
    }

    @Override
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID projectId,
                                           @PathVariable UUID taskId,
                                           @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        projectTaskService.deleteTask(projectId, taskId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{taskId}/assign/{assigneeUserId}")
    public ResponseEntity<TaskResponseDto> assignUserToTask(@PathVariable UUID projectId,
                                                            @PathVariable UUID taskId,
                                                            @PathVariable UUID assigneeUserId,
                                                            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        TaskResponseDto task = projectTaskService.assignUserToTask(projectId, taskId, assigneeUserId, currentUserId);
        return ResponseEntity.ok(task);
    }

    @Override
    @DeleteMapping("/{taskId}/unassign")
    public ResponseEntity<TaskResponseDto> removeUserFromTask(@PathVariable UUID projectId,
                                                              @PathVariable UUID taskId,
                                                              @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        TaskResponseDto task = projectTaskService.removeUserFromTask(projectId, taskId, currentUserId);
        return ResponseEntity.ok(task);
    }
}
