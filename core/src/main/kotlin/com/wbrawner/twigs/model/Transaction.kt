package com.wbrawner.twigs.model

import com.wbrawner.twigs.randomString
import java.time.Instant

data class Transaction(
    val id: String = randomString(),
    val title: String? = null,
    val description: String? = null,
    val date: Instant? = null,
    val amount: Long? = null,
    val category: Category? = null,
    val expense: Boolean? = null,
    val createdBy: User? = null,
    val budget: Budget? = null
)
