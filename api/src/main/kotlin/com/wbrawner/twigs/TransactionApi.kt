package com.wbrawner.twigs

import com.wbrawner.twigs.model.Transaction

data class TransactionRequest(
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val amount: Long? = null,
    val categoryId: String? = null,
    val expense: Boolean? = null,
    val budgetId: String? = null,
)

data class TransactionResponse(
    val id: String,
    val title: String?,
    val description: String?,
    val date: String,
    val amount: Long?,
    val expense: Boolean?,
    val budgetId: String,
    val categoryId: String?,
    val createdBy: String
)

data class BalanceResponse(val balance: Long)

fun Transaction.asResponse(): TransactionResponse = TransactionResponse(
    id = id,
    title = title,
    description = description,
    date = date.toString(),
    amount = amount,
    expense = expense,
    budgetId = budgetId,
    categoryId = categoryId,
    createdBy = createdBy
)