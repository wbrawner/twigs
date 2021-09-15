package com.wbrawner.twigs.db

import com.wbrawner.twigs.Identifiable
import com.wbrawner.twigs.model.Frequency
import com.wbrawner.twigs.storage.Repository
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types.NULL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import javax.sql.DataSource

const val ID = "id"

abstract class JdbcRepository<Entity, Fields : Enum<Fields>>(protected val dataSource: DataSource) :
    Repository<Entity> {
    abstract val tableName: String
    abstract val fields: Map<Fields, (Entity) -> Any?>
    abstract val conflictFields: Collection<String>
    val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun findAll(ids: List<String>?): List<Entity> = dataSource.connection.use { conn ->
        val sql = if (!ids.isNullOrEmpty()) {
            "SELECT * FROM $tableName WHERE $ID in (${ids.questionMarks()})"
        } else {
            "SELECT * FROM $tableName"
        }
        conn.executeQuery(sql, ids ?: emptyList())
    }

    override suspend fun save(item: Entity): Entity = dataSource.connection.use { conn ->
        val sql = StringBuilder("INSERT INTO $tableName (")
        val params = mutableListOf<Any?>()
        if (item is Identifiable) {
            sql.append("$ID, ")
            params.add(item.id)
        }
        params.addAll(fields.values.map { it(item) })
        sql.append(fields.keys.joinToString(", ") { it.name.lowercase() })
        sql.append(") VALUES (")
        sql.append(params.questionMarks())
        sql.append(")")
        if (conflictFields.isNotEmpty()) {
            sql.append(" ON CONFLICT (")
            sql.append(conflictFields.joinToString(","))
            sql.append(") DO UPDATE SET ")
            sql.append(fields.keys.joinToString(", ") {
                "${it.name.lowercase()} = EXCLUDED.${it.name.lowercase()}"
            })
        }
        return if (conn.executeUpdate(sql.toString(), params) == 1) {
            item
        } else {
            throw Error("Failed to save entity $item")
        }
    }

    override suspend fun delete(item: Entity): Boolean = dataSource.connection.use { conn ->
        if (item !is Identifiable) {
            throw Error("No suitable delete operation implemented for ${item!!::class.simpleName}")
        }
        val statement = conn.prepareStatement("DELETE FROM $tableName WHERE $ID=?")
        statement.setString(1, item.id)
        statement.executeUpdate() == 1
    }

    private fun ResultSet.toEntityList(): List<Entity> {
        val entities = mutableListOf<Entity>()
        while (next()) {
            entities.add(toEntity())
        }
        return entities
    }

    abstract fun ResultSet.toEntity(): Entity

    protected fun Connection.executeQuery(sql: String, params: List<Any?>) = prepareStatement(sql)
        .apply {
            logger.debug("QUERY: $sql\nPARAMS: ${params.joinToString(", ")}")
        }
        .setParameters(params)
        .executeQuery()
        .toEntityList()

    protected fun Connection.executeUpdate(sql: String, params: List<Any?> = emptyList()) = prepareStatement(sql)
        .apply {
            logger.debug("QUERY: $sql\nPARAMS: ${params.joinToString(", ")}")
        }
        .setParameters(params)
        .executeUpdate()

    fun PreparedStatement.setParameters(params: Iterable<Any?>): PreparedStatement = apply {
        params.forEachIndexed { index, param ->
            when (param) {
                is Boolean -> setBoolean(index + 1, param)
                is Instant -> setString(index + 1, dateFormatter.format(param))
                is Int -> setInt(index + 1, param)
                is Long -> setLong(index + 1, param)
                is String -> setString(index + 1, param)
                is Enum<*> -> setString(index + 1, param.name)
                is Frequency -> setString(index + 1, param.toString())
                null -> setNull(index + 1, NULL)
                else -> throw Error("Unhandled parameter type: ${param.javaClass.name}")
            }
        }
    }
}

fun <T> Collection<T>.questionMarks(): String = List(this.size) { "?" }.joinToString(", ")


private val dateFormatter = DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd HH:mm:ss")
    .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true)
    .toFormatter()
    .withZone(ZoneId.of("UTC"))

fun ResultSet.getInstant(column: String): Instant? = getString(column)?.let { dateFormatter.parse(it, Instant::from) }
