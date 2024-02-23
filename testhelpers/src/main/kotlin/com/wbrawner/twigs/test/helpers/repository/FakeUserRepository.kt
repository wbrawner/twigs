package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.UserRepository

class FakeUserRepository : FakeRepository<User>(), UserRepository {
    override val entities: MutableList<User> = mutableListOf(
            User(
                name = "testuser",
                email = "test@example.com",
                password = "\$2a\$10\$bETxbFPja1PyXVLybETxb.CWBYzyYdZpmCcA7NSIN8dkdzidt1Xv2" // testpass
            ),
            User(
                name = "otheruser",
                email = "other@example.com",
                password = "\$2a\$10\$bETxbFPja1PyXVLybETxb..rhfIeOkP4qil1Drj29LDUhBxVkm6fS"
            ),
    )

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