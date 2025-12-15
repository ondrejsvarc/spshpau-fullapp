package com.spshpau.projectservice.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BudgetUpdateDto {
    private String currency;

    @Positive(message = "Total amount must be positive")
    private Float totalAmount;
}
