package com.wbrawner.twigs.db

import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.storage.SessionRepository
import java.sql.ResultSet
import java.time.Instant
import javax.sql.DataSource

class JdbcSessionRepository(dataSource: DataSource) : JdbcRepository<Session, JdbcSessionRepository.Fields>(dataSource),
    SessionRepository {
    override val tableName: String = TABLE_SESSION
    override val fields: Map<Fields, (Session) -> Any?> = Fields.values().associateWith { it.entityField }
    override val conflictFields: Collection<String> = listOf(ID)

    override fun findAll(token: String): List<Session> = dataSource.connection.use { conn ->
        val sql = "SELECT * FROM $tableName WHERE ${Fields.TOKEN.name.lowercase()} = ?"
        val params = mutableListOf(token)
        conn.executeQuery(sql, params)
    }

    override fun deleteExpired() {
        dataSource.connection.use { conn ->
            val sql = "DELETE FROM $tableName WHERE ${Fields.TOKEN.name.lowercase()} < ?"
            val params = mutableListOf(Instant.now())
            conn.executeUpdate(sql, params)
        }
    }

    override fun ResultSet.toEntity(): Session = Session(
        id = getString(ID),
        userId = getString(Fields.USER_ID.name.lowercase()),
        token = getString(Fields.TOKEN.name.lowercase()),
        expiration = getInstant(Fields.EXPIRATION.name.lowercase()),
    )

    enum class Fields(val entityField: (Session) -> Any?) {
        USER_ID({ it.userId }),
        TOKEN({ it.token }),
        EXPIRATION({ it.expiration }),
    }

    companion object {
        const val TABLE_SESSION = "sessions"
    }
}


