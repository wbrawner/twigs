package com.wbrawner.budgetserver.transaction

import com.wbrawner.budgetserver.budget.Budget
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
        val expense: Boolean = true,
        @ManyToOne
        @JoinColumn(nullable = false)
        val createdBy: User? = null,
        @ManyToOne
        @JoinColumn(nullable = false)
        val budget: Budget? = null
) : Comparable<Transaction> {
    override fun compareTo(other: Transaction): Int = this.date.compareTo(other.date)
}

data class TransactionResponse(
        val id: Long,
        val title: String,
        val description: String?,
        val date: String,
        val amount: Long,
        val expense: Boolean,
        val budgetId: Long,
        val categoryId: Long?,
        val createdBy: Long
) {
    constructor(transaction: Transaction) : this(
            transaction.id!!,
            transaction.title,
            transaction.description,
            transaction.date.toString(),
            transaction.amount,
            transaction.expense,
            transaction.budget!!.id!!,
            if (transaction.category != null) transaction.category.id!! else null,
            transaction.createdBy!!.id!!
    )
}

data class NewTransactionRequest(
        val title: String,
        val description: String?,
        val date: String,
        val amount: Long,
        val categoryId: Long?,
        val expense: Boolean,
        val budgetId: Long
)

data class UpdateTransactionRequest(
        val title: String?,
        val description: String?,
        val date: String?,
        val amount: Long?,
        val categoryId: Long?,
        val expense: Boolean?,
        val budgetId: Long?,
        val createdBy: Long?
)