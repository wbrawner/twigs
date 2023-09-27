package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.RecurringTransaction
import com.wbrawner.twigs.storage.RecurringTransactionRepository
import java.time.Instant

class FakeRecurringTransactionRepository : FakeRepository<RecurringTransaction>(), RecurringTransactionRepository {
    override suspend fun findAll(now: Instant): List<RecurringTransaction> = entities.filter {
        (it.start == now || it.start.isBefore(now)) && it.finish?.isAfter(now) ?: true
    }

    override suspend fun findAll(budgetId: String): List<RecurringTransaction> = entities.filter {
        it.budgetId == budgetId
    }
}