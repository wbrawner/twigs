package com.wbrawner.twigs.db

import com.wbrawner.twigs.model.Budget
import com.wbrawner.twigs.storage.BudgetRepository
import java.sql.ResultSet
import javax.sql.DataSource

class JdbcBudgetRepository(dataSource: DataSource) : JdbcRepository<Budget, JdbcBudgetRepository.Fields>(dataSource),
    BudgetRepository {
    override val tableName: String = TABLE_BUDGET
    override val fields: Map<Fields, (Budget) -> Any?> = Fields.values().associateWith { it.entityField }
    override val conflictFields: Collection<String> = listOf(ID)

    override fun ResultSet.toEntity(): Budget = Budget(
        id = getString(ID),
        name = getString(Fields.NAME.name.lowercase()),
        description = getString(Fields.DESCRIPTION.name.lowercase()),
        currencyCode = getString(Fields.CURRENCY_CODE.name.lowercase())
    )

    enum class Fields(val entityField: (Budget) -> Any?) {
        NAME({ it.name }),
        DESCRIPTION({ it.description }),
        CURRENCY_CODE({ it.currencyCode })
    }

    companion object {
        const val TABLE_BUDGET = "budgets"
    }
}

