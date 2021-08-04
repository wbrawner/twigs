package com.wbrawner.twigs.model

import com.wbrawner.twigs.randomString

data class User(
    val id: String = randomString(),
    val name: String = "",
    val password: String = "",
    val email: String? = null
)

enum class Permission {
    /**
     * The user can read the content but cannot make any modifications.
     */
    READ,

    /**
     * The user can read and write the content but cannot make any modifications to the container of the content.
     */
    WRITE,

    /**
     * The user can read and write the content, and make modifications to the container of the content including things like name, description, and other users' permissions (with the exception of the owner user, whose role can never be removed by a user with only MANAGE permissions).
     */
    MANAGE,

    /**
     * The user has complete control over the resource. There can only be a single owner user at any given time.
     */
    OWNER;

    fun isAtLeast(wanted: Permission): Boolean {
        return ordinal >= wanted.ordinal
    }

    fun isNotAtLeast(wanted: Permission): Boolean {
        return ordinal < wanted.ordinal
    }
}

data class UserPermission(
    val budgetId: String,
    val userId: String,
    val permission: Permission = Permission.READ
)