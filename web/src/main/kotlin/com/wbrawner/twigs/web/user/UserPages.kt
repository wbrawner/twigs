package com.wbrawner.twigs.web.user

import com.wbrawner.twigs.web.Page

data class LoginPage(val username: String = "", val error: String? = null) : Page("Login")

data class RegisterPage(val username: String = "", val email: String = "", val error: String? = null) : Page("Register")