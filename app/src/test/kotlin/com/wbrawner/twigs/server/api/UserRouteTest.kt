package com.wbrawner.twigs.server.api

import com.wbrawner.twigs.ErrorResponse
import com.wbrawner.twigs.LoginRequest
import com.wbrawner.twigs.SessionResponse
import com.wbrawner.twigs.model.User
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserRouteTest : ApiTest() {
    @Test
    fun `login with invalid username returns 401`() = apiTest { client ->
        val request = LoginRequest("invalid", "pass")
        val response = client.post("/api/users/login") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Invalid credentials", errorBody.message)
    }

    @Test
    fun `login with empty username returns 401`() = apiTest { client ->
        val request = LoginRequest("", "pass")
        val response = client.post("/api/users/login") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Invalid credentials", errorBody.message)
    }

    @Test
    fun `login with invalid password returns 401`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "\$2a\$10\$bETxbFPja1PyXVLybETxb.CWBYzyYdZpmCcA7NSIN8dkdzidt1Xv2"),
        )
        users.forEach { userRepository.save(it) }
        val request = LoginRequest("testuser", "pass")
        val response = client.post("/api/users/login") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Invalid credentials", errorBody.message)
    }

    @Test
    fun `login with empty password returns 401`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "\$2a\$10\$bETxbFPja1PyXVLybETxb.CWBYzyYdZpmCcA7NSIN8dkdzidt1Xv2"),
        )
        users.forEach { userRepository.save(it) }
        val request = LoginRequest("testuser", "")
        val response = client.post("/api/users/login") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Invalid credentials", errorBody.message)
    }

    @Test
    fun `login with valid credentials returns 200`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "\$2a\$10\$bETxbFPja1PyXVLybETxb.CWBYzyYdZpmCcA7NSIN8dkdzidt1Xv2"),
            User(name = "otheruser", password = "\$2a\$10\$bETxbFPja1PyXVLybETxb..rhfIeOkP4qil1Drj29LDUhBxVkm6fS"),
        )
        users.forEach { userRepository.save(it) }
        val request = LoginRequest("testuser", "testpassword")
        val response = client.post("/api/users/login") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val session = response.body<SessionResponse>()
        assertEquals(users.first().id, session.userId)
        assert(session.token.isNotBlank())
    }

    @Test
    fun `login with valid email returns 200`() = apiTest { client ->
        val users = listOf(
            User(
                name = "testuser",
                email = "test@example.com",
                password = "\$2a\$10\$bETxbFPja1PyXVLybETxb.CWBYzyYdZpmCcA7NSIN8dkdzidt1Xv2"
            ),
            User(name = "otheruser", password = "\$2a\$10\$bETxbFPja1PyXVLybETxb..rhfIeOkP4qil1Drj29LDUhBxVkm6fS"),
        )
        users.forEach { userRepository.save(it) }
        val request = LoginRequest("test@example.com", "testpassword")
        val response = client.post("/api/users/login") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val session = response.body<SessionResponse>()
        assertEquals(users.first().id, session.userId)
        assert(session.token.isNotBlank())
    }
}