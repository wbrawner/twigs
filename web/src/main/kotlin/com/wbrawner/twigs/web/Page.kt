package com.wbrawner.twigs.web

import com.wbrawner.twigs.service.user.UserResponse

interface Page {
    val title: String
    val error: String?
}

interface AuthenticatedPage : Page {
    val user: UserResponse
}

object NotFoundPage : Page {
    override val title: String = "404 Not Found"
    override val error: String? = null
}