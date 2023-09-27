package com.wbrawner.twigs.test.helpers

import com.wbrawner.twigs.EmailService
import com.wbrawner.twigs.model.PasswordResetToken

class FakeEmailService : EmailService {
    val emails = mutableListOf<FakeEmail<*>>()

    override fun sendPasswordResetEmail(token: PasswordResetToken, to: String) {
        emails.add(FakeEmail(to, token))
    }
}

data class FakeEmail<Data>(val to: String, val data: Data)