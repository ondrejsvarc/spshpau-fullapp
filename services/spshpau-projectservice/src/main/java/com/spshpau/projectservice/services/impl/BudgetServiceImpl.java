package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.*;
import com.spshpau.projectservice.services.exceptions.*;
import com.spshpau.projectservice.model.BudgetExpense;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.ProjectBudget;
import com.spshpau.projectservice.repositories.BudgetExpenseRepository;
import com.spshpau.projectservice.repositories.ProjectBudgetrepository;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.services.BudgetService;
import com.spshpau.projectservice.services.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {
    private final ProjectBudgetrepository projectBudgetRepository;
    private final BudgetExpenseRepository budgetExpenseRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;


    private float calculateSpentAmount(UUID budgetId) {
        log.debug("Calculating spent amount for budget ID: {}", budgetId);
        return budgetExpenseRepository.sumExpensesByBudgetId(budgetId).orElse(0f);
    }

    @Override
    @Transactional
    public BudgetResponseDto createProjectBudget(UUID projectId, BudgetCreateDto budgetDto, UUID currentUserId) {
        log.info("User {} attempting to create budget for project {}", currentUserId, projectId);
        if (!projectService.isUserOwnerOfProject(projectId, currentUserId)) {
            log.error("User {} is not owner of project {}. Budget creation denied.", currentUserId, projectId);
            throw new UnauthorizedOperationException("Only the project owner can create a budget.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

        if (project.getBudget() != null) {
            throw new BudgetAlreadyExistsException("Budget already exists for project ID: " + projectId);
        }

        ProjectBudget budget = new ProjectBudget();
        budget.setProject(project);
        budget.setCurrency(budgetDto.getCurrency());
        budget.setTotalAmount(budgetDto.getTotalAmount());

        project.setBudget(budget);

        Project savedProject = projectRepository.save(project);

        ProjectBudget persistedBudget = savedProject.getBudget();

        if (persistedBudget == null || persistedBudget.getId() == null) {
            throw new IllegalStateException("Budget was not persisted correctly or its ID was not set. Project ID: " + projectId);
        }

        return BudgetResponseDto.fromEntity(persistedBudget, 0f);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponseDto getProjectBudget(UUID projectId, UUID currentUserId) {
        log.info("User {} attempting to get budget for project {}", currentUserId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectBudget budget = projectBudgetRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Budget not found for project ID: {} by user {}", projectId, currentUserId);
                    return new BudgetNotFoundException("Budget not found for project ID: " + projectId);
                });
        float spentAmount = calculateSpentAmount(budget.getId());
        log.info("Budget retrieved for project {} by user {}. Spent amount: {}", projectId, currentUserId, spentAmount);
        return BudgetResponseDto.fromEntity(budget, spentAmount);
    }

    @Override
    @Transactional
    public BudgetResponseDto updateProjectBudget(UUID projectId, BudgetUpdateDto budgetDto, UUID currentUserId) {
        log.info("User {} attempting to update budget for project {}", currentUserId, projectId);
        if (!projectService.isUserOwnerOfProject(projectId, currentUserId)) {
            log.error("User {} is not owner of project {}. Budget update denied.", currentUserId, projectId);
            throw new UnauthorizedOperationException("Only the project owner can update the budget.");
        }
        ProjectBudget budget = projectBudgetRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Budget not found for project ID: {} during update by user {}. Cannot update.", projectId, currentUserId);
                    return new BudgetNotFoundException("Budget not found for project ID: " + projectId + ". Cannot update.");
                });

        boolean updated = false;
        if (budgetDto.getCurrency() != null && !budgetDto.getCurrency().equals(budget.getCurrency())) {
            log.debug("Updating currency for budget {} from {} to {}", projectId, budget.getCurrency(), budgetDto.getCurrency());
            budget.setCurrency(budgetDto.getCurrency());
            updated = true;
        }
        if (budgetDto.getTotalAmount() != null && !budgetDto.getTotalAmount().equals(budget.getTotalAmount())) {
            log.debug("Updating total amount for budget {} from {} to {}", projectId, budget.getTotalAmount(), budgetDto.getTotalAmount());
            budget.setTotalAmount(budgetDto.getTotalAmount());
            updated = true;
        }

        ProjectBudget updatedBudget = budget;
        if (updated) {
            updatedBudget = projectBudgetRepository.save(budget);
            log.info("Budget updated for project {} by user {}", projectId, currentUserId);
        } else {
            log.info("No changes detected for budget {} during update attempt by user {}", projectId, currentUserId);
        }

        float spentAmount = calculateSpentAmount(updatedBudget.getId());
        return BudgetResponseDto.fromEntity(updatedBudget, spentAmount);
    }

    @Override
    @Transactional
    public void deleteProjectBudget(UUID projectId, UUID currentUserId) {
        log.info("User {} attempting to delete budget for project {}", currentUserId, projectId);
        if (!projectService.isUserOwnerOfProject(projectId, currentUserId)) {
            log.error("User {} is not owner of project {}. Budget deletion denied.", currentUserId, projectId);
            throw new UnauthorizedOperationException("Only the project owner can delete a budget.");
        }
        ProjectBudget budget = projectBudgetRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Budget not found for project ID: {} during deletion by user {}", projectId, currentUserId);
                    return new BudgetNotFoundException("Budget not found for project ID: " + projectId);
                });

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.error("Project not found with ID: {} consistency issue during budget deletion for user {}", projectId, currentUserId);
                    return new ProjectNotFoundException("Project not found with ID: " + projectId);
                });
        project.setBudget(null);
        projectRepository.save(project);
        log.info("Budget for project {} successfully unlinked and will be deleted (or deleted if no orphanRemoval) by user {}", projectId, currentUserId);
    }


    @Override
    @Transactional(readOnly = true)
    public RemainingBudgetDto getRemainingProjectBudget(UUID projectId, UUID currentUserId) {
        log.info("User {} attempting to get remaining budget for project {}", currentUserId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectBudget budget = projectBudgetRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Budget not found for project ID: {} by user {} when getting remaining budget.", projectId, currentUserId);
                    return new BudgetNotFoundException("Budget not found for project ID: " + projectId);
                });
        float spentAmount = calculateSpentAmount(budget.getId());
        RemainingBudgetDto remaining = new RemainingBudgetDto(budget.getTotalAmount(), spentAmount, budget.getTotalAmount() - spentAmount, budget.getCurrency());
        log.info("Remaining budget for project {} retrieved by user {}: Total={}, Spent={}, Remaining={}, Currency={}",
                projectId, currentUserId, remaining.getTotalAmount(), remaining.getSpentAmount(), remaining.getRemainingAmount(), remaining.getCurrency());
        return remaining;
    }

    @Override
    @Transactional
    public ExpenseResponseDto addExpenseToBudget(UUID projectId, ExpenseCreateDto expenseDto, UUID currentUserId) {
        log.info("User {} attempting to add expense to budget for project {}", currentUserId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        ProjectBudget budget = projectBudgetRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Budget not found for project ID: {} by user {} when adding expense. Cannot add expense.", projectId, currentUserId);
                    return new BudgetNotFoundException("Budget not found for project ID: " + projectId + ". Cannot add expense.");
                });

        BudgetExpense expense = new BudgetExpense();
        expense.setBudget(budget);
        expense.setAmount(expenseDto.getAmount());
        expense.setDate(expenseDto.getDate() != null ? expenseDto.getDate() : new Date());
        expense.setComment(expenseDto.getComment());

        BudgetExpense savedExpense = budgetExpenseRepository.save(expense);
        log.info("Expense {} added to budget for project {} by user {}", savedExpense.getId(), projectId, currentUserId);
        return ExpenseResponseDto.fromEntity(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseById(UUID projectId, UUID expenseId, UUID currentUserId) {
        log.info("User {} attempting to get expense {} for budget of project {}", currentUserId, expenseId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        if (!projectBudgetRepository.existsById(projectId)) {
            log.warn("Budget not found for project ID: {} by user {} when getting expense {}", projectId, currentUserId, expenseId);
            throw new BudgetNotFoundException("Budget not found for project ID: " + projectId);
        }
        BudgetExpense expense = budgetExpenseRepository.findByIdAndBudgetId(expenseId, projectId)
                .orElseThrow(() -> {
                    log.warn("Expense with ID {} not found for budget {} (project) by user {}", expenseId, projectId, currentUserId);
                    return new ExpenseNotFoundException("Expense with ID " + expenseId + " not found for budget " + projectId);
                });
        log.info("Expense {} retrieved for budget of project {} by user {}", expenseId, projectId, currentUserId);
        return ExpenseResponseDto.fromEntity(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getExpensesForProjectBudget(UUID projectId, UUID currentUserId, Pageable pageable) {
        log.info("User {} listing expenses for budget of project {} with pageable: {}", currentUserId, projectId, pageable);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        if (!projectBudgetRepository.existsById(projectId)) {
            log.warn("Budget not found for project ID: {} by user {} when listing expenses", projectId, currentUserId);
            throw new BudgetNotFoundException("Budget not found for project ID: " + projectId);
        }
        Page<BudgetExpense> expensesPage = budgetExpenseRepository.findByBudgetId(projectId, pageable);
        log.info("Found {} expenses for budget of project {} for user {}", expensesPage.getTotalElements(), projectId, currentUserId);
        return expensesPage.map(ExpenseResponseDto::fromEntity);
    }


    @Override
    @Transactional
    public ExpenseResponseDto updateExpense(UUID projectId, UUID expenseId, ExpenseUpdateDto expenseDto, UUID currentUserId) {
        log.info("User {} attempting to update expense {} for budget of project {}", currentUserId, expenseId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        if (!projectBudgetRepository.existsById(projectId)) {
            log.warn("Budget not found for project ID: {} by user {} when updating expense {}", projectId, currentUserId, expenseId);
            throw new BudgetNotFoundException("Budget not found for project ID: " + projectId);
        }
        BudgetExpense expense = budgetExpenseRepository.findByIdAndBudgetId(expenseId, projectId)
                .orElseThrow(() -> {
                    log.warn("Expense with ID {} not found for budget {} (project) by user {} during update", expenseId, projectId, currentUserId);
                    return new ExpenseNotFoundException("Expense with ID " + expenseId + " not found for budget " + projectId);
                });

        boolean updated = false;
        if (expenseDto.getAmount() != null && !expenseDto.getAmount().equals(expense.getAmount())) {
            expense.setAmount(expenseDto.getAmount());
            updated = true;
        }
        if (expenseDto.getDate() != null && !expenseDto.getDate().equals(expense.getDate())) {
            expense.setDate(expenseDto.getDate());
            updated = true;
        }
        if (expenseDto.getComment() != null && !expenseDto.getComment().equals(expense.getComment())) {
            expense.setComment(expenseDto.getComment());
            updated = true;
        }

        BudgetExpense updatedExpense = expense;
        if(updated){
            updatedExpense = budgetExpenseRepository.save(expense);
            log.info("Expense {} updated for budget of project {} by user {}", expenseId, projectId, currentUserId);
        } else {
            log.info("No changes detected for expense {} during update attempt by user {}", expenseId, currentUserId);
        }
        return ExpenseResponseDto.fromEntity(updatedExpense);
    }

    @Override
    @Transactional
    public void removeExpense(UUID projectId, UUID expenseId, UUID currentUserId) {
        log.info("User {} attempting to remove expense {} from budget of project {}", currentUserId, expenseId, projectId);
        projectService.verifyUserIsProjectMember(projectId, currentUserId);
        if (!projectBudgetRepository.existsById(projectId)) {
            log.warn("Budget not found for project ID: {} by user {} when removing expense {}", projectId, currentUserId, expenseId);
            throw new BudgetNotFoundException("Budget not found for project ID: " + projectId);
        }
        BudgetExpense expense = budgetExpenseRepository.findByIdAndBudgetId(expenseId, projectId)
                .orElseThrow(() -> {
                    log.warn("Expense with ID {} not found for budget {} (project) by user {} during removal", expenseId, projectId, currentUserId);
                    return new ExpenseNotFoundException("Expense with ID " + expenseId + " not found for budget " + projectId);
                });
        budgetExpenseRepository.delete(expense);
        log.info("Expense {} removed from budget of project {} by user {}", expenseId, projectId, currentUserId);
    }
}