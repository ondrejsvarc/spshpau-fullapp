package com.spshpau.projectservice.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class ProjectUpdateDto {
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 5000, message = "Description can be up to 5000 characters")
    private String description;
}
