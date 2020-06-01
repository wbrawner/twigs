package com.wbrawner.budgetserver.category;

import com.wbrawner.budgetserver.budget.Budget;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends PagingAndSortingRepository<Category, Long> {
    List<Category> findAllByBudget(Budget budget, Pageable pageable);

    List<Category> findAllByBudgetIn(List<Budget> budgets, Pageable pageable);

    Optional<Category> findByBudgetInAndId(List<Budget> budgets, Long id);

    List<Category> findAllByBudgetInAndExpense(List<Budget> budgets, Boolean isExpense, Pageable pageable);

    Optional<Category> findByBudgetAndId(Budget budget, Long id);

    List<Category> findAllByBudgetInAndIdIn(List<Budget> budgets, List<Long> ids, Pageable pageable);
}