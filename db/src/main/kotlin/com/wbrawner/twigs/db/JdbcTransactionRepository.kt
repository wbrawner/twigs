package com.wbrawner.twigs.db

import com.wbrawner.twigs.model.Transaction
import com.wbrawner.twigs.storage.TransactionRepository
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource

class JdbcTransactionRepository(dataSource: DataSource) :
    JdbcRepository<Transaction, JdbcTransactionRepository.Fields>(dataSource), TransactionRepository {
    override val tableName: String = TABLE_TRANSACTION
    override val fields: Map<Fields, (Transaction) -> Any?> = Fields.values().associateWith { it.entityField }
    override val conflictFields: Collection<String> = listOf(ID)

    override fun findAll(
        ids: List<String>?,
        budgetIds: List<String>?,
        categoryIds: List<String>?,
        expense: Boolean?,
        from: Instant?,
        to: Instant?
    ): List<Transaction> = dataSource.connection.use { conn ->
        val sql = StringBuilder("SELECT * FROM $tableName")
        val params = mutableListOf<Any?>(budgetIds)

        fun queryWord(): String = if (params.isEmpty()) " WHERE" else " AND"

        ids?.let {
            sql.append("${queryWord()} $ID IN (${it.questionMarks()})")
            params.addAll(it)
        }
        budgetIds?.let {
            sql.append("${queryWord()} ${Fields.BUDGET_ID.name.lowercase()} IN (${it.questionMarks()})")
            params.addAll(it)
        }
        categoryIds?.let {
            sql.append("${queryWord()} ${Fields.CATEGORY_ID.name.lowercase()} IN (${it.questionMarks()})")
            params.addAll(it)
        }
        expense?.let {
            sql.append("${queryWord()} ${Fields.EXPENSE.name.lowercase()} = ?")
            params.add(it)
        }
        from?.let {
            sql.append("${queryWord()} ${Fields.DATE.name.lowercase()} >= ?")
            params.add(it)
        }
        to?.let {
            sql.append("${queryWord()} ${Fields.DATE.name.lowercase()} <= ?")
            params.add(it)
        }
        conn.executeQuery(sql.toString(), params)
    }

    override fun sumByBudget(budgetId: String, from: Instant, to: Instant): Long =
        querySum(Fields.BUDGET_ID, budgetId, from, to)

    override fun sumByCategory(categoryId: String, from: Instant, to: Instant): Long =
        querySum(Fields.CATEGORY_ID, categoryId, from, to)

    private fun querySum(field: Fields, id: String, from: Instant?, to: Instant?): Long =
        dataSource.connection.use { conn ->
            val sql =
                StringBuilder("SELECT SUM(${Fields.AMOUNT.name.lowercase()}) FROM $tableName WHERE ${field.name.lowercase()} = ?")
            val params = mutableListOf<Any?>(id)
            from?.let {
                sql.append(" AND ${Fields.DATE.name.lowercase()} >= ?")
                params.add(it)
            }
            to?.let {
                sql.append(" AND ${Fields.DATE.name.lowercase()} <= ?")
                params.add(it)
            }
            sql.append(" AND ${Fields.EXPENSE.name.lowercase()} = ?")
            conn.prepareStatement("SELECT (${sql.toString().coalesce()}) - (${sql.toString().coalesce()})")
                .setParameters(params + false + params + true)
                .executeQuery()
                .getLong(1)
        }

    private fun String.coalesce(): String = "COALESCE(($this), 0)"

    override fun ResultSet.toEntity(): Transaction = Transaction(
        id = getString(ID),
        title = getString(Fields.TITLE.name.lowercase()),
        description = getString(Fields.DESCRIPTION.name.lowercase()),
        date = Instant.parse(getString(Fields.DATE.name.lowercase())),
        amount = getLong(Fields.AMOUNT.name.lowercase()),
        expense = getBoolean(Fields.EXPENSE.name.lowercase()),
        createdBy = getString(Fields.CREATED_BY.name.lowercase()),
        categoryId = getString(Fields.CATEGORY_ID.name.lowercase()),
        budgetId = getString(Fields.BUDGET_ID.name.lowercase()),
    )

    enum class Fields(val entityField: (Transaction) -> Any?) {
        TITLE({ it.title }),
        DESCRIPTION({ it.description }),
        DATE({ it.date }),
        AMOUNT({ it.amount }),
        EXPENSE({ it.expense }),
        CREATED_BY({ it.createdBy }),
        CATEGORY_ID({ it.categoryId }),
        BUDGET_ID({ it.budgetId }),
    }

    companion object {
        const val TABLE_TRANSACTION = "transactions"
    }
}

