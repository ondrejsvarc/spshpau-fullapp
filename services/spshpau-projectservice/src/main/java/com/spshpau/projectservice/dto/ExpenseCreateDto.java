package com.spshpau.projectservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.Date;

@Data
public class ExpenseCreateDto {
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private Float amount;

    @NotNull(message = "Date cannot be null")
    @PastOrPresent(message = "Expense date cannot be in the future")
    private Date date;

    @NotBlank(message = "Comment cannot be blank")
    private String comment;
}
