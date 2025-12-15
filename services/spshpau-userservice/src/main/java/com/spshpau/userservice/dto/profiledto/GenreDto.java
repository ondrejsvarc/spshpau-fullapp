package com.spshpau.userservice.dto.profiledto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenreDto {
    @NotBlank(message = "Genre name cannot be blank")
    @Size(max = 50, message = "Genre name cannot exceed 50 characters")
    private String name;
}