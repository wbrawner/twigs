package com.wbrawner.budgetserver

import com.wbrawner.budgetserver.user.User
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

fun getCurrentUser(): User? {
    val user = SecurityContextHolder.getContext().authentication.principal
    return if (user is User) user else null
}

fun GregorianCalendar.setToFirstOfMonth(): GregorianCalendar = this.apply {
    for (field in arrayOf(Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR_OF_DAY, Calendar.DATE)) {
        set(field, getActualMinimum(field))
    }
}

fun GregorianCalendar.setToEndOfMonth(): GregorianCalendar = this.apply {
    for (field in arrayOf(Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR_OF_DAY, Calendar.DATE)) {
        set(field, getActualMaximum(field))
    }
}