package com.wbrawner.twigs

import io.ktor.auth.*
import java.util.*

data class Session(
    val userId: String = "",
    val id: String = randomString(),
    val token: String = randomString(255),
    var expiration: Date = twoWeeksFromNow
) : Principal

