package com.wbrawner.budgetserver

import com.wbrawner.budgetserver.user.User
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

fun getCurrentUser(): User? {
    val user = SecurityContextHolder.getContext().authentication.principal
    return if (user is User) user else null
}

fun GregorianCalendar.setToFirstOfMonth(): GregorianCalendar = this.apply {
    set(Calendar.MILLISECOND, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.DATE, 1)
}