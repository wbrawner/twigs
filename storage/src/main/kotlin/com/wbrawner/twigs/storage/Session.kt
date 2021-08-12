package com.wbrawner.twigs.storage

import com.wbrawner.twigs.Identifiable
import com.wbrawner.twigs.randomString
import com.wbrawner.twigs.twoWeeksFromNow
import io.ktor.auth.*
import java.time.Instant

data class Session(
    override val id: String = randomString(),
    val userId: String = "",
    val token: String = randomString(255),
    var expiration: Instant = twoWeeksFromNow
) : Principal, Identifiable
