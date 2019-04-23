package com.wbrawner.budgetserver

import com.wbrawner.budgetserver.user.User
import org.springframework.security.core.context.SecurityContextHolder

fun getCurrentUser(): User? {
    val user = SecurityContextHolder.getContext().authentication.principal
    return if (user is User) user else null
}
