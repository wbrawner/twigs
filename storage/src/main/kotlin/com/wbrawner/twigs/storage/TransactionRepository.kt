package com.wbrawner.twigs.storage

import com.wbrawner.twigs.model.Transaction
import java.time.Instant

interface TransactionRepository : Repository<Transaction> {
    fun findAll(
        ids: List<String>? = null,
        budgetIds: List<String>? = null,
        categoryIds: List<String>? = null,
        expense: Boolean? = null,
        from: Instant? = null,
        to: Instant? = null,
    ): List<Transaction>

    fun sumByBudget(budgetId: String, from: Instant, to: Instant): Long

    fun sumByCategory(categoryId: String, from: Instant, to: Instant): Long
}