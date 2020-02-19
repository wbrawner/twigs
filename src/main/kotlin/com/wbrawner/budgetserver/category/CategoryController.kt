package com.wbrawner.budgetserver.category

import com.wbrawner.budgetserver.ErrorResponse
import com.wbrawner.budgetserver.budget.BudgetRepository
import com.wbrawner.budgetserver.getCurrentUser
import com.wbrawner.budgetserver.permission.UserPermissionRepository
import com.wbrawner.budgetserver.transaction.TransactionRepository
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.Authorization
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Integer.min
import javax.transaction.Transactional

@RestController
@RequestMapping("/categories")
@Api(value = "Categories", tags = ["Categories"], authorizations = [Authorization("basic")])
@Transactional
open class CategoryController(
        private val budgetRepository: BudgetRepository,
        private val categoryRepository: CategoryRepository,
        private val transactionRepository: TransactionRepository,
        private val userPermissionsRepository: UserPermissionRepository
) {
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getCategories", nickname = "getCategories", tags = ["Categories"])
    open fun getCategories(
            @RequestParam("budgetIds", required = false) budgetIds: List<Long>? = null,
            @RequestParam("isExpense", required = false) isExpense: Boolean? = null,
            @RequestParam("count", required = false) count: Int?,
            @RequestParam("page", required = false) page: Int?,
            @RequestParam("false", required = false) sortBy: String?,
            @RequestParam("sortOrder", required = false) sortOrder: Sort.Direction?
    ): ResponseEntity<List<CategoryResponse>> {
        val budgets = (
                budgetIds
                        ?.let {
                            userPermissionsRepository.findAllByUserAndBudget_IdIn(getCurrentUser()!!, it, null)
                        }
                        ?: userPermissionsRepository.findAllByUser(
                                user = getCurrentUser()!!,
                                pageable = PageRequest.of(page ?: 0, count ?: 1000)
                        )
                )
                .mapNotNull {
                    it.budget
                }
        val pageRequest = PageRequest.of(
                min(0, page?.minus(1) ?: 0),
                count ?: 1000,
                Sort.by(sortOrder ?: Sort.Direction.ASC, sortBy ?: "title")
        )
        val categories = if (isExpense == null) {
            categoryRepository.findAllByBudgetIn(budgets, pageRequest)
        } else {
            categoryRepository.findAllByBudgetInAndExpense(budgets, isExpense, pageRequest)
        }
        return ResponseEntity.ok(categories.map { CategoryResponse(it) })
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getCategory", nickname = "getCategory", tags = ["Categories"])
    open fun getCategory(@PathVariable id: Long): ResponseEntity<CategoryResponse> {
        val budgets = userPermissionsRepository.findAllByUser(getCurrentUser()!!, null)
                .mapNotNull { it.budget }

        val category = categoryRepository.findByBudgetInAndId(budgets, id)
                .orElse(null)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(CategoryResponse(category))
    }

    @GetMapping("/{id}/balance", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getCategoryBalance", nickname = "getCategoryBalance", tags = ["Categories"])
    open fun getCategoryBalance(@PathVariable id: Long): ResponseEntity<CategoryBalanceResponse> {
        val budgets = userPermissionsRepository.findAllByUser(getCurrentUser()!!, null)
                .mapNotNull { it.budget }
        val category = categoryRepository.findByBudgetInAndId(budgets, id)
                .orElse(null)
                ?: return ResponseEntity.notFound().build()
        val transactions = transactionRepository.sumBalanceByCategoryId(category.id!!)
        return ResponseEntity.ok(CategoryBalanceResponse(category.id, transactions))
    }

    @PostMapping("/new", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "newCategory", nickname = "newCategory", tags = ["Categories"])
    open fun newCategory(@RequestBody request: NewCategoryRequest): ResponseEntity<Any> {
        val budget = userPermissionsRepository.findAllByUserAndBudget_Id(getCurrentUser()!!, request.budgetId, null)
                .firstOrNull()
                ?.budget
                ?: return ResponseEntity.badRequest().body(ErrorResponse("Invalid budget ID"))
        return ResponseEntity.ok(CategoryResponse(categoryRepository.save(Category(
                title = request.title,
                description = request.description,
                amount = request.amount,
                budget = budget
        ))))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "updateCategory", nickname = "updateCategory", tags = ["Categories"])
    open fun updateCategory(@PathVariable id: Long, @RequestBody request: UpdateCategoryRequest): ResponseEntity<CategoryResponse> {
        val budgets = userPermissionsRepository.findAllByUser(getCurrentUser()!!, null)
                .mapNotNull { it.budget }
        var category = categoryRepository.findByBudgetInAndId(budgets, id)
                .orElse(null)
                ?: return ResponseEntity.notFound().build()
        request.title?.let { category = category.copy(title = it) }
        request.description?.let { category = category.copy(description = it) }
        request.amount?.let { category = category.copy(amount = it) }
        return ResponseEntity.ok(CategoryResponse(categoryRepository.save(category)))
    }

    @DeleteMapping("/{id}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "deleteCategory", nickname = "deleteCategory", tags = ["Categories"])
    open fun deleteCategory(@PathVariable id: Long): ResponseEntity<Unit> {
        val budgets = userPermissionsRepository.findAllByUser(getCurrentUser()!!, null)
                .mapNotNull { it.budget }
        val category = categoryRepository.findByBudgetInAndId(budgets, id)
                .orElse(null)
                ?: return ResponseEntity.notFound().build()
        val budget = budgets.first { it.id == category.budget!!.id }
        categoryRepository.delete(category)
        transactionRepository.findAllByBudgetAndCategory(budget, category)
                .forEach { transaction ->
                    transactionRepository.save(transaction.copy(category = null))
                }
        return ResponseEntity.ok().build()
    }
}