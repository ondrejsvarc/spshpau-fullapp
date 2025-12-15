package com.spshpau.projectservice.dto;

import com.spshpau.projectservice.model.ProjectMilestone;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
public class MilestoneResponseDto {
    private UUID id;
    private String title;
    private String description;
    private Timestamp dueDate;
    private UUID projectId;

    public static MilestoneResponseDto fromEntity(ProjectMilestone milestone) {
        if (milestone == null) {
            return null;
        }
        return MilestoneResponseDto.builder()
                .id(milestone.getId())
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .dueDate(milestone.getDueDate())
                .projectId(milestone.getProject() != null ? milestone.getProject().getId() : null)
                .build();
    }
}
