package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.Transaction
import com.wbrawner.twigs.storage.TransactionRepository
import java.time.Instant

class FakeTransactionRepository : FakeRepository<Transaction>(), TransactionRepository {
    override fun findAll(
        ids: List<String>?,
        budgetIds: List<String>?,
        categoryIds: List<String>?,
        expense: Boolean?,
        from: Instant?,
        to: Instant?
    ): List<Transaction> = entities.filter { transaction ->
        ids?.contains(transaction.id) ?: true
                && budgetIds?.contains(transaction.budgetId) ?: true
                && categoryIds?.contains(transaction.categoryId) ?: true
                && expense?.let { it == transaction.expense } ?: true
                && from?.isBefore(transaction.date) ?: true
                && to?.isAfter(transaction.date) ?: true
    }

    override fun sumByBudget(budgetId: String, from: Instant, to: Instant): Long = entities.asSequence()
        .filter {
            it.budgetId == budgetId
                    && from.isBefore(it.date)
                    && to.isAfter(it.date)
        }
        .sumOf {
            val modifier = if (it.expense) -1 else 1
            it.amount * modifier
        }

    override fun sumByCategory(categoryId: String, from: Instant, to: Instant): Long = entities.asSequence()
        .filter {
            it.categoryId == categoryId
                    && from.isBefore(it.date)
                    && to.isAfter(it.date)
        }
        .sumOf {
            val modifier = if (it.expense) -1 else 1
            it.amount * modifier
        }
}