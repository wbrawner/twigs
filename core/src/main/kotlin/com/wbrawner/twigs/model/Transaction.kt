package com.wbrawner.twigs.model

import com.wbrawner.twigs.randomString
import java.time.Instant

data class Transaction(
    val id: String = randomString(),
    val title: String? = null,
    val description: String? = null,
    val date: Instant? = null,
    val amount: Long? = null,
    val categoryId: String? = null,
    val expense: Boolean? = null,
    val createdBy: String,
    val budgetId: String
)
