package com.wbrawner.twigs.db

import com.wbrawner.twigs.asFrequency
import com.wbrawner.twigs.model.RecurringTransaction
import com.wbrawner.twigs.storage.RecurringTransactionRepository
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource

class JdbcRecurringTransactionRepository(dataSource: DataSource) :
    JdbcRepository<RecurringTransaction, JdbcRecurringTransactionRepository.Fields>(dataSource),
    RecurringTransactionRepository {
    override val tableName: String = TABLE_RECURRING_TRANSACTION
    override val fields: Map<Fields, (RecurringTransaction) -> Any?> = Fields.values().associateWith { it.entityField }
    override val conflictFields: Collection<String> = listOf(ID)

    override suspend fun findAll(now: Instant): List<RecurringTransaction> = dataSource.connection.use { conn ->
        conn.executeQuery("SELECT * FROM $tableName WHERE ${Fields.START.name.lowercase()} < ? AND ${Fields.FINISH.name.lowercase()} > ?", listOf(now, now))
    }

    override suspend fun findAll(budgetId: String): List<RecurringTransaction> = dataSource.connection.use { conn ->
        if (budgetId.isBlank()) throw IllegalArgumentException("budgetId cannot be null")
        conn.executeQuery("SELECT * FROM $tableName WHERE ${Fields.BUDGET_ID.name.lowercase()} = ?", listOf(budgetId))
    }

    override fun ResultSet.toEntity(): RecurringTransaction = RecurringTransaction(
        id = getString(ID),
        title = getString(Fields.TITLE.name.lowercase()),
        description = getString(Fields.DESCRIPTION.name.lowercase()),
        frequency = getString(Fields.FREQUENCY.name.lowercase()).asFrequency(),
        start = getInstant(Fields.START.name.lowercase())!!,
        finish = getInstant(Fields.FINISH.name.lowercase()),
        lastRun = getInstant(Fields.LAST_RUN.name.lowercase()),
        amount = getLong(Fields.AMOUNT.name.lowercase()),
        expense = getBoolean(Fields.EXPENSE.name.lowercase()),
        createdBy = getString(Fields.CREATED_BY.name.lowercase()),
        categoryId = getString(Fields.CATEGORY_ID.name.lowercase()),
        budgetId = getString(Fields.BUDGET_ID.name.lowercase()),
    )

    enum class Fields(val entityField: (RecurringTransaction) -> Any?) {
        TITLE({ it.title }),
        DESCRIPTION({ it.description }),
        FREQUENCY({ it.frequency }),
        START({ it.start }),
        FINISH({ it.finish }),
        LAST_RUN({ it.lastRun }),
        AMOUNT({ it.amount }),
        EXPENSE({ it.expense }),
        CREATED_BY({ it.createdBy }),
        CATEGORY_ID({ it.categoryId }),
        BUDGET_ID({ it.budgetId }),
    }

    companion object {
        const val TABLE_RECURRING_TRANSACTION = "recurring_transactions"
    }
}

