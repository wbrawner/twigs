package com.wbrawner.budgetserver.account

import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.transaction.Transaction
import com.wbrawner.budgetserver.user.User
import java.util.*
import javax.persistence.*

@Entity
data class Account(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
        val name: String = "",
        val description: String? = null,
        val currencyCode: String? = null,
        @OneToMany(mappedBy = "account") val transactions: Set<Transaction> = TreeSet(),
        @OneToMany(mappedBy = "account") val categories: Set<Category> = TreeSet(),
        @ManyToMany val users: Set<User> = mutableSetOf(),
        @ManyToOne val owner: User
)

data class NewAccountRequest(val name: String, val description: String?, val userIds: List<Long>)

data class UpdateAccountRequest(val name: String?, val description: String?, val userIds: List<Long>?)

data class AccountResponse(val id: Long, val name: String, val description: String?, val users: List<Long>) {
    constructor(account: Account) : this(account.id!!, account.name, account.description, account.users.map { it.id!! })
}

data class AccountBalanceResponse(val id: Long, val balance: Long)
