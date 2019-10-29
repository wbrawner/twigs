package com.wbrawner.budgetserver.budget

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
@RequestMapping("/budgets")
@Api(value = "Budgets", tags = ["Budgets"], authorizations = [Authorization("basic")])
class BudgetController @Autowired constructor(
        private val budgetRepository: BudgetRepository,
        private val transactionRepository: TransactionRepository,
        private val userRepository: UserRepository
) {
    @Transactional
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getBudgets", nickname = "getBudgets", tags = ["Budgets"])
    fun getBudgets(): ResponseEntity<List<BudgetResponse>> = ResponseEntity.ok(
            budgetRepository.findAllByUsersContainsOrOwner(getCurrentUser()!!).map {
                Hibernate.initialize(it.users)
                BudgetResponse(it)
            }
    )

    @Transactional
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getBudget", nickname = "getBudget", tags = ["Budgets"])
    fun getBudget(@PathVariable id: Long): ResponseEntity<BudgetResponse> = budgetRepository.findByUsersContainsAndId(getCurrentUser()!!, id)
            .orElse(null)
            ?.let {
                Hibernate.initialize(it.users)
                ResponseEntity.ok(BudgetResponse(it))
            } ?: ResponseEntity.notFound().build()

    @Transactional
    @GetMapping("/{id}/balance", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getBudgetBalance", nickname = "getBudgetBalance", tags = ["Budgets"])
    fun getBudgetBalance(@PathVariable id: Long): ResponseEntity<BudgetBalanceResponse> =
            budgetRepository.findByUsersContainsAndId(getCurrentUser()!!, id)
                    .orElse(null)
                    ?.let {
                        ResponseEntity.ok(BudgetBalanceResponse(it.id!!, transactionRepository.sumBalanceByBudgetId(it.id)))
                    } ?: ResponseEntity.notFound().build()

    @PostMapping("/new", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "newBudget", nickname = "newBudget", tags = ["Budgets"])
    fun newBudget(@RequestBody request: NewBudgetRequest): ResponseEntity<BudgetResponse> {
        val users = request.userIds
                .map { id -> userRepository.findById(id).orElse(null) }
                .filterNotNull()
                .toMutableSet()
                .apply { this.add(getCurrentUser()!!) }
        val budget = budgetRepository.save(Budget(name = request.name, description = request.description, users = users, owner = getCurrentUser()!!))
        return ResponseEntity.ok(BudgetResponse(budget))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "updateBudget", nickname = "updateBudget", tags = ["Budgets"])
    fun updateBudget(@PathVariable id: Long, request: UpdateBudgetRequest): ResponseEntity<BudgetResponse> {
        var budget = budgetRepository.findByUsersContainsAndId(getCurrentUser()!!, id).orElse(null)
                ?: return ResponseEntity.notFound().build()
        if (request.name != null) budget = budget.copy(name = request.name)
        if (request.description != null) budget = budget.copy(description = request.description)
        if (request.userIds != null) budget = budget.copy(users = userRepository.findAllById(request.userIds).toSet())
        return ResponseEntity.ok(BudgetResponse(budgetRepository.save(budget)))
    }

    @DeleteMapping("/{id}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "deleteBudget", nickname = "deleteBudget", tags = ["Budgets"])
    fun deleteBudget(@PathVariable id: Long): ResponseEntity<Unit> {
        val budget = budgetRepository.findByUsersContainsAndId(getCurrentUser()!!, id).orElse(null)
                ?: return ResponseEntity.notFound().build()
        budgetRepository.delete(budget)
        return ResponseEntity.ok().build()
    }
}