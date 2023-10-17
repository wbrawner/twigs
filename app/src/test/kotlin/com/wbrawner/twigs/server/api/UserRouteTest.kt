package com.wbrawner.twigs.server.api

import com.wbrawner.twigs.*
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
    fun `login with valid username and password returns 200`() = apiTest { client ->
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
    fun `login with valid email and password returns 200`() = apiTest { client ->
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

    @Test
    fun `register with null username returns 400`() = apiTest { client ->
        val request = UserRequest(password = "")
        val response = client.post("/api/users/register") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Username must not be null or blank", errorBody.message)
    }

    @Test
    fun `register with empty username returns 400`() = apiTest { client ->
        val request = UserRequest(username = "", password = "")
        val response = client.post("/api/users/register") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Username must not be null or blank", errorBody.message)
    }

    @Test
    fun `register with null password returns 400`() = apiTest { client ->
        val request = UserRequest(username = "test@example.com")
        val response = client.post("/api/users/register") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Password must not be null or blank", errorBody.message)
    }

    @Test
    fun `register with empty password returns 400`() = apiTest { client ->
        val request = UserRequest(username = "test@example.com", password = "")
        val response = client.post("/api/users/register") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Password must not be null or blank", errorBody.message)
    }

    @Test
    fun `register with existing username returns 400`() = apiTest { client ->
        val users = listOf(
            User(
                name = "testuser",
                email = "test@example.com",
                password = "\$2a\$10\$bETxbFPja1PyXVLybETxb.CWBYzyYdZpmCcA7NSIN8dkdzidt1Xv2"
            ),
        )
        users.forEach { userRepository.save(it) }
        val request = UserRequest(username = "testuser", password = "password")
        val response = client.post("/api/users/register") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Username or email already taken", errorBody.message)
    }

    @Test
    fun `register with existing email returns 400`() = apiTest { client ->
        val users = listOf(
            User(
                name = "testuser",
                email = "test@example.com",
                password = "\$2a\$10\$bETxbFPja1PyXVLybETxb.CWBYzyYdZpmCcA7NSIN8dkdzidt1Xv2"
            ),
        )
        users.forEach { userRepository.save(it) }
        val request = UserRequest(username = "testuser2", email = "test@example.com", password = "password")
        val response = client.post("/api/users/register") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val errorBody = response.body<ErrorResponse>()
        assertEquals("Username or email already taken", errorBody.message)
    }

    @Test
    fun `register with valid username and password returns 200`() = apiTest { client ->
        val request = UserRequest("testuser", "testpassword")
        val response = client.post("/api/users/register") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val userResponse = response.body<UserResponse>()
        assert(userResponse.id.isNotBlank())
        assertEquals(request.username, userResponse.username)
        assertEquals("", userResponse.email)
        assertEquals(1, userRepository.entities.size)
        val savedUser: User = userRepository.entities.first()
        assertEquals(userResponse.id, savedUser.id)
        assertEquals(request.username, savedUser.name)
        assertEquals("", savedUser.email)
        assertEquals("\$2a\$10\$bETxbFPja1PyXVLybETxb.CWBYzyYdZpmCcA7NSIN8dkdzidt1Xv2", savedUser.password)
    }

    @Test
    fun `get users requires authentication`() = apiTest { client ->
        val response = client.get("/api/users")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}