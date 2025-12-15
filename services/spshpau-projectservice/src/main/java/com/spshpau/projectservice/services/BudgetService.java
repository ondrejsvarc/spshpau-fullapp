package com.spshpau.projectservice.services;

import com.spshpau.projectservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BudgetService {
    BudgetResponseDto createProjectBudget(UUID projectId, BudgetCreateDto budgetDto, UUID currentUserId);
    BudgetResponseDto getProjectBudget(UUID projectId, UUID currentUserId);
    BudgetResponseDto updateProjectBudget(UUID projectId, BudgetUpdateDto budgetDto, UUID currentUserId);
    void deleteProjectBudget(UUID projectId, UUID currentUserId);
    RemainingBudgetDto getRemainingProjectBudget(UUID projectId, UUID currentUserId);

    ExpenseResponseDto addExpenseToBudget(UUID projectId, ExpenseCreateDto expenseDto, UUID currentUserId);
    ExpenseResponseDto getExpenseById(UUID projectId, UUID expenseId, UUID currentUserId);
    Page<ExpenseResponseDto> getExpensesForProjectBudget(UUID projectId, UUID currentUserId, Pageable pageable);
    ExpenseResponseDto updateExpense(UUID projectId, UUID expenseId, ExpenseUpdateDto expenseDto, UUID currentUserId);
    void removeExpense(UUID projectId, UUID expenseId, UUID currentUserId);
}
