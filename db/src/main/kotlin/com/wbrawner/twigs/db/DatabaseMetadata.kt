package com.wbrawner.twigs.db

data class DatabaseMetadata(
    val version: Int = 0,
    val salt: String = ""
)