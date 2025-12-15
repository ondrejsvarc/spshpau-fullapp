package com.spshpau.projectservice.dto;

import com.spshpau.projectservice.model.ProjectBudget;
import lombok.Builder;
import lombok.Data;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class BudgetResponseDto {
    private UUID projectId; // Budget ID is same as Project ID
    private String currency;
    private Float totalAmount;
    private Float spentAmount;
    private Float remainingAmount;
    private Set<ExpenseResponseDto> expenses;

    public static BudgetResponseDto fromEntity(ProjectBudget budget, Float spentAmount) {
        if (budget == null) return null;
        Float total = budget.getTotalAmount();
        Float spent = spentAmount != null ? spentAmount : 0f;

        return BudgetResponseDto.builder()
                .projectId(budget.getId())
                .currency(budget.getCurrency())
                .totalAmount(total)
                .spentAmount(spent)
                .remainingAmount(total - spent)
                .expenses(budget.getExpenses().stream()
                        .map(ExpenseResponseDto::fromEntity)
                        .collect(Collectors.toSet()))
                .build();
    }
}
