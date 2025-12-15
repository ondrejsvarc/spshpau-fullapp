package com.spshpau.projectservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemainingBudgetDto {
    private Float totalAmount;
    private Float spentAmount;
    private Float remainingAmount;
    private String currency;
}
