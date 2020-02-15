package com.wbrawner.budgetserver.budget

import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.transaction.Transaction
import com.wbrawner.budgetserver.user.User
import com.wbrawner.budgetserver.user.UserResponse
import java.util.*
import javax.persistence.*

@Entity
data class Budget(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
        val name: String = "",
        val description: String? = null,
        val currencyCode: String? = null,
        @OneToMany(mappedBy = "budget") val transactions: Set<Transaction> = TreeSet(),
        @OneToMany(mappedBy = "budget") val categories: Set<Category> = TreeSet(),
        @ManyToMany val users: Set<User> = mutableSetOf(),
        @JoinColumn(nullable = false)
        @ManyToOne
        val owner: User? = null
)

data class NewBudgetRequest(val name: String, val description: String?, val userIds: Set<Long>)

data class UpdateBudgetRequest(val name: String?, val description: String?, val userIds: Set<Long>?)

data class BudgetResponse(val id: Long, val name: String, val description: String?, val users: List<UserResponse>) {
    constructor(budget: Budget) : this(budget.id!!, budget.name, budget.description, budget.users.map { UserResponse(it) })
}

data class BudgetBalanceResponse(val id: Long, val balance: Long)
