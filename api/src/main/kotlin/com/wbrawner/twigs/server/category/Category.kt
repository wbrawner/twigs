package com.wbrawner.twigs.server.category

import com.wbrawner.twigs.server.budget.Budget
import com.wbrawner.twigs.server.randomString
import javax.persistence.*

@Entity
data class Category(
    @Id
    val id: String = randomString(),
    var title: String= "",
    var description: String? = null,
    var amount: Long = 0L,
    @field:ManyToOne
    @field:JoinColumn(nullable = false)
    var budget: Budget? = null,
    var expense: Boolean = true,
    @field:Column(nullable = false, columnDefinition = "boolean default false")
    var archived: Boolean = false
)

data class NewCategoryRequest(
    val title: String,
    val description: String? = null,
    val amount: Long,
    val budgetId: String,
    val expense: Boolean
)

data class UpdateCategoryRequest(
    val title: String? = null,
    val description: String? = null,
    val amount: Long? = null,
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