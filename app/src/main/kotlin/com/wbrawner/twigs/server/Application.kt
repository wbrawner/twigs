package com.wbrawner.twigs.server

import com.wbrawner.twigs.budgetRoutes
import com.wbrawner.twigs.categoryRoutes
import io.ktor.application.*
import io.ktor.auth.*

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

fun Application.module(budgetReposi) {

    install(Authentication)
    budgetRoutes()
    categoryRoutes()
}