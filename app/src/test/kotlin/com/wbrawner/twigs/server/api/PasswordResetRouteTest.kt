package com.wbrawner.twigs.server.api

import com.wbrawner.twigs.ErrorResponse
import com.wbrawner.twigs.PasswordResetRequest
import com.wbrawner.twigs.ResetPasswordRequest
import com.wbrawner.twigs.model.PasswordResetToken
import com.wbrawner.twigs.randomString
import com.wbrawner.twigs.test.helpers.repository.FakeUserRepository.Companion.TEST_USER
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class PasswordResetRouteTest : ApiTest() {
    @Test
    fun `reset password with invalid username returns 202`() = apiTest { client ->
        val request = ResetPasswordRequest(username = "invaliduser")
        val response = client.post("/api/resetpassword") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Accepted, response.status)
        assert(emailService.emails.isEmpty())
    }

    @Test
    fun `reset password with valid username returns 202`() = apiTest { client ->
        val request = ResetPasswordRequest(username = "testuser")
        val response = client.post("/api/resetpassword") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Accepted, response.status)
        assertEquals(1, emailService.emails.size)
        val email = emailService.emails.first()
        assertEquals(TEST_USER.email, email.to)
        assertEquals(1, passwordResetRepository.entities.size)
        val passwordReset = passwordResetRepository.entities.first()
        assertEquals(TEST_USER.id, passwordReset.userId)
    }

    @Test
    fun `password reset with invalid token returns 400`() = apiTest { client ->
        val request = PasswordResetRequest(token = randomString(), password = "newpass")
        val response = client.post("/api/passwordreset") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val error = response.body<ErrorResponse>()
        assertEquals("Invalid token", error.message)
    }

    @Test
    fun `password reset with expired token returns 400`() = apiTest { client ->
        val token = passwordResetRepository.save(PasswordResetToken(expiration = twoWeeksAgo))
        val request = PasswordResetRequest(token = token.id, password = "newpass")
        val response = client.post("/api/passwordreset") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val error = response.body<ErrorResponse>()
        assertEquals("Token expired", error.message)
    }

    @Test
    fun `password reset with valid token returns 200`() = apiTest { client ->
        val token = passwordResetRepository.save(PasswordResetToken(userId = userRepository.findAll("testuser").first().id))
        val request = PasswordResetRequest(token = token.id, password = "newpass")
        val response = client.post("/api/passwordreset") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals(
            "newpass",
            userRepository.findAll(TEST_USER.name).first().password
        )
        assert(passwordResetRepository.entities.isEmpty())
    }
}

private val twoWeeksAgo: Instant
    get() = GregorianCalendar(TimeZone.getTimeZone("UTC")).run {
        add(Calendar.DATE, -14)
        toInstant()
    }
