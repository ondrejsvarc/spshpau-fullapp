package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.profiledto.SkillDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

public interface SkillController {
    /**
     * Creates a new Skill.
     * This endpoint is restricted to users with administrative privileges.
     *
     * @param skillDto DTO containing the data for the new skill (e.g., name).
     * @return ResponseEntity containing the created {@link SkillSummaryDto} with a 201 Created status,
     * or an error status if creation fails (e.g., duplicate name).
     * Example Success Response (201 Created):
     * <pre>{@code
     * {
     * "id": "new-skill-uuid",
     * "name": "Mixing"
     * }
     * }</pre>
     */
    ResponseEntity<SkillSummaryDto> addSkill(@RequestBody SkillDto skillDto);

    /**
     * Deletes a Skill by its unique ID.
     * This endpoint is restricted to users with administrative privileges.
     *
     * @param skillId The UUID of the Skill to delete.
     * @return ResponseEntity with a 204 No Content status if successful,
     * or an error status if the skill is not found or cannot be deleted.
     * Example Success Response (204 No Content):
     * (Empty Body)
     */
    ResponseEntity<Void> deleteSkill(@PathVariable UUID skillId);

    /**
     * Retrieves all Skills with support for pagination.
     *
     * @param pageable Pagination information (e.g., page number, size, sort order).
     * @return ResponseEntity containing a Page of {@link SkillSummaryDto},
     * or an error status if retrieval fails.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {"id": "skill-uuid-1", "name": "Bass Guitar"},
     * {"id": "skill-uuid-2", "name": "Drums"}
     * ],
     * "pageable": {
     * "sort": {"sorted": true, "unsorted": false, "empty": false},
     * "offset": 0,
     * "pageNumber": 0,
     * "pageSize": 20,
     * "paged": true,
     * "unpaged": false
     * },
     * "totalPages": 3,
     * "totalElements": 55,
     * "last": false,
     * "size": 20,
     * "number": 0,
     * "sort": {"sorted": true, "unsorted": false, "empty": false},
     * "numberOfElements": 20,
     * "first": true,
     * "empty": false
     * }
     * }</pre>
     */
    ResponseEntity<Page<SkillSummaryDto>> getAllSkills(Pageable pageable);
}
