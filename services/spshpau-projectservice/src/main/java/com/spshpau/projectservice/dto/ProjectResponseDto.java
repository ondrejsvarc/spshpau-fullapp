package com.spshpau.projectservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.spshpau.projectservice.model.Project;


@Data
@Builder
public class ProjectResponseDto {
    private UUID id;
    private String title;
    private String description;
    private UserSummaryDto owner;
    private Set<UserSummaryDto> collaborators;

    public static ProjectResponseDto fromEntity(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .owner(UserSummaryDto.fromEntity(project.getOwner()))
                .collaborators(project.getCollaborators().stream()
                        .map(UserSummaryDto::fromEntity)
                        .collect(Collectors.toSet()))
                .build();
    }
}
