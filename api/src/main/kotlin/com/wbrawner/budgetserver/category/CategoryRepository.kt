package com.wbrawner.budgetserver.category

import com.wbrawner.budgetserver.budget.Budget
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface CategoryRepository : PagingAndSortingRepository<Category, String> {
    fun findAllByBudget(budget: Budget?, pageable: Pageable?): List<Category>

    @Query("SELECT c FROM Category c where c.budget IN (:budgets) AND (:expense IS NULL OR c.expense = :expense) AND (:archived IS NULL OR c.archived = :archived)")
    fun findAllByBudgetIn(
        budgets: List<Budget?>?,
        expense: Boolean?,
        archived: Boolean?,
        pageable: Pageable?
    ): List<Category>

    fun findByBudgetInAndId(budgets: List<Budget?>?, id: String?): Optional<Category>
    fun findByBudgetAndId(budget: Budget?, id: String?): Optional<Category>
    fun findAllByBudgetInAndIdIn(budgets: List<Budget?>?, ids: List<String?>?, pageable: Pageable?): List<Category>
}