package com.spshpau.userservice.dto.profiledto;

import com.spshpau.userservice.model.enums.ExperienceLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for detailed ArtistProfile information, including associated genres and skills.
 */
@Data
@NoArgsConstructor
public class ArtistProfileDetailDto {
    private UUID id;
    private boolean availability;
    private String bio;
    private ExperienceLevel experienceLevel;
    private Set<GenreSummaryDto> genres;
    private Set<SkillSummaryDto> skills;
}
