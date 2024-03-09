package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.UserRepository

class FakeUserRepository : FakeRepository<User>(), UserRepository {
    override val entities: MutableList<User> = mutableListOf(TEST_USER, OTHER_USER)

    override fun findAll(nameOrEmail: String, password: String?): List<User> {
        return entities.filter { user ->
            (user.name.equals(nameOrEmail, ignoreCase = true) || user.email.equals(
                nameOrEmail,
                ignoreCase = true
            )) && password?.let { user.password == it } ?: true
        }
    }

    override fun findAll(nameLike: String): List<User> {
        return entities.filter { it.name.contains(nameLike, ignoreCase = true) }
    }

    companion object {
        val TEST_USER = User(
            id = "id-test-user",
            name = "testuser",
            email = "test@example.com",
            password = "testpass"
        )

        val OTHER_USER = User(
            id = "id-other-user",
            name = "otheruser",
            email = "other@example.com",
            password = "otherpass"
        )
    }
}