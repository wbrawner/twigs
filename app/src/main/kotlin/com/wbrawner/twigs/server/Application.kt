package com.wbrawner.twigs.server

import com.wbrawner.twigs.*
import com.wbrawner.twigs.db.*
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.storage.*
import com.wbrawner.twigs.web.webRoutes
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.serialization.*
import io.ktor.sessions.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

private const val DATABASE_VERSION = 1

@ExperimentalTime
fun Application.module() {
    val dbHost = environment.config.propertyOrNull("twigs.database.host")?.getString() ?: "localhost"
    val dbPort = environment.config.propertyOrNull("twigs.database.port")?.getString() ?: "5432"
    val dbName = environment.config.propertyOrNull("twigs.database.name")?.getString() ?: "twigs"
    val dbUser = environment.config.propertyOrNull("twigs.database.user")?.getString() ?: "twigs"
    val dbPass = environment.config.propertyOrNull("twigs.database.password")?.getString() ?: "twigs"
    val jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName?stringtype=unspecified"
    HikariDataSource(HikariConfig().apply {
        setJdbcUrl(jdbcUrl)
        username = dbUser
        password = dbPass
    }).also {
        moduleWithDependencies(
            metadataRepository = MetadataRepository(it),
            budgetRepository = JdbcBudgetRepository(it),
            categoryRepository = JdbcCategoryRepository(it),
            permissionRepository = JdbcPermissionRepository(it),
//            recurringTransactionRepository = Fa,
            sessionRepository = JdbcSessionRepository(it),
            transactionRepository = JdbcTransactionRepository(it),
            userRepository = JdbcUserRepository(it)
        )
    }
}

@ExperimentalTime
fun Application.moduleWithDependencies(
    metadataRepository: MetadataRepository,
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    permissionRepository: PermissionRepository,
//    recurringTransactionRepository: RecurringTransactionRepository,
    sessionRepository: SessionRepository,
    transactionRepository: TransactionRepository,
    userRepository: UserRepository
) {
    install(CallLogging)
    install(Authentication) {
        session<Session> {
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
            validate { session ->
                environment.log.info("Validating session")
                val storedSession = sessionRepository.findAll(session.token)
                    .firstOrNull()
                if (storedSession == null) {
                    environment.log.info("Did not find session!")
                    return@validate null
                } else {
                    environment.log.info("Found session!")
                }
                return@validate if (twoWeeksFromNow.isAfter(storedSession.expiration)) {
                    sessionRepository.save(storedSession.copy(expiration = twoWeeksFromNow))
                } else {
                    null
                }
            }
        }
    }
    install(Sessions) {
        header<Session>("Authorization") {
            serializer = object : SessionSerializer<Session> {
                override fun deserialize(text: String): Session {
                    environment.log.info("Deserializing session!")
                    return Session(token = text.substringAfter("Bearer "))
                }

                override fun serialize(session: Session): String = session.token
            }
        }
    }
    install(ContentNegotiation) {
        json(json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
            allowStructuredMapKeys = true
            prettyPrint = false
            useArrayPolymorphism = true
        })
    }
    budgetRoutes(budgetRepository, permissionRepository)
    categoryRoutes(categoryRepository, permissionRepository)
    transactionRoutes(transactionRepository, permissionRepository)
    userRoutes(permissionRepository, sessionRepository, userRepository)
    webRoutes()
    launch {
        val metadata = (metadataRepository.findAll().firstOrNull() ?: DatabaseMetadata())
        var version = metadata.version
        while (currentCoroutineContext().isActive && version++ < DATABASE_VERSION) {
            metadataRepository.runMigration(version)
            metadataRepository.save(metadata.copy(version = version))
        }
        salt = metadata.salt.ifEmpty {
            metadataRepository.save(
                metadata.copy(
                    salt = environment.config
                        .propertyOrNull("twigs.password.salt")
                        ?.getString()
                        ?: randomString(16)
                )
            ).salt
        }
        val jobs = listOf(
            SessionCleanupJob(sessionRepository),
//            RecurringTransactionProcessingJob(recurringTransactionRepository, transactionRepository)
        )
        while (currentCoroutineContext().isActive) {
            delay(Duration.hours(24))
            jobs.forEach { it.run() }
        }
    }
}

interface Job {
    suspend fun run()
}