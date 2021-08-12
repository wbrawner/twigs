package com.wbrawner.twigs.db

import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.UserRepository
import java.sql.ResultSet
import javax.sql.DataSource

class JdbcUserRepository(dataSource: DataSource) : JdbcRepository<User, JdbcUserRepository.Fields>(dataSource),
    UserRepository {
    override val tableName: String = TABLE_USER
    override val fields: Map<Fields, (User) -> Any?> = Fields.values().associateWith { it.entityField }
    override val conflictFields: Collection<String> = listOf(ID)

    override fun ResultSet.toEntity(): User = User(
        id = getString(ID),
        name = getString(Fields.USERNAME.name.lowercase()),
        password = getString(Fields.PASSWORD.name.lowercase()),
        email = getString(Fields.EMAIL.name.lowercase())
    )

    override fun findAll(nameLike: String): List<User> = dataSource.connection.use { conn ->
        conn.executeQuery(
            "SELECT * FROM $tableName WHERE ${Fields.USERNAME.name.lowercase()} LIKE ? || '%'",
            listOf(nameLike)
        )
    }

    override fun findAll(nameOrEmail: String, password: String): List<User> = dataSource.connection.use { conn ->
        conn.executeQuery(
            "SELECT * FROM $tableName WHERE (${Fields.USERNAME.name.lowercase()} = ? OR ${Fields.EMAIL.name.lowercase()} = ?) AND ${Fields.PASSWORD.name.lowercase()} = ?",
            listOf(nameOrEmail, nameOrEmail, password)
        )
    }

    enum class Fields(val entityField: (User) -> Any?) {
        USERNAME({ it.name }),
        PASSWORD({ it.password }),
        EMAIL({ it.email })
    }

    companion object {
        const val TABLE_USER = "users"
    }
}

