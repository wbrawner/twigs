package com.wbrawner.budgetserver.passwordresetrequest

import com.wbrawner.budgetserver.randomString
import com.wbrawner.budgetserver.user.User
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class PasswordResetRequest(
    @Id
    val id: String = randomString(),
    @field:ManyToOne private val user: User? = null,
    private val date: Calendar = GregorianCalendar(),
    private val token: String = randomString()
)