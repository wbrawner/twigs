package com.wbrawner.twigs.server.api

import com.wbrawner.twigs.*
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.test.helpers.repository.FakeUserRepository.Companion.OTHER_USER
import com.wbrawner.twigs.test.helpers.repository.FakeUserRepository.Companion.TEST_USER
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
        val request = LoginRequest(TEST_USER.name, "pass")
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
        val request = LoginRequest(TEST_USER.name, "")
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
        val request = LoginRequest(TEST_USER.name, TEST_USER.password)
        val response = client.post("/api/users/login") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val session = response.body<SessionResponse>()
        assertEquals(TEST_USER.id, session.userId)
        assert(session.token.isNotBlank())
    }

    @Test
    fun `login with valid email and password returns 200`() = apiTest { client ->
        val request = LoginRequest(TEST_USER.email, TEST_USER.password)
        val response = client.post("/api/users/login") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val session = response.body<SessionResponse>()
        assertEquals(TEST_USER.id, session.userId)
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
        val request = UserRequest(username = TEST_USER.name, password = "password")
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
        val request = UserRequest(username = "testuser2", email = TEST_USER.email, password = "password")
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
        val initialUserCount = userRepository.entities.size
        val request = UserRequest("newuser", "newpass")
        val response = client.post("/api/users/register") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val userResponse = response.body<UserResponse>()
        assert(userResponse.id.isNotBlank())
        assertEquals(request.username, userResponse.username)
        assertEquals("", userResponse.email)
        assertEquals(initialUserCount + 1, userRepository.entities.size)
        val savedUser: User? = userRepository.findAll("newuser").firstOrNull()
        assertNotNull(savedUser)
        requireNotNull(savedUser)
        assertEquals(userResponse.id, savedUser.id)
        assertEquals(request.username, savedUser.name)
        assertEquals("", savedUser.email)
        assertEquals("newpass", savedUser.password)
    }

    @Test
    fun `get users requires authentication`() = apiTest { client ->
        val response = client.get("/api/users")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get users with empty query returns 400`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val response = client.get("/api/users?query=") {
            header("Authorization", "Bearer ${session.token}")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error: ErrorResponse = response.body()
        assertEquals("query cannot be empty", error.message)
    }

    @Test
    fun `get users with valid query but no matches returns empty list`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val response = client.get("/api/users?query=something") {
            header("Authorization", "Bearer ${session.token}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val userQueryResponse: List<UserResponse> = response.body()
        assertEquals(0, userQueryResponse.size)
    }

    @Test
    fun `get users with valid query and matches returns list`() = apiTest { client ->
        val session = Session(userId = TEST_USER.id)
        sessionRepository.save(session)
        val response = client.get("/api/users?query=user") {
            header("Authorization", "Bearer ${session.token}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val userQueryResponse: List<UserResponse> = response.body()
        assertEquals(2, userQueryResponse.size)
        assertEquals(TEST_USER.asResponse(), userQueryResponse[0])
        assertEquals(OTHER_USER.asResponse(), userQueryResponse[1])
    }

    @Test
    fun `get users with empty budgetId returns 400`() = apiTest { client ->
        val session = Session(userId = TEST_USER.id)
        sessionRepository.save(session)
        val response = client.get("/api/users?budgetId=") {
            header("Authorization", "Bearer ${session.token}")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}