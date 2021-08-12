package com.wbrawner.twigs.model

import com.wbrawner.twigs.Identifiable
import com.wbrawner.twigs.randomString
import java.time.Instant

data class Transaction(
    override val id: String = randomString(),
    val title: String,
    val description: String? = null,
    val date: Instant,
    val amount: Long,
    val expense: Boolean,
    val createdBy: String,
    val categoryId: String? = null,
    val budgetId: String
) : Identifiable
