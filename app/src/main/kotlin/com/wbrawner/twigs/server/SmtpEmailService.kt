package com.wbrawner.twigs.server

import com.wbrawner.twigs.EmailService
import com.wbrawner.twigs.model.PasswordResetToken
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


class SmtpEmailService(
    val from: String?,
    val host: String?,
    val port: Int?,
    val username: String?,
    val password: String?
) : EmailService {
    private val canSendEmail = !from.isNullOrBlank()
            && !host.isNullOrBlank()
            && port != null
            && !username.isNullOrBlank()
            && !password.isNullOrBlank()

    private val session = Session.getInstance(
        Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", host ?: "")
            put("mail.smtp.port", port ?: 25)
            put("mail.smtp.from", from ?: "")
        },
        object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

    override fun sendPasswordResetEmail(token: PasswordResetToken, to: String) {
        val resetUrl = "twigs://resetpassword?token=${token.id}"
        val plainText = javaClass.getResource("/email/plain/passwordreset.txt")
            ?.readText()
            ?.replace("{reset_url}", resetUrl)
        val html = javaClass.getResource("/email/html/passwordreset.html")
            ?.readText()
            ?.replace("{reset_url}", resetUrl)
        sendEmail(
            plainText,
            html,
            to,
            "Twigs Password Reset" // TODO: Localization
        )
    }

    private fun sendEmail(plainText: String?, html: String?, to: String, subject: String) {
        if (!canSendEmail) return
        if (plainText.isNullOrBlank() && html.isNullOrBlank()) return
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(from, "Twigs"))
        message.setRecipients(Message.RecipientType.TO, to)
        val multipart: Multipart = MimeMultipart("alternative").apply {
            plainText?.let {
                addBodyPart(it.asMimeBodyPart("text/plain; charset=utf-8"))
            }
            html?.let {
                addBodyPart(it.asMimeBodyPart("text/html; charset=utf-8"))
            }
        }
        message.setContent(multipart)
        message.subject = subject
        Transport.send(message)
    }

    private fun String.asMimeBodyPart(mimeType: String): MimeBodyPart = MimeBodyPart().apply {
        setContent(this@asMimeBodyPart, mimeType)
    }
}