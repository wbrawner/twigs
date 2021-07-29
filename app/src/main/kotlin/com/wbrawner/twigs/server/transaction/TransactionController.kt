package com.wbrawner.twigs.server.transaction

import com.wbrawner.twigs.ErrorResponse
import com.wbrawner.twigs.server.category.Category
import com.wbrawner.twigs.server.category.CategoryRepository
import com.wbrawner.twigs.server.currentUser
import com.wbrawner.twigs.server.endOfMonth
import com.wbrawner.twigs.server.firstOfMonth
import com.wbrawner.twigs.server.permission.Permission
import com.wbrawner.twigs.server.permission.UserPermission
import com.wbrawner.twigs.server.permission.UserPermissionRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.stream.Collectors
import javax.transaction.Transactional
import kotlin.math.min

@RestController
@RequestMapping(path = ["/transactions"])
@Transactional
open class TransactionController(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val userPermissionsRepository: UserPermissionRepository
) {
    private val logger = LoggerFactory.getLogger(TransactionController::class.java)
    @GetMapping(path = [""], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getTransactions(
        @RequestParam(value = "categoryIds", required = false) categoryIds: List<String>?,
        @RequestParam(value = "budgetIds", required = false) budgetIds: List<String>?,
        @RequestParam(value = "from", required = false) from: String?,
        @RequestParam(value = "to", required = false) to: String?,
        @RequestParam(value = "count", required = false) count: Int?,
        @RequestParam(value = "page", required = false) page: Int?,
        @RequestParam(value = "sortBy", required = false) sortBy: String?,
        @RequestParam(value = "sortOrder", required = false) sortOrder: Sort.Direction?
    ): ResponseEntity<List<TransactionResponse>> {
        val userPermissions: List<UserPermission> = if (budgetIds != null && budgetIds.isNotEmpty()) {
            userPermissionsRepository.findAllByUserAndBudget_IdIn(
                currentUser,
                budgetIds,
                PageRequest.of(page ?: 0, count ?: 1000)
            )
        } else {
            userPermissionsRepository.findAllByUser(currentUser, null)
        }
        val budgets = userPermissions.stream()
            .map { obj: UserPermission -> obj.budget }
            .collect(Collectors.toList())
        var categories: List<Category?>? = null
        if (categoryIds != null && categoryIds.isNotEmpty()) {
            categories = categoryRepository.findAllByBudgetInAndIdIn(budgets, categoryIds, null)
        }
        val pageRequest = PageRequest.of(
            min(0, if (page != null) page - 1 else 0),
            count ?: 1000,
            sortOrder ?: Sort.Direction.DESC,
            sortBy ?: "date"
        )
        val fromInstant: Instant = try {
            Instant.parse(from)
        } catch (e: Exception) {
            if (e !is NullPointerException) logger.error("Failed to parse '$from' to Instant for 'from' parameter", e)
            firstOfMonth.toInstant()
        }
        val toInstant: Instant = try {
            Instant.parse(to)
        } catch (e: Exception) {
            if (e !is NullPointerException) logger.error("Failed to parse '$to' to Instant for 'to' parameter", e)
            endOfMonth.toInstant()
        }
        val query = if (categories == null) {
            transactionRepository.findAllByBudgetInAndDateGreaterThanAndDateLessThan(
                budgets,
                fromInstant,
                toInstant,
                pageRequest
            )
        } else {
            transactionRepository.findAllByBudgetInAndCategoryInAndDateGreaterThanAndDateLessThan(
                budgets,
                categories,
                fromInstant,
                toInstant,
                pageRequest
            )
        }
        val transactions = query.map { transaction: Transaction -> TransactionResponse(transaction) }
        return ResponseEntity.ok(transactions)
    }

    @GetMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getTransaction(@PathVariable id: String?): ResponseEntity<TransactionResponse> {
        val budgets = userPermissionsRepository.findAllByUser(currentUser, null)
            .stream()
            .map { obj: UserPermission -> obj.budget }
            .collect(Collectors.toList())
        val transaction = transactionRepository.findByIdAndBudgetIn(id, budgets).orElse(null)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TransactionResponse(transaction))
    }

    @PostMapping(
        path = [""],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    open fun newTransaction(@RequestBody request: NewTransactionRequest): ResponseEntity<Any> {
        val userResponse = userPermissionsRepository.findByUserAndBudget_Id(currentUser, request.budgetId)
            .orElse(null) ?: return ResponseEntity.badRequest().body(ErrorResponse("Invalid budget ID"))
        if (userResponse.permission.isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val budget = userResponse.budget
        var category: Category? = null
        if (request.categoryId != null) {
            category = categoryRepository.findByBudgetAndId(budget, request.categoryId).orElse(null)
        }
        return ResponseEntity.ok(
            TransactionResponse(
                transactionRepository.save(
                    Transaction(
                        title = request.title,
                        description = request.description,
                        date = Instant.parse(request.date),
                        amount = request.amount,
                        category = category,
                        expense = request.expense,
                        createdBy = currentUser,
                        budget = budget
                    )
                )
            )
        )
    }

    @PutMapping(
        path = ["/{id}"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    open fun updateTransaction(
        @PathVariable id: String,
        @RequestBody request: UpdateTransactionRequest
    ): ResponseEntity<Any> {
        val transaction = transactionRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        userPermissionsRepository.findByUserAndBudget_Id(
            currentUser,
            transaction.budget!!.id
        ).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (request.title != null) {
            transaction.title = request.title
        }
        if (request.description != null) {
            transaction.description = request.description
        }
        if (request.date != null) {
            transaction.date = Instant.parse(request.date)
        }
        if (request.amount != null) {
            transaction.amount = request.amount
        }
        if (request.expense != null) {
            transaction.expense = request.expense
        }
        if (request.budgetId != null) {
            val newUserPermission =
                userPermissionsRepository.findByUserAndBudget_Id(currentUser, request.budgetId).orElse(null)
            if (newUserPermission == null || newUserPermission.permission.isNotAtLeast(Permission.WRITE)) {
                return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse("Invalid budget"))
            }
            transaction.budget = newUserPermission.budget
        }
        if (request.categoryId != null) {
            val category = categoryRepository.findByBudgetAndId(transaction.budget, request.categoryId).orElse(null)
                ?: return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse("Invalid category"))
            transaction.category = category
        }
        return ResponseEntity.ok(TransactionResponse(transactionRepository.save(transaction)))
    }

    @DeleteMapping(path = ["/{id}"], produces = [MediaType.TEXT_PLAIN_VALUE])
    open fun deleteTransaction(@PathVariable id: String): ResponseEntity<Void> {
        val transaction = transactionRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        // Check that the transaction belongs to an budget that the user has access to before deleting it
        val userPermission = userPermissionsRepository.findByUserAndBudget_Id(
            currentUser,
            transaction.budget!!.id
        ).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (userPermission.permission.isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        transactionRepository.delete(transaction)
        return ResponseEntity.ok().build()
    }
}