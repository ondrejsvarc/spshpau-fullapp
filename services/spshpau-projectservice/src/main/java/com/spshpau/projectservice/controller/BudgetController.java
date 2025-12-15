package com.spshpau.projectservice.controller;

import com.spshpau.projectservice.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

public interface BudgetController {

    // Budget Endpoints
    /**
     * Creates a new budget for a specified project.
     *
     * @param projectId The ID of the project for which to create the budget.
     * @param budgetDto The budget creation data.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the created BudgetResponseDto and HTTP status.
     * Example Response (201 Created):
     * <pre>{@code
     * {
     * "projectId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
     * "currency": "USD",
     * "totalAmount": 10000.00,
     * "spentAmount": 0.00,
     * "remainingAmount": 10000.00,
     * "expenses": []
     * }
     * }</pre>
     */
    ResponseEntity<BudgetResponseDto> createProjectBudget(@PathVariable UUID projectId,
                                                          @Valid @RequestBody BudgetCreateDto budgetDto,
                                                          Jwt jwt);

    /**
     * Retrieves the budget for a specified project.
     *
     * @param projectId The ID of the project for which to retrieve the budget.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the BudgetResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "projectId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
     * "currency": "USD",
     * "totalAmount": 10000.00,
     * "spentAmount": 1500.00,
     * "remainingAmount": 8500.00,
     * "expenses": [
     * {
     * "id": "e1f2g3h4-i5j6-k7l8-m9n0-o1p2q3r4s5t6",
     * "amount": 1500.00,
     * "date": "2024-05-09T10:00:00.000+00:00",
     * "comment": "Initial setup costs",
     * "budgetId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
     * }
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<BudgetResponseDto> getProjectBudget(@PathVariable UUID projectId, Jwt jwt);

    /**
     * Updates an existing budget for a specified project.
     *
     * @param projectId The ID of the project for which to update the budget.
     * @param budgetDto The budget update data.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the updated BudgetResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "projectId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
     * "currency": "EUR",
     * "totalAmount": 12000.00,
     * "spentAmount": 1500.00,
     * "remainingAmount": 10500.00,
     * "expenses": [
     * {
     * "id": "e1f2g3h4-i5j6-k7l8-m9n0-o1p2q3r4s5t6",
     * "amount": 1500.00,
     * "date": "2024-05-09T10:00:00.000+00:00",
     * "comment": "Initial setup costs",
     * "budgetId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
     * }
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<BudgetResponseDto> updateProjectBudget(@PathVariable UUID projectId,
                                                          @Valid @RequestBody BudgetUpdateDto budgetDto,
                                                          Jwt jwt);

    /**
     * Deletes the budget for a specified project.
     *
     * @param projectId The ID of the project for which to delete the budget.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity with no content and HTTP status 204.
     */
    ResponseEntity<Void> deleteProjectBudget(@PathVariable UUID projectId, Jwt jwt);

    /**
     * Retrieves the remaining budget for a specified project.
     *
     * @param projectId The ID of the project for which to retrieve the remaining budget.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the RemainingBudgetDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "totalAmount": 12000.00,
     * "spentAmount": 1500.00,
     * "remainingAmount": 10500.00,
     * "currency": "EUR"
     * }
     * }</pre>
     */
    ResponseEntity<RemainingBudgetDto> getRemainingProjectBudget(@PathVariable UUID projectId, Jwt jwt);

    // Expense Endpoints
    /**
     * Adds a new expense to the budget of a specified project.
     *
     * @param projectId The ID of the project to which the expense will be added.
     * @param expenseDto The expense creation data.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the created ExpenseResponseDto and HTTP status.
     * Example Response (201 Created):
     * <pre>{@code
     * {
     * "id": "f1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
     * "amount": 250.75,
     * "date": "2024-05-10T12:00:00.000+00:00",
     * "comment": "Software license",
     * "budgetId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
     * }
     * }</pre>
     */
    ResponseEntity<ExpenseResponseDto> addExpense(@PathVariable UUID projectId,
                                                  @Valid @RequestBody ExpenseCreateDto expenseDto,
                                                  Jwt jwt);
    /**
     * Retrieves a specific expense by its ID for a given project.
     *
     * @param projectId The ID of the project to which the expense belongs.
     * @param expenseId The ID of the expense to retrieve.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the ExpenseResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "id": "f1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
     * "amount": 250.75,
     * "date": "2024-05-10T12:00:00.000+00:00",
     * "comment": "Software license",
     * "budgetId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
     * }
     * }</pre>
     */
    ResponseEntity<ExpenseResponseDto> getExpenseById(@PathVariable UUID projectId,
                                                      @PathVariable UUID expenseId,
                                                      Jwt jwt);

    /**
     * Retrieves a paginated list of expenses for a specified project.
     *
     * @param projectId The ID of the project for which to retrieve expenses.
     * @param jwt The JWT token for authentication and authorization.
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a Page of ExpenseResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {
     * "id": "f1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
     * "amount": 250.75,
     * "date": "2024-05-10T12:00:00.000+00:00",
     * "comment": "Software license",
     * "budgetId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
     * },
     * {
     * "id": "e1f2g3h4-i5j6-k7l8-m9n0-o1p2q3r4s5t6",
     * "amount": 1500.00,
     * "date": "2024-05-09T10:00:00.000+00:00",
     * "comment": "Initial setup costs",
     * "budgetId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
     * }
     * ],
     * "pageable": {
     * "sort": {
     * "sorted": true,
     * "unsorted": false,
     * "empty": false
     * },
     * "offset": 0,
     * "pageNumber": 0,
     * "pageSize": 2,
     * "paged": true,
     * "unpaged": false
     * },
     * "last": true,
     * "totalPages": 1,
     * "totalElements": 2,
     * "size": 2,
     * "number": 0,
     * "sort": {
     * "sorted": true,
     * "unsorted": false,
     * "empty": false
     * },
     * "first": true,
     * "numberOfElements": 2,
     * "empty": false
     * }
     * }</pre>
     */
    ResponseEntity<Page<ExpenseResponseDto>> getExpensesForProject(@PathVariable UUID projectId,
                                                                   Jwt jwt,
                                                                   Pageable pageable);

    /**
     * Updates an existing expense for a specified project.
     *
     * @param projectId The ID of the project to which the expense belongs.
     * @param expenseId The ID of the expense to update.
     * @param expenseDto The expense update data.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the updated ExpenseResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "id": "f1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
     * "amount": 275.00,
     * "date": "2024-05-10T12:00:00.000+00:00",
     * "comment": "Updated software license cost",
     * "budgetId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
     * }
     * }</pre>
     */
    ResponseEntity<ExpenseResponseDto> updateExpense(@PathVariable UUID projectId,
                                                     @PathVariable UUID expenseId,
                                                     @Valid @RequestBody ExpenseUpdateDto expenseDto,
                                                     Jwt jwt);

    /**
     * Removes an expense from a specified project.
     *
     * @param projectId The ID of the project from which to remove the expense.
     * @param expenseId The ID of the expense to remove.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity with no content and HTTP status 204.
     */
    ResponseEntity<Void> removeExpense(@PathVariable UUID projectId,
                                       @PathVariable UUID expenseId,
                                       Jwt jwt);
}