package com.wbrawner.twigs.server.api

import com.wbrawner.twigs.server.moduleWithDependencies
import com.wbrawner.twigs.service.budget.DefaultBudgetService
import com.wbrawner.twigs.service.category.DefaultCategoryService
import com.wbrawner.twigs.service.recurringtransaction.DefaultRecurringTransactionService
import com.wbrawner.twigs.service.transaction.DefaultTransactionService
import com.wbrawner.twigs.service.user.DefaultUserService
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
                budgetService = DefaultBudgetService(budgetRepository, permissionRepository),
                categoryService = DefaultCategoryService(categoryRepository, permissionRepository),
                recurringTransactionService = DefaultRecurringTransactionService(
                    recurringTransactionRepository,
                    permissionRepository
                ),
                transactionService = DefaultTransactionService(
                    transactionRepository,
                    categoryRepository,
                    permissionRepository
                ),
                userService = DefaultUserService(
                    emailService,
                    passwordResetRepository,
                    permissionRepository,
                    sessionRepository,
                    userRepository,
                    { it }
                ),
                jobs = listOf(),
                sessionValidator = {
                    sessionRepository.findAll(it.token).firstOrNull()
                }
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