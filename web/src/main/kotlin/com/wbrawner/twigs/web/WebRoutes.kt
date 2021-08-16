package com.wbrawner.twigs.web

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.webRoutes() {
    routing {
        static {
            resources("twigs")
            default("index.html")
        }
        intercept(ApplicationCallPipeline.Setup) {
            if (!call.request.path().startsWith("/api") && !call.request.path().matches(Regex(".*\\.\\w+$"))) {
                call.resolveResource("twigs/index.html")?.let {
                    call.respond(it)
                    return@intercept finish()
                }
            }
        }
    }
}