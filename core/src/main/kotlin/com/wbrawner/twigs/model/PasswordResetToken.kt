package com.wbrawner.twigs.model

import com.wbrawner.twigs.Identifiable
import com.wbrawner.twigs.randomString
import com.wbrawner.twigs.tomorrow
import java.time.Instant

data class PasswordResetToken(
    override val id: String = randomString(),
    val userId: String = "",
    var expiration: Instant = tomorrow
) : Identifiable
