package com.wbrawner.budgetserver.passwordresetrequest

import com.wbrawner.budgetserver.user.User
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class PasswordResetRequest(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
        val user: User? = null,
        val date: Calendar = GregorianCalendar(),
        val token: String = UUID.randomUUID().toString().replace("-", "")
)