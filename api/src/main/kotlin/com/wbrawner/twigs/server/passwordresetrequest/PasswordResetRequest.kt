package com.wbrawner.twigs.server.passwordresetrequest

import com.wbrawner.twigs.server.randomString
import com.wbrawner.twigs.server.user.User
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