package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.SkillDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SkillService {
    /**
     * Creates a new skill based on the provided DTO.
     * The skill name must be unique (case-insensitive).
     *
     * @param skillDto A {@link SkillDto} containing the details of the skill to create (e.g., name). Must not be null.
     * @return A {@link SkillSummaryDto} representing the newly created skill.
     * @throws com.spshpau.userservice.services.exceptions.DuplicateException if a skill with the same name already exists.
     */
    SkillSummaryDto createSkill(SkillDto skillDto);

    /**
     * Deletes a skill identified by its unique ID.
     * If the skill is associated with any profiles, the deletion might be restricted
     * or might cascade, depending on repository and database constraints.
     *
     * @param skillId The unique identifier of the skill to delete.
     * @throws com.spshpau.userservice.services.exceptions.SkillNotFoundException if no skill with the given ID is found.
     * @throws org.springframework.dao.DataIntegrityViolationException if the skill cannot be deleted due to existing references
     * (e.g., if it's still linked to artist profiles and cascading delete is not configured).
     */
    void deleteSkill(UUID skillId);

    /**
     * Retrieves a paginated list of all available skills.
     *
     * @param pageable Pagination information (e.g., page number, size, sort order). Must not be null.
     * @return A {@link Page} of {@link SkillSummaryDto} objects. Returns an empty page if no skills are found.
     */
    Page<SkillSummaryDto> getAllSkills(Pageable pageable);
}
