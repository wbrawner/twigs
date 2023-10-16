package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.UserRepository

class FakeUserRepository : FakeRepository<User>(), UserRepository {
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
}