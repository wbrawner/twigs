package com.wbrawner.twigs

import com.wbrawner.twigs.model.PasswordResetToken

interface EmailService {
    fun sendPasswordResetEmail(token: PasswordResetToken, to: String)
}