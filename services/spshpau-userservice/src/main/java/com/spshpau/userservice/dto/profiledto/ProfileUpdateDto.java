package com.spshpau.userservice.dto.profiledto;

import com.spshpau.userservice.model.enums.ExperienceLevel;
import lombok.Data;

@Data
public class ProfileUpdateDto {
    private Boolean availability;
    private String bio;
    private ExperienceLevel experienceLevel;
}
