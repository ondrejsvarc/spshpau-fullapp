package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.SkillDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import com.spshpau.userservice.model.Skill;
import com.spshpau.userservice.repositories.SkillRepository;
import com.spshpau.userservice.services.exceptions.DuplicateException;
import com.spshpau.userservice.services.exceptions.SkillNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillServiceImpl skillService;

    private SkillDto skillDto;
    private Skill skill;
    private UUID skillId;

    @BeforeEach
    void setUp() {
        skillId = UUID.randomUUID();

        skillDto = new SkillDto();
        skillDto.setName("Guitar");

        skill = new Skill("Guitar");
        skill.setId(skillId);
    }

    // --- Tests for createSkill ---
    @Test
    void createSkill_whenNameIsUnique_shouldCreateAndReturnSkillSummaryDto() {
        when(skillRepository.findByNameIgnoreCase(skillDto.getName())).thenReturn(Optional.empty());
        ArgumentCaptor<Skill> skillArgumentCaptor = ArgumentCaptor.forClass(Skill.class);
        when(skillRepository.save(skillArgumentCaptor.capture())).thenReturn(skill);

        SkillSummaryDto result = skillService.createSkill(skillDto);

        assertNotNull(result);
        assertEquals(skill.getId(), result.getId());
        assertEquals(skillDto.getName(), result.getName());

        verify(skillRepository).findByNameIgnoreCase(skillDto.getName());
        verify(skillRepository).save(any(Skill.class));

        Skill capturedSkill = skillArgumentCaptor.getValue();
        assertEquals(skillDto.getName(), capturedSkill.getName());
        assertNull(capturedSkill.getId());
    }

    @Test
    void createSkill_whenNameIsNotUnique_shouldThrowDuplicateException() {
        Skill existingSkill = new Skill("Guitar");
        existingSkill.setId(UUID.randomUUID());
        when(skillRepository.findByNameIgnoreCase(skillDto.getName())).thenReturn(Optional.of(existingSkill));

        DuplicateException exception = assertThrows(DuplicateException.class, () -> {
            skillService.createSkill(skillDto);
        });

        assertEquals("Skill with name '" + skillDto.getName() + "' already exists.", exception.getMessage());
        verify(skillRepository).findByNameIgnoreCase(skillDto.getName());
        verify(skillRepository, never()).save(any(Skill.class));
    }

    // --- Tests for deleteSkill ---
    @Test
    void deleteSkill_whenSkillExists_shouldDeleteSkill() {
        when(skillRepository.existsById(skillId)).thenReturn(true);
        doNothing().when(skillRepository).deleteById(skillId);

        assertDoesNotThrow(() -> skillService.deleteSkill(skillId));

        verify(skillRepository).existsById(skillId);
        verify(skillRepository).deleteById(skillId);
    }

    @Test
    void deleteSkill_whenSkillDoesNotExist_shouldThrowSkillNotFoundException() {
        when(skillRepository.existsById(skillId)).thenReturn(false);

        SkillNotFoundException exception = assertThrows(SkillNotFoundException.class, () -> {
            skillService.deleteSkill(skillId);
        });

        assertEquals("Skill not found with ID: " + skillId, exception.getMessage());
        verify(skillRepository).existsById(skillId);
        verify(skillRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deleteSkill_whenRepositoryThrowsUnexpectedException_shouldRethrow() {
        when(skillRepository.existsById(skillId)).thenReturn(true);
        DataIntegrityViolationException dbException = new DataIntegrityViolationException("DB constraint issue");
        doThrow(dbException).when(skillRepository).deleteById(skillId);

        DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class, () -> {
            skillService.deleteSkill(skillId);
        });
        assertEquals(dbException, thrown);

        verify(skillRepository).existsById(skillId);
        verify(skillRepository).deleteById(skillId);
    }


    // --- Tests for getAllSkills ---
    @Test
    void getAllSkills_whenSkillsExist_shouldReturnPageOfSkillSummaryDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Skill skill1 = new Skill("Vocals"); skill1.setId(UUID.randomUUID());
        Skill skill2 = new Skill("Drums"); skill2.setId(UUID.randomUUID());
        List<Skill> skillList = List.of(skill1, skill2);
        Page<Skill> skillPage = new PageImpl<>(skillList, pageable, skillList.size());

        when(skillRepository.findAll(pageable)).thenReturn(skillPage);

        Page<SkillSummaryDto> result = skillService.getAllSkills(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(skill1.getName(), result.getContent().get(0).getName());
        assertEquals(skill1.getId(), result.getContent().get(0).getId());
        assertEquals(skill2.getName(), result.getContent().get(1).getName());

        verify(skillRepository).findAll(pageable);
    }

    @Test
    void getAllSkills_whenNoSkillsExist_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Skill> emptySkillPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(skillRepository.findAll(pageable)).thenReturn(emptySkillPage);

        Page<SkillSummaryDto> result = skillService.getAllSkills(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(skillRepository).findAll(pageable);
    }
}