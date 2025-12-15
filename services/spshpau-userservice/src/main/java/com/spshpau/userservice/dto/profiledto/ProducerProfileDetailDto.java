package com.spshpau.userservice.dto.profiledto;

import com.spshpau.userservice.model.enums.ExperienceLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for detailed ProducerProfile information, including associated genres.
 */
@Data
@NoArgsConstructor
public class ProducerProfileDetailDto {
    private UUID id;
    private boolean availability;
    private String bio;
    private ExperienceLevel experienceLevel;
    private Set<GenreSummaryDto> genres;
}
