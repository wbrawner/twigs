package com.wbrawner.twigs.server.budget

import com.wbrawner.budgetserver.currentUser
import com.wbrawner.budgetserver.permission.Permission
import com.wbrawner.budgetserver.permission.UserPermission
import com.wbrawner.budgetserver.permission.UserPermissionRepository
import com.wbrawner.budgetserver.permission.UserPermissionRequest
import com.wbrawner.budgetserver.transaction.TransactionRepository
import com.wbrawner.budgetserver.user.User
import com.wbrawner.budgetserver.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.function.Consumer
import java.util.function.Function
import javax.transaction.Transactional

@RestController
@RequestMapping(value = ["/budgets"])
@Transactional
open class BudgetController(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val userPermissionsRepository: UserPermissionRepository
) {
    private val logger = LoggerFactory.getLogger(BudgetController::class.java)

    @GetMapping(value = [""], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getBudgets(page: Int?, count: Int?): ResponseEntity<List<BudgetResponse>> {
        val user = currentUser ?: return ResponseEntity.status(401).build()
        val budgets: List<BudgetResponse> = userPermissionsRepository.findAllByUser(
            user,
            PageRequest.of(
                page ?: 0,
                count ?: 1000
            )
        ).mapNotNull { userPermission: UserPermission ->
            val budget = userPermission.budget ?: return@mapNotNull null
            BudgetResponse(budget, userPermissionsRepository.findAllByBudget(budget, null))
        }
        return ResponseEntity.ok(budgets)
    }

    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getBudget(@PathVariable id: String): ResponseEntity<BudgetResponse> {
        return getBudgetWithPermission(id, Permission.READ) { budget: Budget ->
            ResponseEntity.ok(BudgetResponse(budget, userPermissionsRepository.findAllByBudget(budget, null)))
        }
    }

    @GetMapping(value = ["/{id}/balance"], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getBudgetBalance(
        @PathVariable id: String,
        @RequestParam(value = "from", required = false) from: String? = null,
        @RequestParam(value = "to", required = false) to: String? = null
    ): ResponseEntity<BudgetBalanceResponse> {
        return getBudgetWithPermission(id, Permission.READ) { budget: Budget ->
            val fromInstant: Instant = try {
                Instant.parse(from)
            } catch (e: Exception) {
                if (e !is NullPointerException) logger.error(
                    "Failed to parse '$from' to Instant for 'from' parameter",
                    e
                )
                Instant.ofEpochSecond(0)
            }
            val toInstant: Instant = try {
                Instant.parse(to)
            } catch (e: Exception) {
                if (e !is NullPointerException) logger.error("Failed to parse '$to' to Instant for 'to' parameter", e)
                Instant.now()
            }
            val balance = transactionRepository.sumBalanceByBudgetId(budget.id, fromInstant, toInstant)
            ResponseEntity.ok(BudgetBalanceResponse(budget.id, balance))
        }
    }

    @PostMapping(
        value = [""],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    open fun newBudget(@RequestBody request: BudgetRequest): ResponseEntity<BudgetResponse> {
        val budget = budgetRepository.save(Budget(request.name, request.description))
        val users: MutableSet<UserPermission> = request.users
            .mapNotNull { userPermissionRequest: UserPermissionRequest ->
                val user = userRepository.findById(userPermissionRequest.user!!).orElse(null) ?: return@mapNotNull null
                userPermissionsRepository.save(
                    UserPermission(budget, user, userPermissionRequest.permission)
                )
            }
            .toMutableSet()
        val currentUserIncluded = users.any { userPermission: UserPermission -> userPermission.user!!.id == currentUser!!.id }
        if (!currentUserIncluded) {
            users.add(
                userPermissionsRepository.save(
                    UserPermission(budget, currentUser!!, Permission.OWNER)
                )
            )
        }
        return ResponseEntity.ok(BudgetResponse(budget, ArrayList(users)))
    }

    @PutMapping(
        value = ["/{id}"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    open fun updateBudget(
        @PathVariable id: String,
        @RequestBody request: BudgetRequest
    ): ResponseEntity<BudgetResponse> {
        return getBudgetWithPermission(id, Permission.MANAGE) { budget: Budget ->
            budget.name = request.name
            budget.description = request.description
            val users = ArrayList<UserPermission>()
            if (request.users.isNotEmpty()) {
                request.users.forEach(Consumer { userPermissionRequest: UserPermissionRequest ->
                    userRepository.findById(userPermissionRequest.user!!).ifPresent { requestedUser: User ->
                        users.add(
                            userPermissionsRepository.save(
                                UserPermission(
                                    budget,
                                    requestedUser,
                                    userPermissionRequest.permission
                                )
                            )
                        )
                    }
                })
            } else {
                users.addAll(userPermissionsRepository.findAllByBudget(budget, null))
            }
            ResponseEntity.ok(BudgetResponse(budgetRepository.save(budget), users))
        }
    }

    @DeleteMapping(value = ["/{id}"], produces = [MediaType.TEXT_PLAIN_VALUE])
    open fun deleteBudget(@PathVariable id: String): ResponseEntity<Void?> {
        return getBudgetWithPermission(id, Permission.MANAGE) { budget: Budget ->
            budgetRepository.delete(budget)
            ResponseEntity.ok().build()
        }
    }

    private fun <T> getBudgetWithPermission(
        budgetId: String,
        permission: Permission,
        callback: Function<Budget, ResponseEntity<T>>
    ): ResponseEntity<T> {
        val user = currentUser ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val userPermission = userPermissionsRepository.findByUserAndBudget_Id(user, budgetId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (userPermission.permission.isNotAtLeast(permission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val budget = userPermission.budget ?: return ResponseEntity.notFound().build()
        return callback.apply(budget)
    }
}