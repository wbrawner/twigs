package com.wbrawner.twigs.storage

import com.wbrawner.twigs.randomString
import com.wbrawner.twigs.twoWeeksFromNow
import io.ktor.auth.*
import java.util.*

data class Session(
    val userId: String = "",
    val id: String = randomString(),
    val token: String = randomString(255),
    var expiration: Date = twoWeeksFromNow
) : Principal

