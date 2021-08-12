package com.wbrawner.twigs.db

import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.UserPermission
import com.wbrawner.twigs.storage.PermissionRepository
import java.sql.ResultSet
import javax.sql.DataSource

class JdbcPermissionRepository(dataSource: DataSource) :
    JdbcRepository<UserPermission, JdbcPermissionRepository.Fields>(dataSource), PermissionRepository {
    override val tableName: String = TABLE_PERMISSIONS
    override val fields: Map<Fields, (UserPermission) -> Any?> = Fields.values().associateWith { it.entityField }
    override val conflictFields: Collection<String> =
        listOf(Fields.USER_ID.name.lowercase(), Fields.BUDGET_ID.name.lowercase())

    override fun findAll(budgetIds: List<String>?, userId: String?): List<UserPermission> =
        dataSource.connection.use { conn ->
            if (budgetIds.isNullOrEmpty() && userId.isNullOrBlank()) {
                throw Error("budgetIds or userId must be provided")
            }
            val sql = StringBuilder("SELECT * FROM $tableName")
            val params = mutableListOf<String>()
            budgetIds?.let {
                sql.append(" WHERE ${Fields.BUDGET_ID.name.lowercase()} IN (${it.questionMarks()})")
                params.addAll(it)
            }
            userId?.let {
                sql.append(if (params.isEmpty()) " WHERE " else " AND ")
                sql.append("${Fields.USER_ID.name.lowercase()} = ?")
                params.add(it)
            }
            conn.executeQuery(sql.toString(), params)
        }

    override fun ResultSet.toEntity(): UserPermission = UserPermission(
        budgetId = getString(Fields.BUDGET_ID.name.lowercase()),
        userId = getString(Fields.USER_ID.name.lowercase()),
        permission = Permission.valueOf(getString(Fields.PERMISSION.name.lowercase()))
    )

    enum class Fields(val entityField: (UserPermission) -> Any?) {
        BUDGET_ID(
            { it.budgetId }),
        USER_ID(
            { it.userId }),
        PERMISSION(
            { it.permission })
    }

    companion object {
        const val TABLE_PERMISSIONS = "user_permissions"
    }
}

