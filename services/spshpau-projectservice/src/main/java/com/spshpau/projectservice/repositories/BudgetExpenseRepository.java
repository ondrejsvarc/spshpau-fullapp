package com.spshpau.projectservice.repositories;

import com.spshpau.projectservice.model.BudgetExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetExpenseRepository extends JpaRepository<BudgetExpense, UUID> {
    @Query("SELECT SUM(e.amount) FROM BudgetExpense e WHERE e.budget.id = :budgetId")
    Optional<Float> sumExpensesByBudgetId(@Param("budgetId") UUID budgetId);

    Page<BudgetExpense> findByBudgetId(UUID budgetId, Pageable pageable);

    Optional<BudgetExpense> findByIdAndBudgetId(UUID expenseId, UUID budgetId);
}
