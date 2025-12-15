package com.spshpau.projectservice.controller;

import com.spshpau.projectservice.dto.MilestoneCreateDto;
import com.spshpau.projectservice.dto.MilestoneResponseDto;
import com.spshpau.projectservice.dto.MilestoneUpdateDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface ProjectMilestoneController {

    /**
     * Creates a new milestone for a specified project.
     *
     * @param projectId The ID of the project for which to create the milestone.
     * @param milestoneDto The milestone creation data.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the created MilestoneResponseDto and HTTP status.
     * Example Response (201 Created):
     * <pre>{@code
     * {
     * "id": "m1n2o3p4-q5r6-s7t8-u9v0-w1x2y3z4a5b6",
     * "title": "Phase 1 Completion",
     * "description": "All tasks for phase 1 are completed.",
     * "dueDate": "2024-06-30T23:59:59.000+00:00",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * }
     * }</pre>
     */
    ResponseEntity<MilestoneResponseDto> createMilestone(@PathVariable UUID projectId,
                                                         @Valid @RequestBody MilestoneCreateDto milestoneDto,
                                                         Jwt jwt);

    /**
     * Retrieves a specific milestone by its ID for a given project.
     *
     * @param projectId The ID of the project to which the milestone belongs.
     * @param milestoneId The ID of the milestone to retrieve.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the MilestoneResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "id": "m1n2o3p4-q5r6-s7t8-u9v0-w1x2y3z4a5b6",
     * "title": "Phase 1 Completion",
     * "description": "All tasks for phase 1 are completed.",
     * "dueDate": "2024-06-30T23:59:59.000+00:00",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * }
     * }</pre>
     */
    ResponseEntity<MilestoneResponseDto> getMilestoneById(@PathVariable UUID projectId,
                                                          @PathVariable UUID milestoneId,
                                                          Jwt jwt);

    /**
     * Retrieves a paginated list of milestones for a specified project.
     *
     * @param projectId The ID of the project for which to retrieve milestones.
     * @param jwt The JWT token for authentication and authorization.
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a Page of MilestoneResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {
     * "id": "m1n2o3p4-q5r6-s7t8-u9v0-w1x2y3z4a5b6",
     * "title": "Phase 1 Completion",
     * "description": "All tasks for phase 1 are completed.",
     * "dueDate": "2024-06-30T23:59:59.000+00:00",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * }
     * ],
     * "pageable": {"offset": 0, "pageSize": 20, ...},
     * "totalElements": 1,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<MilestoneResponseDto>> getMilestonesForProject(@PathVariable UUID projectId,
                                                                       Jwt jwt,
                                                                       Pageable pageable);

    /**
     * Updates an existing milestone for a specified project.
     *
     * @param projectId The ID of the project to which the milestone belongs.
     * @param milestoneId The ID of the milestone to update.
     * @param milestoneDto The milestone update data.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the updated MilestoneResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "id": "m1n2o3p4-q5r6-s7t8-u9v0-w1x2y3z4a5b6",
     * "title": "Phase 1 Completion (Updated)",
     * "description": "All tasks and documentation for phase 1 are completed.",
     * "dueDate": "2024-07-05T23:59:59.000+00:00",
     * "projectId": "c1d2e3f4-g5h6-7890-1234-567890abcdef"
     * }
     * }</pre>
     */
    ResponseEntity<MilestoneResponseDto> updateMilestone(@PathVariable UUID projectId,
                                                         @PathVariable UUID milestoneId,
                                                         @Valid @RequestBody MilestoneUpdateDto milestoneDto,
                                                         Jwt jwt);

    /**
     * Deletes a milestone from a specified project.
     *
     * @param projectId The ID of the project from which to delete the milestone.
     * @param milestoneId The ID of the milestone to delete.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity with no content and HTTP status 204.
     */
    ResponseEntity<Void> deleteMilestone(@PathVariable UUID projectId,
                                         @PathVariable UUID milestoneId,
                                         Jwt jwt);
}