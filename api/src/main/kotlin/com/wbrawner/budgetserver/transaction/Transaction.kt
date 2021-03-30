package com.wbrawner.budgetserver.transaction

import com.wbrawner.budgetserver.budget.Budget
import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.randomString
import com.wbrawner.budgetserver.user.User
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class Transaction(
    @Id
    val id: String = randomString(),
    var title: String? = null,
    var description: String? = null,
    var date: Instant? = null,
    var amount: Long? = null,
    @field:ManyToOne var category: Category? = null,
    var expense: Boolean? = null,
    @field:JoinColumn(nullable = false) @field:ManyToOne val createdBy: User? = null,
    @field:JoinColumn(nullable = false) @field:ManyToOne var budget: Budget? = null
)

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