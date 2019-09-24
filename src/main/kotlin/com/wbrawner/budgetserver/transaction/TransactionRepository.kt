package com.wbrawner.budgetserver.transaction

import com.wbrawner.budgetserver.budget.Budget
import com.wbrawner.budgetserver.category.Category
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface TransactionRepository: PagingAndSortingRepository<Transaction, Long> {
    fun findAllByBudget(budget: Budget, pageable: Pageable): List<Transaction>
    fun findAllByBudgetIn(budgets: List<Budget>, pageable: Pageable): List<Transaction>
    fun findAllByBudgetInAndDateGreaterThan(budgets: List<Budget>, start: Date, pageable: Pageable): List<Transaction>
    fun findAllByBudgetInAndDateGreaterThanAndDateLessThan(budgets: List<Budget>, start: Date, end: Date, pageable: Pageable): List<Transaction>
    fun findByBudgetAndId(budget: Budget, id: Long): Optional<Transaction>
    fun findAllByBudgetAndCategory(budget: Budget, category: Category): List<Transaction>
    fun findAllByBudgetInAndCategoryIn(budgets: List<Budget>, categories: List<Category>, pageable: Pageable? = null): List<Transaction>
    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE Budget_id = :BudgetId AND expense = 0), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE Budget_id = :BudgetId AND expense = 1), 0));"
    )
    fun sumBalanceByBudgetId(BudgetId: Long): Long

    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 0), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 1), 0));"
    )
    fun sumBalanceByCategoryId(categoryId: Long): Long
}