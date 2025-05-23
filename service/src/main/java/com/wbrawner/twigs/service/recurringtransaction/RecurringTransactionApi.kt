package com.wbrawner.twigs.service.recurringtransaction

import com.wbrawner.twigs.model.RecurringTransaction
import kotlinx.serialization.Serializable
import java.time.temporal.ChronoUnit

@Serializable
data class RecurringTransactionRequest(
    val title: String? = null,
    val description: String? = null,
    val amount: Long? = null,
    val categoryId: String? = null,
    val expense: Boolean? = null,
    val budgetId: String? = null,
    val frequency: String,
    val start: String? = null,
    val finish: String? = null,
)

@Serializable
data class RecurringTransactionResponse(
    val id: String,
    val title: String?,
    val description: String?,
    val frequency: String,
    val start: String,
    val finish: String?,
    val lastRun: String?,
    val amount: Long?,
    val expense: Boolean?,
    val budgetId: String,
    val categoryId: String?,
    val createdBy: String
)

fun RecurringTransaction.asResponse(): RecurringTransactionResponse = RecurringTransactionResponse(
    id = id,
    title = title,
    description = description,
    frequency = frequency.toString(),
    start = start.truncatedTo(ChronoUnit.SECONDS).toString(),
    finish = finish?.truncatedTo(ChronoUnit.SECONDS)?.toString(),
    lastRun = lastRun?.truncatedTo(ChronoUnit.SECONDS)?.toString(),
    amount = amount,
    expense = expense,
    budgetId = budgetId,
    categoryId = categoryId,
    createdBy = createdBy
)