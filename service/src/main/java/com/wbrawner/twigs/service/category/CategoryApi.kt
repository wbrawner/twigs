package com.wbrawner.twigs.service.category

import com.wbrawner.twigs.model.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryRequest(
    val title: String? = null,
    val description: String? = null,
    val amount: Long? = null,
    val budgetId: String? = null,
    val expense: Boolean? = null,
    val archived: Boolean? = null
)

@Serializable
data class CategoryResponse(
    val id: String,
    val title: String,
    val description: String?,
    val amount: Long,
    val budgetId: String,
    val expense: Boolean,
    val archived: Boolean
)

fun Category.asResponse(): CategoryResponse = CategoryResponse(
    id,
    title,
    description,
    amount,
    budgetId,
    expense,
    archived
)