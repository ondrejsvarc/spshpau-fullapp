package com.spshpau.projectservice.controller.impl;

import com.spshpau.projectservice.controller.BudgetController;
import com.spshpau.projectservice.dto.*;
import com.spshpau.projectservice.services.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/budget")
@RequiredArgsConstructor
public class BudgetControllerImpl implements BudgetController {
    private final BudgetService budgetService;

    private UUID getUserIdFromJwt(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    // --- Budget Endpoints ---
    @Override
    @PostMapping
    public ResponseEntity<BudgetResponseDto> createProjectBudget(@PathVariable UUID projectId,
                                                                 @Valid @RequestBody BudgetCreateDto budgetDto,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        BudgetResponseDto createdBudget = budgetService.createProjectBudget(projectId, budgetDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget);
    }

    @Override
    @GetMapping
    public ResponseEntity<BudgetResponseDto> getProjectBudget(@PathVariable UUID projectId, @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        BudgetResponseDto budget = budgetService.getProjectBudget(projectId, currentUserId);
        return ResponseEntity.ok(budget);
    }

    @Override
    @PutMapping
    public ResponseEntity<BudgetResponseDto> updateProjectBudget(@PathVariable UUID projectId,
                                                                 @Valid @RequestBody BudgetUpdateDto budgetDto,
                                                                 @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        BudgetResponseDto updatedBudget = budgetService.updateProjectBudget(projectId, budgetDto, currentUserId);
        return ResponseEntity.ok(updatedBudget);
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteProjectBudget(@PathVariable UUID projectId, @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        budgetService.deleteProjectBudget(projectId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/remaining")
    public ResponseEntity<RemainingBudgetDto> getRemainingProjectBudget(@PathVariable UUID projectId, @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        RemainingBudgetDto remainingBudget = budgetService.getRemainingProjectBudget(projectId, currentUserId);
        return ResponseEntity.ok(remainingBudget);
    }

    // --- Expense Endpoints ---
    @Override
    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponseDto> addExpense(@PathVariable UUID projectId,
                                                         @Valid @RequestBody ExpenseCreateDto expenseDto,
                                                         @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        ExpenseResponseDto createdExpense = budgetService.addExpenseToBudget(projectId, expenseDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
    }

    @Override
    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> getExpenseById(@PathVariable UUID projectId,
                                                             @PathVariable UUID expenseId,
                                                             @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        ExpenseResponseDto expense = budgetService.getExpenseById(projectId, expenseId, currentUserId);
        return ResponseEntity.ok(expense);
    }

    @Override
    @GetMapping("/expenses")
    public ResponseEntity<Page<ExpenseResponseDto>> getExpensesForProject(@PathVariable UUID projectId,
                                                                          @AuthenticationPrincipal Jwt jwt,
                                                                          @PageableDefault(size=50, sort="date") Pageable pageable) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        Page<ExpenseResponseDto> expenses = budgetService.getExpensesForProjectBudget(projectId, currentUserId, pageable);
        return ResponseEntity.ok(expenses);
    }

    @Override
    @PutMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> updateExpense(@PathVariable UUID projectId,
                                                            @PathVariable UUID expenseId,
                                                            @Valid @RequestBody ExpenseUpdateDto expenseDto,
                                                            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        ExpenseResponseDto updatedExpense = budgetService.updateExpense(projectId, expenseId, expenseDto, currentUserId);
        return ResponseEntity.ok(updatedExpense);
    }

    @Override
    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> removeExpense(@PathVariable UUID projectId,
                                              @PathVariable UUID expenseId,
                                              @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        budgetService.removeExpense(projectId, expenseId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
