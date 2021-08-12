package com.wbrawner.twigs

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String)