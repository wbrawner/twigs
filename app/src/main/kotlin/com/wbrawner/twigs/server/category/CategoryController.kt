package com.wbrawner.twigs.server.category

import com.wbrawner.twigs.server.ErrorResponse
import com.wbrawner.twigs.server.currentUser
import com.wbrawner.twigs.server.firstOfMonth
import com.wbrawner.twigs.server.permission.Permission
import com.wbrawner.twigs.server.permission.UserPermission
import com.wbrawner.twigs.server.permission.UserPermissionRepository
import com.wbrawner.twigs.server.transaction.Transaction
import com.wbrawner.twigs.server.transaction.TransactionRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.transaction.Transactional

@RestController
@RequestMapping(path = ["/categories"])
@Transactional
open class CategoryController(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val userPermissionsRepository: UserPermissionRepository
) {
    @GetMapping(path = [""], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getCategories(
        @RequestParam(name = "budgetIds", required = false) budgetIds: List<String?>?,
        @RequestParam(name = "isExpense", required = false) isExpense: Boolean?,
        @RequestParam(name = "includeArchived", required = false) includeArchived: Boolean?,
        @RequestParam(name = "count", required = false) count: Int?,
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "false", required = false) sortBy: String?,
        @RequestParam(name = "sortOrder", required = false) sortOrder: Sort.Direction?
    ): ResponseEntity<List<CategoryResponse>> {
        val userPermissions: List<UserPermission>
        userPermissions = if (budgetIds != null && !budgetIds.isEmpty()) {
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
        val pageRequest = PageRequest.of(
            Math.min(0, if (page != null) page - 1 else 0),
            count ?: 1000,
            sortOrder ?: Sort.Direction.ASC,
            sortBy ?: "title"
        )
        val archived = if (includeArchived == null || includeArchived == false) false else null
        val categories = categoryRepository.findAllByBudgetIn(budgets, isExpense, archived, pageRequest)
        return ResponseEntity.ok(
            categories.stream()
                .map { category: Category -> CategoryResponse(category) }
                .collect(Collectors.toList())
        )
    }

    @GetMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getCategory(@PathVariable id: String?): ResponseEntity<CategoryResponse> {
        val budgets = userPermissionsRepository.findAllByUser(currentUser, null)
            .stream()
            .map { obj: UserPermission -> obj.budget }
            .collect(Collectors.toList())
        val category = categoryRepository.findByBudgetInAndId(budgets, id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(CategoryResponse(category))
    }

    @GetMapping(path = ["/{id}/balance"], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getCategoryBalance(@PathVariable id: String?): ResponseEntity<CategoryBalanceResponse> {
        val budgets = userPermissionsRepository.findAllByUser(currentUser, null)
            .stream()
            .map { obj: UserPermission -> obj.budget }
            .collect(Collectors.toList())
        val category = categoryRepository.findByBudgetInAndId(budgets, id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val sum = transactionRepository.sumBalanceByCategoryId(category.id, firstOfMonth)
        return ResponseEntity.ok(CategoryBalanceResponse(category.id, sum))
    }

    @PostMapping(
        path = [""],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    open fun newCategory(@RequestBody request: NewCategoryRequest): ResponseEntity<Any> {
        val userResponse = userPermissionsRepository.findByUserAndBudget_Id(currentUser, request.budgetId)
            .orElse(null) ?: return ResponseEntity.badRequest().body(ErrorResponse("Invalid budget ID"))
        if (userResponse.permission.isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val budget = userResponse.budget
        return ResponseEntity.ok(
            CategoryResponse(
                categoryRepository.save(
                    Category(
                        title = request.title,
                        description = request.description,
                        amount = request.amount,
                        budget = budget,
                        expense = request.expense
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
    open fun updateCategory(
        @PathVariable id: String,
        @RequestBody request: UpdateCategoryRequest
    ): ResponseEntity<CategoryResponse> {
        val category = categoryRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val userPermission = userPermissionsRepository.findByUserAndBudget_Id(
            currentUser,
            category.budget!!.id
        ).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (userPermission.permission.isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        if (request.title != null) {
            category.title = request.title
        }
        if (request.description != null) {
            category.description = request.description
        }
        if (request.amount != null) {
            category.amount = request.amount
        }
        if (request.expense != null) {
            category.expense = request.expense
        }
        if (request.archived != null) {
            category.archived = request.archived
        }
        return ResponseEntity.ok(CategoryResponse(categoryRepository.save(category)))
    }

    @DeleteMapping(path = ["/{id}"], produces = [MediaType.TEXT_PLAIN_VALUE])
    open fun deleteCategory(@PathVariable id: String): ResponseEntity<Void> {
        val category = categoryRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val userPermission =
            userPermissionsRepository.findByUserAndBudget_Id(currentUser, category.budget!!.id).orElse(null)
                ?: return ResponseEntity.notFound().build()
        if (userPermission.permission.isNotAtLeast(Permission.WRITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        transactionRepository.findAllByBudgetAndCategory(userPermission.budget, category)
            .forEach(Consumer { transaction: Transaction ->
                transaction.category = null
                transactionRepository.save(transaction)
            })
        categoryRepository.delete(category)
        return ResponseEntity.ok().build()
    }
}