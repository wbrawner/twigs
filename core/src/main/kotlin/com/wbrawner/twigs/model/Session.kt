package com.wbrawner.twigs.model

import com.wbrawner.twigs.Identifiable
import com.wbrawner.twigs.randomString
import com.wbrawner.twigs.twoWeeksFromNow
import io.ktor.server.auth.*
import java.time.Instant

open class Session(
    override val id: String = randomString(),
    val userId: String = "",
    open val token: String = randomString(255),
    val expiration: Instant = twoWeeksFromNow
) : Principal, Identifiable {
    fun updateExpiration(newExpiration: Instant) = Session(
        id = id,
        userId = userId,
        token = token,
        expiration = newExpiration
    )
}

data class HeaderSession(override val token: String) : Session(token = token)

data class CookieSession(override val token: String) : Session(token = token)