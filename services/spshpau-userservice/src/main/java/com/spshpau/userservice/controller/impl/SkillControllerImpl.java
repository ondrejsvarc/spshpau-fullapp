package com.spshpau.userservice.controller.impl;

import com.spshpau.userservice.controller.SkillController;
import com.spshpau.userservice.dto.profiledto.SkillDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import com.spshpau.userservice.model.Skill;
import com.spshpau.userservice.services.SkillService;
import com.spshpau.userservice.services.exceptions.DuplicateException;
import com.spshpau.userservice.services.exceptions.SkillNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillControllerImpl implements SkillController {
    private final SkillService skillService;

    @Override
    @PostMapping("/add")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<SkillSummaryDto> addSkill(@Valid @RequestBody SkillDto skillDto) {
        try {
            SkillSummaryDto createdSkill = skillService.createSkill(skillDto);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(createdSkill.getId())
                    .toUri();
            return ResponseEntity.created(location).body(createdSkill);
        } catch (DuplicateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating skill", ex);
        }
    }

    @Override
    @DeleteMapping("/delete/{skillId}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Void> deleteSkill(@PathVariable UUID skillId) {
        try {
            skillService.deleteSkill(skillId);
            return ResponseEntity.noContent().build();
        } catch (SkillNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found with ID: " + skillId, ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting skill", ex);
        }
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<SkillSummaryDto>> getAllSkills(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        try {
            Page<SkillSummaryDto> skillPage = skillService.getAllSkills(pageable);
            return ResponseEntity.ok(skillPage);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving skills", ex);
        }
    }
}
