package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.UserRepository

class FakeUserRepository : FakeRepository<User>(), UserRepository {
    override fun findAll(nameOrEmail: String, password: String?): List<User> {
        return entities.filter {
            (it.name.equals(nameOrEmail, ignoreCase = true) || it.email.equals(
                nameOrEmail,
                ignoreCase = true
            )) && it.password == password
        }
    }

    override fun findAll(nameLike: String): List<User> {
        return entities.filter { it.name.contains(nameLike, ignoreCase = true) }
    }
}