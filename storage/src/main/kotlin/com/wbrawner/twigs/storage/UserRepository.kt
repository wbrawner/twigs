package com.wbrawner.twigs.storage

import com.wbrawner.twigs.model.User

interface UserRepository : Repository<User> {
    fun findAll(
        ids: List<String>? = null,
    ): List<User>

    fun find(
        name: String? = null,
        email: String? = null,
        password: String? = null
    ): List<User>

    fun findAll(nameLike: String): List<User>

    fun deleteById(id: String): Boolean
}