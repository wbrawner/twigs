package com.wbrawner.budgetserver.account

import com.wbrawner.budgetserver.getCurrentUser
import com.wbrawner.budgetserver.transaction.TransactionRepository
import com.wbrawner.budgetserver.user.UserRepository
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.Authorization
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@RestController
@RequestMapping("/accounts")
@Api(value = "Accounts", tags = ["Accounts"], authorizations = [Authorization("basic")])
class AccountController @Autowired constructor(
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository,
        private val userRepository: UserRepository
) {
    @Transactional
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getAccounts", nickname = "getAccounts", tags = ["Accounts"])
    fun getAccounts(): ResponseEntity<List<AccountResponse>> = ResponseEntity.ok(
            accountRepository.findAllByUsersContainsOrOwner(getCurrentUser()!!).map {
                Hibernate.initialize(it.users)
                AccountResponse(it)
            }
    )

    @Transactional
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getAccount", nickname = "getAccount", tags = ["Accounts"])
    fun getAccount(@PathVariable id: Long): ResponseEntity<AccountResponse> = accountRepository.findByUsersContainsAndId(getCurrentUser()!!, id)
            .orElse(null)
            ?.let {
                Hibernate.initialize(it.users)
                ResponseEntity.ok(AccountResponse(it))
            } ?: ResponseEntity.notFound().build()

    @Transactional
    @GetMapping("/{id}/balance", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getAccountBalance", nickname = "getAccountBalance", tags = ["Accounts"])
    fun getAccountBalance(@PathVariable id: Long): ResponseEntity<AccountBalanceResponse> =
            accountRepository.findByUsersContainsAndId(getCurrentUser()!!, id)
                    .orElse(null)
                    ?.let {
                        ResponseEntity.ok(AccountBalanceResponse(it.id!!, transactionRepository.sumBalanceByAccountId(it.id)))
                    } ?: ResponseEntity.notFound().build()

    @PostMapping("/new", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "newAccount", nickname = "newAccount", tags = ["Accounts"])
    fun newAccount(@RequestBody request: NewAccountRequest): ResponseEntity<AccountResponse> {
        val users = request.userIds
                .map { id -> userRepository.findById(id).orElse(null) }
                .filter { user -> user != null }
                .toMutableSet()
                .apply { this.add(getCurrentUser()) }
        val account = accountRepository.save(Account(name = request.name, description = request.description, users = users, owner = getCurrentUser()!!))
        return ResponseEntity.ok(AccountResponse(account))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "updateAccount", nickname = "updateAccount", tags = ["Accounts"])
    fun updateAccount(@PathVariable id: Long, request: UpdateAccountRequest): ResponseEntity<AccountResponse> {
        var account = accountRepository.findByUsersContainsAndId(getCurrentUser()!!, id).orElse(null) ?: return ResponseEntity.notFound().build()
        if (request.name != null) account = account.copy(name = request.name)
        if (request.description != null) account = account.copy(description = request.description)
        if (request.userIds != null) account = account.copy(users = userRepository.findAllById(request.userIds).toSet())
        return ResponseEntity.ok(AccountResponse(accountRepository.save(account)))
    }

    @DeleteMapping("/{id}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "deleteAccount", nickname = "deleteAccount", tags = ["Accounts"])
    fun deleteAccount(@PathVariable id: Long): ResponseEntity<Unit> {
        val account = accountRepository.findByUsersContainsAndId(getCurrentUser()!!, id).orElse(null) ?: return ResponseEntity.notFound().build()
        accountRepository.delete(account)
        return ResponseEntity.ok().build()
    }
}