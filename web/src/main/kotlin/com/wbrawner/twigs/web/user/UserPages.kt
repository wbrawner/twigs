package com.wbrawner.twigs.web.user

import com.wbrawner.twigs.web.Page

data class LoginPage(val username: String = "", override val error: String? = null) : Page {
    override val title: String = "Login"
}

data class RegisterPage(val username: String = "", val email: String = "", override val error: String? = null) : Page {
    override val title: String = "Register"
}