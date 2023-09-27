package com.wbrawner.twigs.server.api

import com.wbrawner.twigs.BudgetRequest
import com.wbrawner.twigs.BudgetResponse
import com.wbrawner.twigs.UserPermissionRequest
import com.wbrawner.twigs.UserPermissionResponse
import com.wbrawner.twigs.model.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class BudgetRouteTest : ApiTest() {

    @Test
    fun `fetching budgets requires authentication`() = apiTest { client ->
        val response = client.get("/api/budgets")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `fetching budgets returns empty list when there are no budgets`() = apiTest { client ->
        val session = Session()
        sessionRepository.save(session)
        val response = client.get("/api/budgets") {
            header("Authorization", "Bearer ${session.token}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(0, response.body<List<BudgetResponse>>().size)
    }

    @Test
    fun `fetching budgets only returns budgets for current user`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val currentUserBudget = budgetRepository.save(Budget(name = "Test User's Budget"))
        val otherUserBudget = budgetRepository.save(Budget(name = "Other User's Budget"))
        permissionRepository.save(
            UserPermission(
                budgetId = currentUserBudget.id,
                userId = users[0].id,
                Permission.OWNER
            )
        )
        permissionRepository.save(UserPermission(budgetId = otherUserBudget.id, userId = users[1].id, Permission.OWNER))
        val response = client.get("/api/budgets") {
            header("Authorization", "Bearer ${session.token}")
        }
        val returnedBudgets = response.body<List<BudgetResponse>>()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, returnedBudgets.size)
        assertEquals(currentUserBudget.id, returnedBudgets.first().id)
    }

    @Test
    fun `creating budgets requires authentication`() = apiTest { client ->
        val request = BudgetRequest("Test Budget", "A budget for testing")
        val response = client.post("/api/budgets") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `newly created budgets are saved`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermissionRequest(users[0].id, Permission.OWNER),
            UserPermissionRequest(users[1].id, Permission.READ),
        )
        val request = BudgetRequest("Test Budget", "A budget for testing", permissions)
        val response = client.post("/api/budgets") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody: BudgetResponse = response.body()
        assert(responseBody.id.isNotEmpty())
        assertEquals("Test Budget", responseBody.name)
        assertEquals("A budget for testing", responseBody.description)
        assertEquals(2, responseBody.users.size)
        assert(responseBody.users.containsAll(permissions.map { UserPermissionResponse(it.user, it.permission) }))
    }

    @Test
    fun `newly created budgets include current user as owner if omitted`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermissionRequest(users[1].id, Permission.OWNER),
        )
        val request = BudgetRequest("Test Budget", "A budget for testing", permissions)
        val response = client.post("/api/budgets") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody: BudgetResponse = response.body()
        assert(responseBody.id.isNotEmpty())
        assertEquals("Test Budget", responseBody.name)
        assertEquals("A budget for testing", responseBody.description)
        assertEquals(2, responseBody.users.size)
        val expectedPermissions = listOf(
            UserPermissionResponse(users[0].id, Permission.OWNER),
            UserPermissionResponse(users[1].id, Permission.OWNER),
        )
        assert(responseBody.users.containsAll(expectedPermissions))
    }

    @Test
    fun `updating budgets requires authentication`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val request = BudgetRequest("Update Budget", "A budget for testing")
        val response = client.put("/api/budgets/${existingBudget.id}") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `updating budgets returns forbidden for users with read only access`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermission(budgetId = existingBudget.id, userId = users[0].id, Permission.READ),
            UserPermission(budgetId = existingBudget.id, userId = users[1].id, Permission.OWNER),
        )
        permissions.forEach {
            permissionRepository.save(it)
        }
        val request = BudgetRequest("Update Budget", "A budget for testing")
        val response = client.put("/api/budgets/${existingBudget.id}") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `updating budgets returns forbidden for users with write only access`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermission(budgetId = existingBudget.id, userId = users[0].id, Permission.WRITE),
            UserPermission(budgetId = existingBudget.id, userId = users[1].id, Permission.OWNER),
        )
        permissions.forEach {
            permissionRepository.save(it)
        }
        val request = BudgetRequest("Update Budget", "A budget for testing")
        val response = client.put("/api/budgets/${existingBudget.id}") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `updating budgets returns success for users with manage access`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermission(budgetId = existingBudget.id, userId = users[0].id, Permission.MANAGE),
            UserPermission(budgetId = existingBudget.id, userId = users[1].id, Permission.OWNER),
        )
        permissions.forEach {
            permissionRepository.save(it)
        }
        val request = BudgetRequest("Update Budget", "An update budget for testing")
        val response = client.put("/api/budgets/${existingBudget.id}") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val updatedBudget: BudgetResponse = response.body()
        assertEquals(request.name, updatedBudget.name)
        assertEquals(request.description, updatedBudget.description)
        val expectedUsers = permissions.map { UserPermissionResponse(it.userId, it.permission) }
        val updatedUsers = updatedBudget.users
        assertEquals(expectedUsers, updatedUsers)
    }

    @Disabled("Will be fixed with service layer refactor")
    @Test
    fun `updating budgets returns not found for users with no access`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermission(budgetId = existingBudget.id, userId = users[1].id, Permission.OWNER),
        )
        permissions.forEach {
            permissionRepository.save(it)
        }
        val request = BudgetRequest("Update Budget", "An update budget for testing")
        val response = client.put("/api/budgets/${existingBudget.id}") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Disabled("Will be fixed with service layer refactor")
    @Test
    fun `updating non-existent budgets returns not found`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val request = BudgetRequest("Update Budget", "An update budget for testing")
        val response = client.put("/api/budgets/random-budget-id") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Disabled("Will be fixed with service layer refactor")
    @Test
    fun `updating budgets returns forbidden for users with manage access attempting to remove owner`() =
        apiTest { client ->
            val users = listOf(
                User(name = "testuser", password = "testpassword"),
                User(name = "otheruser", password = "otherpassword"),
            )
            users.forEach { userRepository.save(it) }
            val existingBudget =
                budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
            val session = Session(userId = users.first().id)
            sessionRepository.save(session)
            val permissions = setOf(
                UserPermission(budgetId = existingBudget.id, userId = users[0].id, Permission.MANAGE),
                UserPermission(budgetId = existingBudget.id, userId = users[1].id, Permission.OWNER),
            )
            permissions.forEach {
                permissionRepository.save(it)
            }
            val request = BudgetRequest(
                "Update Budget",
                "An update budget for testing",
                setOf(
                    UserPermissionRequest(users[0].id, Permission.OWNER),
                    UserPermissionRequest(users[0].id, Permission.MANAGE),
                )
            )
            val response = client.put("/api/budgets/${existingBudget.id}") {
                header("Authorization", "Bearer ${session.token}")
                header("Content-Type", "application/json")
                setBody(request)
            }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

    @Test
    fun `deleting budgets requires authentication`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val request = BudgetRequest("Update Budget", "A budget for testing")
        val response = client.put("/api/budgets/${existingBudget.id}") {
            header("Content-Type", "application/json")
            setBody(request)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `deleting budgets returns forbidden for users with read only access`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermission(budgetId = existingBudget.id, userId = users[0].id, Permission.READ),
            UserPermission(budgetId = existingBudget.id, userId = users[1].id, Permission.OWNER),
        )
        permissions.forEach {
            permissionRepository.save(it)
        }
        val response = client.delete("/api/budgets/${existingBudget.id}") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `deleting budgets returns forbidden for users with write only access`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermission(budgetId = existingBudget.id, userId = users[0].id, Permission.WRITE),
            UserPermission(budgetId = existingBudget.id, userId = users[1].id, Permission.OWNER),
        )
        permissions.forEach {
            permissionRepository.save(it)
        }
        val response = client.delete("/api/budgets/${existingBudget.id}") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `deleting budgets returns forbidden for users with manage access`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermission(budgetId = existingBudget.id, userId = users[0].id, Permission.MANAGE),
            UserPermission(budgetId = existingBudget.id, userId = users[1].id, Permission.OWNER),
        )
        permissions.forEach {
            permissionRepository.save(it)
        }
        val response = client.delete("/api/budgets/${existingBudget.id}") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `deleting budgets returns success for users with owner access`() = apiTest { client ->
        val users = listOf(
            User(name = "testuser", password = "testpassword"),
            User(name = "otheruser", password = "otherpassword"),
        )
        users.forEach { userRepository.save(it) }
        val existingBudget = budgetRepository.save(Budget(name = "Test Budget", description = "A budget for testing"))
        val session = Session(userId = users.first().id)
        sessionRepository.save(session)
        val permissions = setOf(
            UserPermission(budgetId = existingBudget.id, userId = users[0].id, Permission.OWNER),
            UserPermission(budgetId = existingBudget.id, userId = users[1].id, Permission.OWNER),
        )
        permissions.forEach {
            permissionRepository.save(it)
        }
        val response = client.delete("/api/budgets/${existingBudget.id}") {
            header("Authorization", "Bearer ${session.token}")
            header("Content-Type", "application/json")
        }
        assertEquals(HttpStatusCode.NoContent, response.status)
    }
}