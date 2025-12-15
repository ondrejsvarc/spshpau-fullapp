package com.spshpau.projectservice.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class MilestoneUpdateDto {
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(max = 5000, message = "Description can be up to 5000 characters")
    private String description;

    private Timestamp dueDate;
}
