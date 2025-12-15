package com.spshpau.projectservice.dto;

import com.spshpau.projectservice.dto.enums.ExperienceLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProducerProfileSummaryDto {
    private boolean availability;
    private ExperienceLevel experienceLevel;
}
