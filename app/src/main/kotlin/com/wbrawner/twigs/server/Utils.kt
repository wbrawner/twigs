package com.wbrawner.twigs.server

import com.wbrawner.twigs.server.budget.Budget
import com.wbrawner.twigs.server.permission.Permission
import com.wbrawner.twigs.server.permission.UserPermissionRepository
import com.wbrawner.twigs.server.transaction.TransactionRepository
import com.wbrawner.twigs.server.user.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

private val CALENDAR_FIELDS = intArrayOf(
        Calendar.MILLISECOND,
        Calendar.SECOND,
        Calendar.MINUTE,
        Calendar.HOUR_OF_DAY,
        Calendar.DATE
)

val firstOfMonth: Date
    get() = GregorianCalendar().run {
        for (calField in CALENDAR_FIELDS) {
            set(calField, getActualMinimum(calField))
        }
        time
    }

val endOfMonth: Date
    get() = GregorianCalendar().run {
        for (calField in CALENDAR_FIELDS) {
            set(calField, getActualMaximum(calField))
        }
        time
    }

val twoWeeksFromNow: Date
    get() = GregorianCalendar().run {
        add(Calendar.DATE, 14)
        time
    }

val currentUser: User?
    get() = SecurityContextHolder.getContext().authentication.principal as? User

private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

fun randomString(length: Int = 32): String {
    val id = StringBuilder()
    for (i in 0 until length) {
        id.append(CHARACTERS.random())
    }
    return id.toString()
}

fun <T> getBudgetWithPermission(
    transactionRepository: TransactionRepository,
    userPermissionsRepository: UserPermissionRepository,
    transactionId: String,
    permission: Permission,
    action: (Budget) -> ResponseEntity<T>
): ResponseEntity<T> {
    val transaction = transactionRepository.findById(transactionId).orElse(null)
        ?: return ResponseEntity.notFound().build()
    val userPermission = userPermissionsRepository.findByUserAndBudget_Id(
        currentUser,
        transaction.budget!!.id
    ).orElse(null)
        ?: return ResponseEntity.notFound().build()
    if (userPermission.permission.isNotAtLeast(permission)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }
    return action(userPermission.budget!!)
}