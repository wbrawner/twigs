package com.wbrawner.budgetserver.category

import com.wbrawner.budgetserver.budget.Budget
import com.wbrawner.budgetserver.transaction.Transaction
import javax.persistence.*

@Entity
data class Category(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
        val title: String = "",
        val description: String? = null,
        val amount: Long = 0,
        @ManyToOne val budget: Budget,
        @OneToMany(mappedBy = "category") val transactions: Set<Transaction> = emptySet(),
        val expense: Boolean? = true
) : Comparable<Category> {
    override fun compareTo(other: Category): Int = title.compareTo(other.title)
}

data class CategoryResponse(
        val id: Long,
        val title: String,
        val description: String?,
        val amount: Long,
        val budgetId: Long,
        val expense: Boolean? = true
) {
    constructor(category: Category) : this(
            category.id!!,
            category.title,
            category.description,
            category.amount,
            category.budget.id!!,
            category.expense
    )
}

data class CategoryBalanceResponse(val id: Long, val balance: Long)

data class NewCategoryRequest(
        val title: String,
        val description: String?,
        val amount: Long,
        val budgetId: Long,
        val expense: Boolean? = true
)

data class UpdateCategoryRequest(
        val title: String?,
        val description: String?,
        val amount: Long?,
        val expense: Boolean?
)