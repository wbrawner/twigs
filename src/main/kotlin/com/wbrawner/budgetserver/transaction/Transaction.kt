package com.wbrawner.budgetserver.transaction

import com.wbrawner.budgetserver.account.Account
import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.user.User
import java.time.Instant
import javax.persistence.*

@Entity
data class Transaction(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
        val title: String = "",
        val description: String? = null,
        val date: Instant = Instant.now(),
        val amount: Long = 0,
        @ManyToOne val category: Category? = null,
        val isExpense: Boolean = true,
        @ManyToOne val createdBy: User,
        @ManyToOne val account: Account
) : Comparable<Transaction> {
    override fun compareTo(other: Transaction): Int = this.date.compareTo(other.date)
}

data class TransactionResponse(
        val id: Long,
        val title: String,
        val description: String?,
        val date: String,
        val amount: Long,
        val isExpense: Boolean,
        val accountId: Long,
        val categoryId: Long?,
        val createdBy: Long
) {
    constructor(transaction: Transaction) : this(
            transaction.id!!,
            transaction.title,
            transaction.description,
            transaction.date.toString(),
            transaction.amount,
            transaction.isExpense,
            transaction.account.id!!,
            if (transaction.category != null) transaction.category.id!! else null,
            transaction.createdBy.id!!
    )
}

data class NewTransactionRequest(
        val title: String,
        val description: String?,
        val date: String,
        val amount: Long,
        val categoryId: Long?,
        val isExpense: Boolean,
        val accountId: Long
)

data class UpdateTransactionRequest(
        val title: String?,
        val description: String?,
        val date: String?,
        val amount: Long?,
        val categoryId: Long?,
        val isExpense: Boolean?,
        val accountId: Long?,
        val createdBy: Long?
)