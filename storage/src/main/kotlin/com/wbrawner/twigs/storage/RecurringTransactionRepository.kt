package com.wbrawner.twigs.storage

import com.wbrawner.twigs.model.RecurringTransaction
import java.time.Instant

interface RecurringTransactionRepository : Repository<RecurringTransaction> {
    suspend fun findAll(now: Instant): List<RecurringTransaction>
}