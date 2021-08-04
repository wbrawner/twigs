package com.wbrawner.twigs

import com.wbrawner.twigs.model.Category

data class CategoryRequest(
    val title: String? = null,
    val description: String? = null,
    val amount: Long? = null,
    val budgetId: String? = null,
    val expense: Boolean? = null,
    val archived: Boolean? = null
)

data class CategoryResponse(
    val id: String,
    val title: String,
    val description: String?,
    val amount: Long,
    val budgetId: String,
    val isExpense: Boolean,
    val isArchived: Boolean
) {
    constructor(category: Category) : this(
        category.id,
        category.title,
        category.description,
        category.amount,
        category.budget!!.id,
        category.expense,
        category.archived
    )
}

data class CategoryBalanceResponse(val id: String, val balance: Long)