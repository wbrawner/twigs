package com.wbrawner.budgetserver.budget

import com.wbrawner.budgetserver.getCurrentUser
import com.wbrawner.budgetserver.permission.Permission
import com.wbrawner.budgetserver.permission.UserPermission
import com.wbrawner.budgetserver.permission.UserPermissionRepository
import com.wbrawner.budgetserver.transaction.TransactionRepository
import com.wbrawner.budgetserver.user.UserRepository
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.Authorization
import org.hibernate.Hibernate
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@RestController
@RequestMapping("/budgets")
@Api(value = "Budgets", tags = ["Budgets"], authorizations = [Authorization("basic")])
@Transactional
open class BudgetController(
        private val budgetRepository: BudgetRepository,
        private val transactionRepository: TransactionRepository,
        private val userRepository: UserRepository,
        private val userPermissionsRepository: UserPermissionRepository
) {
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getBudgets", nickname = "getBudgets", tags = ["Budgets"])
    open fun getBudgets(page: Int?, count: Int?): ResponseEntity<List<BudgetResponse>> = ResponseEntity.ok(
            userPermissionsRepository.findAllByUser(
                    user = getCurrentUser()!!,
                    pageable = PageRequest.of(page ?: 0, count ?: 1000))
                    .map {
                        Hibernate.initialize(it.budget)
                        BudgetResponse(it.budget!!, userPermissionsRepository.findAllByUserAndBudget(getCurrentUser()!!, it.budget, null))
                    }
    )

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getBudget", nickname = "getBudget", tags = ["Budgets"])
    open fun getBudget(@PathVariable id: Long): ResponseEntity<BudgetResponse> = userPermissionsRepository.findAllByUserAndBudget_Id(getCurrentUser()!!, id, null)
            .firstOrNull()
            ?.budget
            ?.let {
                ResponseEntity.ok(BudgetResponse(it, userPermissionsRepository.findAllByBudget(it, null)))
            }
            ?: ResponseEntity.notFound().build()

    @GetMapping("/{id}/balance", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getBudgetBalance", nickname = "getBudgetBalance", tags = ["Budgets"])
    open fun getBudgetBalance(@PathVariable id: Long): ResponseEntity<BudgetBalanceResponse> =
            userPermissionsRepository.findAllByUserAndBudget_Id(getCurrentUser()!!, id, null)
                    .firstOrNull()
                    ?.budget
                    ?.let {
                        ResponseEntity.ok(BudgetBalanceResponse(it.id!!, transactionRepository.sumBalanceByBudgetId(it.id)))
                    } ?: ResponseEntity.notFound().build()

    @PostMapping("/new", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "newBudget", nickname = "newBudget", tags = ["Budgets"])
    open fun newBudget(@RequestBody request: NewBudgetRequest): ResponseEntity<BudgetResponse> {
        val budget = budgetRepository.save(Budget(name = request.name, description = request.description))
        val users = request.users
                .mapNotNull {
                    userRepository.findById(it.user).orElse(null)?.let { user ->
                        userPermissionsRepository.save(
                                UserPermission(budget = budget, user = user, permission = it.permission)
                        )
                    }
                }
                .toMutableSet()
        if (users.firstOrNull { it.user?.id == getCurrentUser()!!.id } == null) {
            users.add(
                    userPermissionsRepository.save(
                            UserPermission(budget = budget, user = getCurrentUser(), permission = Permission.OWNER)
                    )
            )
        }
        return ResponseEntity.ok(BudgetResponse(budget, users.toList()))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "updateBudget", nickname = "updateBudget", tags = ["Budgets"])
    open fun updateBudget(@PathVariable id: Long, request: UpdateBudgetRequest): ResponseEntity<BudgetResponse> {
        var budget = userPermissionsRepository.findAllByUserAndBudget_Id(getCurrentUser()!!, id, null)
                .firstOrNull()
                ?.budget
                ?: return ResponseEntity.notFound().build()
        request.name?.let {
            budget = budget.copy(name = it)
        }
        request.description?.let {
            budget = budget.copy(description = request.description)
        }
        val users = request.users?.mapNotNull { req ->
            userRepository.findById(req.user).orElse(null)?.let {
                userPermissionsRepository.save(UserPermission(budget = budget, user = it, permission = req.permission))
            }
        } ?: userPermissionsRepository.findAllByUserAndBudget(getCurrentUser()!!, budget, null)
        return ResponseEntity.ok(BudgetResponse(budgetRepository.save(budget), users))
    }

    @DeleteMapping("/{id}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "deleteBudget", nickname = "deleteBudget", tags = ["Budgets"])
    open fun deleteBudget(@PathVariable id: Long): ResponseEntity<Unit> {
        val budget = userPermissionsRepository.findAllByUserAndBudget_Id(getCurrentUser()!!, id, null)
                .firstOrNull()
                ?.budget
                ?: return ResponseEntity.notFound().build()
        budgetRepository.delete(budget)
        return ResponseEntity.ok().build()
    }
}