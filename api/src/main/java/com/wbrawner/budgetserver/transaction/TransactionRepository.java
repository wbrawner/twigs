package com.wbrawner.budgetserver.transaction;

import com.wbrawner.budgetserver.budget.Budget;
import com.wbrawner.budgetserver.category.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends PagingAndSortingRepository<Transaction, String> {
    Optional<Transaction> findByIdAndBudgetIn(String id, List<Budget> budgets);

    List<Transaction> findAllByBudgetInAndCategoryInAndDateGreaterThanAndDateLessThan(
            List<Budget> budgets,
            List<Category> categories,
            Instant start,
            Instant end,
            Pageable pageable
    );

    List<Transaction> findAllByBudgetAndCategory(Budget budget, Category category);

    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE Budget_id = :BudgetId AND expense = 0 AND date > :start), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE Budget_id = :BudgetId AND expense = 1 AND date > :date), 0));"
    )
    Long sumBalanceByBudgetId(String BudgetId, Date start);

    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 0 AND date > :start), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 1 AND date > :start), 0));"
    )
    Long sumBalanceByCategoryId(String categoryId, Date start);
}