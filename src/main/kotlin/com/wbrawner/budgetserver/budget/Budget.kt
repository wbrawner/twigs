package com.wbrawner.budgetserver.budget

import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.permission.UserPermission
import com.wbrawner.budgetserver.permission.UserPermissionRequest
import com.wbrawner.budgetserver.permission.UserPermissionResponse
import com.wbrawner.budgetserver.transaction.Transaction
import java.util.*
import javax.persistence.*

@Entity
data class Budget(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
        val name: String = "",
        val description: String? = null,
        val currencyCode: String? = null,
        @OneToMany(mappedBy = "budget") val transactions: MutableSet<Transaction> = TreeSet(),
        @OneToMany(mappedBy = "budget") val categories: MutableSet<Category> = TreeSet(),
        @OneToMany(mappedBy = "budget") val users: MutableSet<UserPermission> = mutableSetOf()
)

data class NewBudgetRequest(val name: String, val description: String?, val users: Set<UserPermissionRequest>)

data class UpdateBudgetRequest(val name: String?, val description: String?, val users: Set<UserPermissionRequest>?)

data class BudgetResponse(val id: Long, val name: String, val description: String?, val users: List<UserPermissionResponse>) {
    constructor(budget: Budget, users: List<UserPermission>) : this(
            budget.id!!,
            budget.name,
            budget.description,
            users.map { UserPermissionResponse(it) }
    )
}

data class BudgetBalanceResponse(val id: Long, val balance: Long)
