package com.wbrawner.twigs.storage

import com.wbrawner.twigs.model.User

interface UserRepository : Repository<User> {
    fun findAll(
        nameOrEmail: String,
        password: String? = null
    ): List<User>

    fun findAll(nameLike: String): List<User>
}