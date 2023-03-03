package com.wbrawner.twigs.category;

import com.wbrawner.twigs.budget.Budget;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends PagingAndSortingRepository<Category, String> {
    List<Category> findAllByBudget(Budget budget, Pageable pageable);

    @Query("SELECT c FROM Category c where c.budget IN (:budgets) AND (:expense IS NULL OR c.expense = :expense) AND (:archived IS NULL OR c.archived = :archived)")
    List<Category> findAllByBudgetIn(List<Budget> budgets, Boolean expense, Boolean archived, Pageable pageable);

    Optional<Category> findByBudgetInAndId(List<Budget> budgets, String id);

    Optional<Category> findByBudgetAndId(Budget budget, String id);

    List<Category> findAllByBudgetInAndIdIn(List<Budget> budgets, List<String> ids, Pageable pageable);
}