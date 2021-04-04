package com.wbrawner.budgetserver.recurrence;

import com.wbrawner.budgetserver.budget.Budget;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends PagingAndSortingRepository<RecurringTransaction, String> {
    Optional<RecurringTransaction> findByIdAndBudgetIn(String id, List<Budget> budgets);

    List<RecurringTransaction> findAllByBudget(Budget budget);
}