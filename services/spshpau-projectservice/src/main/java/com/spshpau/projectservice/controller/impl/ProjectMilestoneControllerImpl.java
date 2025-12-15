package com.spshpau.projectservice.controller.impl;

import com.spshpau.projectservice.controller.ProjectMilestoneController;
import com.spshpau.projectservice.dto.MilestoneCreateDto;
import com.spshpau.projectservice.dto.MilestoneResponseDto;
import com.spshpau.projectservice.dto.MilestoneUpdateDto;
import com.spshpau.projectservice.services.ProjectMilestoneService;
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
@RequestMapping("/api/v1/projects/{projectId}/milestones")
@RequiredArgsConstructor
public class ProjectMilestoneControllerImpl implements ProjectMilestoneController {

    private final ProjectMilestoneService projectMilestoneService;

    private UUID getUserIdFromJwt(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @Override
    @PostMapping
    public ResponseEntity<MilestoneResponseDto> createMilestone(@PathVariable UUID projectId,
                                                                @Valid @RequestBody MilestoneCreateDto milestoneDto,
                                                                @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        MilestoneResponseDto createdMilestone = projectMilestoneService.createMilestone(projectId, milestoneDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMilestone);
    }

    @Override
    @GetMapping("/{milestoneId}")
    public ResponseEntity<MilestoneResponseDto> getMilestoneById(@PathVariable UUID projectId,
                                                                 @PathVariable UUID milestoneId,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        MilestoneResponseDto milestone = projectMilestoneService.getMilestoneById(projectId, milestoneId, currentUserId);
        return ResponseEntity.ok(milestone);
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<MilestoneResponseDto>> getMilestonesForProject(@PathVariable UUID projectId,
                                                                              @AuthenticationPrincipal Jwt jwt,
                                                                              @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        Page<MilestoneResponseDto> milestones = projectMilestoneService.getMilestonesForProject(projectId, currentUserId, pageable);
        return ResponseEntity.ok(milestones);
    }

    @Override
    @PutMapping("/{milestoneId}")
    public ResponseEntity<MilestoneResponseDto> updateMilestone(@PathVariable UUID projectId,
                                                                @PathVariable UUID milestoneId,
                                                                @Valid @RequestBody MilestoneUpdateDto milestoneDto,
                                                                @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        MilestoneResponseDto updatedMilestone = projectMilestoneService.updateMilestone(projectId, milestoneId, milestoneDto, currentUserId);
        return ResponseEntity.ok(updatedMilestone);
    }

    @Override
    @DeleteMapping("/{milestoneId}")
    public ResponseEntity<Void> deleteMilestone(@PathVariable UUID projectId,
                                                @PathVariable UUID milestoneId,
                                                @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        projectMilestoneService.deleteMilestone(projectId, milestoneId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
