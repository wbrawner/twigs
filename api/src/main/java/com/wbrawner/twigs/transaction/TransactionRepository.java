package com.wbrawner.twigs.transaction;

import com.wbrawner.twigs.budget.Budget;
import com.wbrawner.twigs.category.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Transaction, String>,
        PagingAndSortingRepository<Transaction, String> {
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