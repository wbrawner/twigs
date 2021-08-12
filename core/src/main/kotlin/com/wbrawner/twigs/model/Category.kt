package com.wbrawner.twigs.model

import com.wbrawner.twigs.Identifiable
import com.wbrawner.twigs.randomString

data class Category(
    override val id: String = randomString(),
    val title: String,
    val amount: Long,
    val budgetId: String,
    val description: String? = null,
    val expense: Boolean = true,
    val archived: Boolean = false
) : Identifiable
