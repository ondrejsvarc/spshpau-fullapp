package com.spshpau.projectservice.dto;

import com.spshpau.projectservice.model.BudgetExpense;
import lombok.Builder;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class ExpenseResponseDto {
    private UUID id;
    private Float amount;
    private Date date;
    private String comment;
    private UUID budgetId; // This is the project ID

    public static ExpenseResponseDto fromEntity(BudgetExpense expense) {
        if (expense == null) return null;
        return ExpenseResponseDto.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .comment(expense.getComment())
                .budgetId(expense.getBudget() != null ? expense.getBudget().getId() : null)
                .build();
    }
}
