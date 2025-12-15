package com.spshpau.projectservice.dto;

import com.spshpau.projectservice.model.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
public class TaskCreateDto {
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(max = 5000, message = "Description can be up to 5000 characters")
    private String description;

    private Timestamp dueDate;

    @NotNull(message = "Status cannot be null")
    private TaskStatus status;

    private UUID assignedUserId;
}
