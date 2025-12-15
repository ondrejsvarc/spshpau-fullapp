package com.spshpau.projectservice.services;

import com.spshpau.projectservice.dto.MilestoneCreateDto;
import com.spshpau.projectservice.dto.MilestoneResponseDto;
import com.spshpau.projectservice.dto.MilestoneUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectMilestoneService {
    MilestoneResponseDto createMilestone(UUID projectId, MilestoneCreateDto milestoneDto, UUID currentUserId);
    MilestoneResponseDto getMilestoneById(UUID projectId, UUID milestoneId, UUID currentUserId);
    Page<MilestoneResponseDto> getMilestonesForProject(UUID projectId, UUID currentUserId, Pageable pageable);
    MilestoneResponseDto updateMilestone(UUID projectId, UUID milestoneId, MilestoneUpdateDto milestoneDto, UUID currentUserId);
    void deleteMilestone(UUID projectId, UUID milestoneId, UUID currentUserId);
}
