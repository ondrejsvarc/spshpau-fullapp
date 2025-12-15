package com.spshpau.projectservice.dto;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.Date;

@Data
public class ExpenseUpdateDto {
    @Positive(message = "Amount must be positive")
    private Float amount;

    @PastOrPresent(message = "Expense date cannot be in the future")
    private Date date;

    private String comment;
}
