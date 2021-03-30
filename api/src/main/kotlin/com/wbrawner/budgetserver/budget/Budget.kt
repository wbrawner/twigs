package com.wbrawner.budgetserver.budget

import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.permission.UserPermission
import com.wbrawner.budgetserver.permission.UserPermissionRequest
import com.wbrawner.budgetserver.permission.UserPermissionResponse
import com.wbrawner.budgetserver.randomString
import com.wbrawner.budgetserver.transaction.Transaction
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class Budget(
    @Id
    var id: String = randomString(),
    var name: String? = null,
    var description: String? = null,
    var currencyCode: String? = "USD",
    @OneToMany(mappedBy = "budget")
    val transactions: Set<Transaction> = TreeSet(),
    @OneToMany(mappedBy = "budget")
    val categories: Set<Category> = TreeSet(),
    @OneToMany(mappedBy = "budget")
    val users: Set<Transaction> = HashSet()
)

data class BudgetRequest(
    val name: String = "",
    val description: String = "",
    val users: Set<UserPermissionRequest> = emptySet()
)

data class BudgetResponse(
    val id: String,
    val name: String?,
    val description: String?,
    private val users: List<UserPermissionResponse>
) {
    constructor(budget: Budget, users: List<UserPermission>) : this(
        Objects.requireNonNull<String>(budget.id),
        budget.name,
        budget.description,
        users.map { userPermission: UserPermission -> UserPermissionResponse(userPermission) }
    )
}

data class BudgetBalanceResponse(val id: String, val balance: Long)