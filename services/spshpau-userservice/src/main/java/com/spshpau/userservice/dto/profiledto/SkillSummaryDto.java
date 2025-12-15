package com.spshpau.userservice.dto.profiledto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * A summary DTO for Skill information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillSummaryDto {
    private UUID id;
    private String name;
}
