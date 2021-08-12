package com.wbrawner.twigs.db

import com.wbrawner.twigs.randomString

data class DatabaseMetadata(
    val version: Int = 0,
    val salt: String = randomString(16)
)