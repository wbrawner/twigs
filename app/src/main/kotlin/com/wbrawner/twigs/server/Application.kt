package com.wbrawner.twigs.server

import com.wbrawner.twigs.budgetRoutes
import com.wbrawner.twigs.categoryRoutes
import com.wbrawner.twigs.storage.*
import com.wbrawner.twigs.twoWeeksFromNow
import com.wbrawner.twigs.userRoutes
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
    userRoutes(permissionRepository, sessionRepository, userRepository)
    launch {
        while (currentCoroutineContext().isActive) {
            delay(Duration.hours(24))
            sessionRepository.deleteExpired()
        }
    }
}