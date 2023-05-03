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

    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE Budget_id = :BudgetId AND expense = 0 AND date >= :from AND date <= :to), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE Budget_id = :BudgetId AND expense = 1 AND date >= :from AND date <= :to), 0));"
    )
    Long sumBalanceByBudgetId(String BudgetId, Instant from, Instant to);

    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 0 AND date > :start), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 1 AND date > :start), 0));"
    )
    Long sumBalanceByCategoryId(String categoryId, Date start);

    void deleteAllByBudget(Budget budget);
}