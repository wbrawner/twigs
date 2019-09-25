package com.wbrawner.budgetserver.transaction

import com.wbrawner.budgetserver.budget.Budget
import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.setToFirstOfMonth
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.time.Instant
import java.util.*

interface TransactionRepository: PagingAndSortingRepository<Transaction, Long> {
    fun findAllByBudget(budget: Budget, pageable: Pageable): List<Transaction>
    fun findAllByBudgetIn(budgets: List<Budget>, pageable: Pageable): List<Transaction>
    fun findAllByBudgetInAndCategoryInAndDateGreaterThan(budgets: List<Budget>, categories: List<Category>, start: Instant, pageable: Pageable? = null): List<Transaction>
    fun findAllByBudgetInAndDateGreaterThanAndDateLessThan(budgets: List<Budget>, start: Date, end: Date, pageable: Pageable): List<Transaction>
    fun findByBudgetAndId(budget: Budget, id: Long): Optional<Transaction>
    fun findAllByBudgetAndCategory(budget: Budget, category: Category): List<Transaction>
    fun findAllByBudgetInAndCategoryIn(budgets: List<Budget>, categories: List<Category>, pageable: Pageable? = null) =
            findAllByBudgetInAndCategoryInAndDateGreaterThan(budgets, categories, GregorianCalendar().setToFirstOfMonth().toInstant(), pageable)
    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE Budget_id = :BudgetId AND expense = 0 AND date > :start), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE Budget_id = :BudgetId AND expense = 1 AND date > :date), 0));"
    )
    fun sumBalanceByBudgetId(BudgetId: Long, start: Date = GregorianCalendar().setToFirstOfMonth().time): Long

    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 0 AND date > :start), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 1 AND date > :start), 0));"
    )
    fun sumBalanceByCategoryId(categoryId: Long, start: Date = GregorianCalendar().setToFirstOfMonth().time): Long
}