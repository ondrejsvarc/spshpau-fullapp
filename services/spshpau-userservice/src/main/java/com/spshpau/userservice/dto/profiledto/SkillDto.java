package com.spshpau.userservice.dto.profiledto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SkillDto {
    @NotBlank(message = "Skill name cannot be blank")
    @Size(max = 50, message = "Skill name cannot exceed 50 characters")
    private String name;
}
