package com.wbrawner.twigs

import com.wbrawner.twigs.model.Transaction

data class NewTransactionRequest(
    val title: String,
    val description: String? = null,
    val date: String,
    val amount: Long,
    val categoryId: String? = null,
    val expense: Boolean,
    val budgetId: String
)

data class UpdateTransactionRequest(
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val amount: Long? = null,
    val categoryId: String? = null,
    val expense: Boolean? = null,
    val budgetId: String? = null,
    val createdBy: String? = null
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
) {
    constructor(transaction: Transaction) : this(
        transaction.id,
        transaction.title,
        transaction.description,
        transaction.date.toString(),
        transaction.amount,
        transaction.expense,
        transaction.budget!!.id,
        transaction.category?.id,
        transaction.createdBy!!.id
    )
}