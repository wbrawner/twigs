package com.wbrawner.twigs.server

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.webRoutes() {
    routing {
        static {
            staticBasePackage = "static"
            defaultResource("index.html")
            resources(".")
        }
        intercept(ApplicationCallPipeline.Setup) {
            if (!call.request.path().startsWith("/api") && !call.request.path().matches(Regex(".*\\.\\w+$"))) {
                call.resolveResource("static/index.html")?.let {
                    call.respond(it)
                    return@intercept finish()
                }
            }
        }
    }
}