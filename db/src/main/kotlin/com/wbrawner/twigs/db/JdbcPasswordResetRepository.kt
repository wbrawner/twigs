package com.wbrawner.twigs.db

import com.wbrawner.twigs.model.PasswordResetToken
import com.wbrawner.twigs.storage.PasswordResetRepository
import java.sql.ResultSet
import javax.sql.DataSource

class JdbcPasswordResetRepository(dataSource: DataSource) :
    JdbcRepository<PasswordResetToken, JdbcPasswordResetRepository.Fields>(dataSource),
    PasswordResetRepository {
    override val tableName: String = TABLE_USER
    override val fields: Map<Fields, (PasswordResetToken) -> Any?> = Fields.values().associateWith { it.entityField }
    override val conflictFields: Collection<String> = listOf(ID)

    override fun ResultSet.toEntity(): PasswordResetToken = PasswordResetToken(
        id = getString(ID),
        userId = getString(Fields.USER_ID.name.lowercase()),
        expiration = getInstant(Fields.EXPIRATION.name.lowercase())!!
    )

    enum class Fields(val entityField: (PasswordResetToken) -> Any?) {
        USER_ID({ it.userId }),
        EXPIRATION({ it.expiration })
    }

    companion object {
        const val TABLE_USER = "password_reset_tokens"
    }
}

