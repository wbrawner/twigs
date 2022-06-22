package com.wbrawner.twigs.web

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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