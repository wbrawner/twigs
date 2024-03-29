package com.wbrawner.twigs.db

import com.wbrawner.twigs.storage.Repository
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource

interface MetadataRepository : Repository<DatabaseMetadata> {
    fun runMigration(toVersion: Int)
}

class JdbcMetadataRepository(dataSource: DataSource) :
    JdbcRepository<DatabaseMetadata, JdbcMetadataRepository.Fields>(dataSource), MetadataRepository {
    override val tableName: String = TABLE_METADATA
    override val fields: Map<Fields, (DatabaseMetadata) -> Any?> = Fields.values().associateWith { it.entityField }
    override val conflictFields: Collection<String> = listOf()

    override fun runMigration(toVersion: Int) {
        val queries = MetadataRepository::class.java
            .getResource("/sql/$toVersion.sql")
            ?.readText()
            ?.split(";")
            ?.filterNot { it.isBlank() }
            ?: throw Error("No migration found for version $toVersion")
        dataSource.connection.use { conn ->
            queries.forEach { query ->
                conn.executeUpdate(query)
            }
        }
    }

    override suspend fun delete(item: DatabaseMetadata): Boolean = throw Error("DatabaseMetadata cannot be deleted")

    override suspend fun findAll(ids: List<String>?): List<DatabaseMetadata> = try {
        super.findAll(null)
    } catch (e: SQLException) {
        emptyList()
    }

    override suspend fun save(item: DatabaseMetadata): DatabaseMetadata = dataSource.connection.use { conn ->
        conn.executeUpdate("DELETE FROM $tableName", emptyList())
        super.save(item)
    }

    override fun ResultSet.toEntity(): DatabaseMetadata = DatabaseMetadata(
        version = getInt(Fields.VERSION.name.lowercase()),
        salt = getString(Fields.SALT.name.lowercase())
    )

    enum class Fields(val entityField: (DatabaseMetadata) -> Any?) {
        VERSION({ it.version }),
        SALT({ it.salt }),
    }

    companion object {
        const val TABLE_METADATA = "twigs_metadata"
    }
}
