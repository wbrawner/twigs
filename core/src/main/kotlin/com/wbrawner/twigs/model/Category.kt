package com.wbrawner.twigs.model

import com.wbrawner.twigs.randomString

data class Category(
    val id: String = randomString(),
    var title: String = "",
    var description: String? = null,
    var amount: Long = 0L,
    var budget: Budget? = null,
    var expense: Boolean = true,
    var archived: Boolean = false
)
