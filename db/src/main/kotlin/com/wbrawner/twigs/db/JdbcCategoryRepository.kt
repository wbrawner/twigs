package com.wbrawner.twigs.db

import com.wbrawner.twigs.model.Category
import com.wbrawner.twigs.storage.CategoryRepository
import java.sql.ResultSet
import javax.sql.DataSource

class JdbcCategoryRepository(dataSource: DataSource) :
    JdbcRepository<Category, JdbcCategoryRepository.Fields>(dataSource), CategoryRepository {
    override val tableName: String = TABLE_CATEGORY
    override val fields: Map<Fields, (Category) -> Any?> = Fields.values().associateWith { it.entityField }
    override val conflictFields: Collection<String> = listOf(ID)

    override fun findAll(
        budgetIds: List<String>,
        ids: List<String>?,
        expense: Boolean?,
        archived: Boolean?
    ): List<Category> = dataSource.connection.use { conn ->
        if (budgetIds.isEmpty()) {
            throw Error("budgetIds cannot be empty")
        }
        val sql =
            StringBuilder("SELECT * FROM $tableName WHERE ${Fields.BUDGET_ID.name.lowercase()} in (${budgetIds.questionMarks()})")
        val params = mutableListOf<Any?>()
        params.addAll(budgetIds)
        ids?.let {
            sql.append(" AND $ID IN (${it.questionMarks()})")
            params.addAll(it)
        }
        expense?.let {
            sql.append(" AND ${Fields.EXPENSE.name.lowercase()} = ?")
            params.add(it)
        }
        archived?.let {
            sql.append(" AND ${Fields.ARCHIVED.name.lowercase()} = ?")
            params.add(it)
        }
        sql.append(" ORDER BY ${Fields.TITLE.name.lowercase()} ASC")
        conn.executeQuery(sql.toString(), params)
    }

    override fun ResultSet.toEntity(): Category = Category(
        id = getString(ID),
        title = getString(Fields.TITLE.name.lowercase()),
        description = getString(Fields.DESCRIPTION.name.lowercase()),
        amount = getLong(Fields.AMOUNT.name.lowercase()),
        expense = getBoolean(Fields.EXPENSE.name.lowercase()),
        archived = getBoolean(Fields.ARCHIVED.name.lowercase()),
        budgetId = getString(Fields.BUDGET_ID.name.lowercase()),
    )

    enum class Fields(val entityField: (Category) -> Any?) {
        TITLE({ it.title }),
        DESCRIPTION({ it.description }),
        AMOUNT({ it.amount }),
        EXPENSE({ it.expense }),
        ARCHIVED({ it.archived }),
        BUDGET_ID({ it.budgetId }),
    }

    companion object {
        const val TABLE_CATEGORY = "categories"
    }
}

