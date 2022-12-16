package com.wbrawner.twigs.server

import ch.qos.logback.classic.Level
import com.wbrawner.twigs.*
import com.wbrawner.twigs.db.*
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.storage.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

private const val DATABASE_VERSION = 3

fun Application.module() {
    val dbHost = environment.config.propertyOrNull("twigs.database.host")?.getString() ?: "localhost"
    val dbPort = environment.config.propertyOrNull("twigs.database.port")?.getString() ?: "5432"
    val dbName = environment.config.propertyOrNull("twigs.database.name")?.getString() ?: "twigs"
    val dbUser = environment.config.propertyOrNull("twigs.database.user")?.getString() ?: "twigs"
    val dbPass = environment.config.propertyOrNull("twigs.database.password")?.getString() ?: "twigs"
    val jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName?stringtype=unspecified"
    (LoggerFactory.getLogger("com.zaxxer.hikari") as ch.qos.logback.classic.Logger).level = Level.ERROR
    HikariDataSource(HikariConfig().apply {
        setJdbcUrl(jdbcUrl)
        username = dbUser
        password = dbPass
    }).also {
        moduleWithDependencies(
            emailService = SmtpEmailService(
                from = environment.config.propertyOrNull("twigs.smtp.from")?.getString(),
                host = environment.config.propertyOrNull("twigs.smtp.host")?.getString(),
                port = environment.config.propertyOrNull("twigs.smtp.port")?.getString()?.toIntOrNull(),
                username = environment.config.propertyOrNull("twigs.smtp.user")?.getString(),
                password = environment.config.propertyOrNull("twigs.smtp.pass")?.getString(),
            ),
            metadataRepository = MetadataRepository(it),
            budgetRepository = JdbcBudgetRepository(it),
            categoryRepository = JdbcCategoryRepository(it),
            passwordResetRepository = JdbcPasswordResetRepository(it),
            permissionRepository = JdbcPermissionRepository(it),
            recurringTransactionRepository = JdbcRecurringTransactionRepository(it),
            sessionRepository = JdbcSessionRepository(it),
            transactionRepository = JdbcTransactionRepository(it),
            userRepository = JdbcUserRepository(it)
        )
    }
}

fun Application.moduleWithDependencies(
    emailService: EmailService,
    metadataRepository: MetadataRepository,
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    passwordResetRepository: PasswordResetRepository,
    permissionRepository: PermissionRepository,
    recurringTransactionRepository: RecurringTransactionRepository,
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
                application.environment.log.info("Validating session")
                val storedSession = sessionRepository.findAll(session.token)
                    .firstOrNull()
                if (storedSession == null) {
                    application.environment.log.info("Did not find session!")
                    return@validate null
                } else {
                    application.environment.log.info("Found session!")
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
                    this@moduleWithDependencies.environment.log.info("Deserializing session!")
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
            explicitNulls = false
            isLenient = true
            allowSpecialFloatingPointValues = true
            allowStructuredMapKeys = true
            prettyPrint = false
            useArrayPolymorphism = true
        })
    }
    install(CORS) {
        allowHost("twigs.wbrawner.com", listOf("http", "https")) // TODO: Make configurable
        allowHost("localhost:4200", listOf("http", "https"))     // TODO: Make configurable
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.AcceptEncoding)
        allowHeader(HttpHeaders.AcceptLanguage)
        allowHeader(HttpHeaders.Connection)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Host)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.AccessControlRequestHeaders)
        allowHeader(HttpHeaders.AccessControlRequestMethod)
        allowHeader("Sec-Fetch-Dest")
        allowHeader("Sec-Fetch-Mode")
        allowHeader("Sec-Fetch-Site")
        allowHeader("sec-ch-ua")
        allowHeader("sec-ch-ua-mobile")
        allowHeader("sec-ch-ua-platform")
        allowHeader(HttpHeaders.UserAgent)
        allowHeader("DNT")
        allowCredentials = true
    }
    budgetRoutes(budgetRepository, permissionRepository)
    categoryRoutes(categoryRepository, permissionRepository)
    recurringTransactionRoutes(recurringTransactionRepository, permissionRepository)
    transactionRoutes(transactionRepository, permissionRepository)
    userRoutes(emailService, passwordResetRepository, permissionRepository, sessionRepository, userRepository)
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
            RecurringTransactionProcessingJob(recurringTransactionRepository, transactionRepository)
        )
        while (currentCoroutineContext().isActive) {
            jobs.forEach { it.run() }
            delay(TimeUnit.HOURS.toMillis(1))
        }
    }
}

interface Job {
    suspend fun run()
}
