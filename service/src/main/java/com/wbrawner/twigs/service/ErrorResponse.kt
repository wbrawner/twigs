package com.wbrawner.twigs.service

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String)

class HttpException(
    val statusCode: HttpStatusCode,
    override val cause: Throwable? = null,
    override val message: String? = null
) : Throwable() {
    constructor(statusCode: HttpStatusCode) : this(statusCode = statusCode, message = statusCode.description)

    fun toResponse(): ErrorResponse =
        ErrorResponse(requireNotNull(message) { "Cannot send error to client without message" })
}