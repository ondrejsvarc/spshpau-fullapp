package com.spshpau.userservice.dto.profiledto;

import com.spshpau.userservice.model.enums.ExperienceLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProducerProfileSummaryDto {
    private boolean availability;
    private ExperienceLevel experienceLevel;
}
