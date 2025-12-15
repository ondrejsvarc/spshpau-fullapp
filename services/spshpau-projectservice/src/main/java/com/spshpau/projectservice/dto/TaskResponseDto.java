package com.spshpau.projectservice.dto;

import com.spshpau.projectservice.model.ProjectTask;
import com.spshpau.projectservice.model.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
public class TaskResponseDto {
    private UUID id;
    private String title;
    private String description;
    private Timestamp createdAt;
    private Timestamp dueDate;
    private TaskStatus status;
    private UUID projectId;
    private UserSummaryDto assignedUser;

    public static TaskResponseDto fromEntity(ProjectTask task) {
        if (task == null) {
            return null;
        }
        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .createdAt(task.getCreatedAt())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .assignedUser(task.getAssignedUser() != null ? UserSummaryDto.fromEntity(task.getAssignedUser()) : null)
                .build();
    }
}
