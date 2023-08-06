package com.wbrawner.twigs.recurringtransaction;

import com.wbrawner.twigs.budget.Budget;
import com.wbrawner.twigs.category.Category;
import com.wbrawner.twigs.transaction.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends CrudRepository<RecurringTransaction, String>,
        PagingAndSortingRepository<RecurringTransaction, String> {
    Optional<Transaction> findByIdAndBudgetIn(String id, List<Budget> budgets);

    List<Transaction> findAllByBudgetInAndCategoryInAndDateGreaterThanAndDateLessThan(
            List<Budget> budgets,
            List<Category> categories,
            Instant start,
            Instant end,
            Pageable pageable
    );

    List<Transaction> findAllByBudgetInAndDateGreaterThanAndDateLessThan(
            List<Budget> budgets,
            Instant start,
            Instant end,
            Pageable pageable
    );

    List<Transaction> findAllByBudgetAndCategory(Budget budget, Category category);

    void deleteAllByBudget(Budget budget);
}