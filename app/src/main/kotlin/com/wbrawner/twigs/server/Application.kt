package com.wbrawner.twigs.server

import com.wbrawner.twigs.*
import com.wbrawner.twigs.model.Transaction
import com.wbrawner.twigs.storage.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.sessions.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@ExperimentalTime
fun Application.module(
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    permissionRepository: PermissionRepository,
    sessionRepository: SessionRepository,
    transactionRepository: TransactionRepository,
    userRepository: UserRepository
) {
    install(Sessions) {
        header<String>("Authorization")
    }
    install(Authentication) {
        session<String> {
            validate { token ->
                val session = sessionRepository.findAll(token).firstOrNull()
                    ?: return@validate null
                return@validate if (twoWeeksFromNow.after(session.expiration)) {
                    session
                } else {
                    null
                }
            }
        }
    }
    budgetRoutes(budgetRepository, permissionRepository)
    categoryRoutes(categoryRepository, permissionRepository)
    transactionRoutes(transactionRepository, permissionRepository)
    userRoutes(permissionRepository, sessionRepository, userRepository)
    launch {
        while (currentCoroutineContext().isActive) {
            delay(Duration.hours(24))
            sessionRepository.deleteExpired()
        }
    }
}