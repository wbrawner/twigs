package com.wbrawner.twigs.server.api

import com.wbrawner.twigs.server.moduleWithDependencies
import com.wbrawner.twigs.test.helpers.FakeEmailService
import com.wbrawner.twigs.test.helpers.repository.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach

open class ApiTest {
    lateinit var budgetRepository: FakeBudgetRepository
    lateinit var categoryRepository: FakeCategoryRepository
    lateinit var emailService: FakeEmailService
    lateinit var metadataRepository: FakeMetadataRepository
    lateinit var passwordResetRepository: FakePasswordResetRepository
    lateinit var permissionRepository: FakePermissionRepository
    lateinit var recurringTransactionRepository: FakeRecurringTransactionRepository
    lateinit var sessionRepository: FakeSessionRepository
    lateinit var transactionRepository: FakeTransactionRepository
    lateinit var userRepository: FakeUserRepository

    @BeforeEach
    fun setup() {
        budgetRepository = FakeBudgetRepository()
        categoryRepository = FakeCategoryRepository()
        emailService = FakeEmailService()
        metadataRepository = FakeMetadataRepository()
        passwordResetRepository = FakePasswordResetRepository()
        permissionRepository = FakePermissionRepository()
        recurringTransactionRepository = FakeRecurringTransactionRepository()
        sessionRepository = FakeSessionRepository()
        transactionRepository = FakeTransactionRepository()
        userRepository = FakeUserRepository()
    }

    fun apiTest(test: suspend ApiTest.(client: HttpClient) -> Unit) = testApplication {
        application {
            moduleWithDependencies(
                emailService = emailService,
                metadataRepository = metadataRepository,
                budgetRepository = budgetRepository,
                categoryRepository = categoryRepository,
                passwordHasher = { it },
                passwordResetRepository = passwordResetRepository,
                permissionRepository = permissionRepository,
                recurringTransactionRepository = recurringTransactionRepository,
                sessionRepository = sessionRepository,
                transactionRepository = transactionRepository,
                userRepository = userRepository
            )
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        test(client)
    }
}