package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.SkillDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import com.spshpau.userservice.model.Skill;
import com.spshpau.userservice.repositories.SkillRepository;
import com.spshpau.userservice.services.SkillService;
import com.spshpau.userservice.services.exceptions.DuplicateException;
import com.spshpau.userservice.services.exceptions.SkillNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    private SkillSummaryDto mapEntityToSummaryDto(Skill entity) {
        if (entity == null) return null;
        return new SkillSummaryDto(entity.getId(), entity.getName());
    }

    @Override
    @Transactional
    public SkillSummaryDto createSkill(SkillDto skillDto) {
        log.debug("Attempting to create skill with name: {}", skillDto.getName());
        Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(skillDto.getName());
        if (existingSkill.isPresent()) {
            log.warn("Skill creation failed: Name '{}' already exists.", skillDto.getName());
            throw new DuplicateException("Skill with name '" + skillDto.getName() + "' already exists.");
        }

        Skill newSkill = new Skill(skillDto.getName());
        Skill savedSkill = skillRepository.save(newSkill);
        log.info("Successfully created skill '{}' with ID: {}", savedSkill.getName(), savedSkill.getId());
        return mapEntityToSummaryDto(savedSkill);
    }

    @Override
    @Transactional
    public void deleteSkill(UUID skillId) {
        log.debug("Attempting to delete skill with ID: {}", skillId);
        if (!skillRepository.existsById(skillId)) {
            log.warn("Skill deletion failed: Skill not found with ID: {}", skillId);
            throw new SkillNotFoundException("Skill not found with ID: " + skillId);
        }
        try {
            skillRepository.deleteById(skillId);
            log.info("Successfully deleted skill with ID: {}", skillId);
        } catch (Exception e) {
            log.error("An unexpected error occurred while deleting skill with ID: {}", skillId, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkillSummaryDto> getAllSkills(Pageable pageable) {
        log.debug("Fetching skills with pageable: {}", pageable);
        Page<Skill> skillPage = skillRepository.findAll(pageable);
        return skillPage.map(this::mapEntityToSummaryDto);
    }
}
