package com.wbrawner.budgetserver.category

import com.wbrawner.budgetserver.ErrorResponse
import com.wbrawner.budgetserver.account.AccountRepository
import com.wbrawner.budgetserver.getCurrentUser
import com.wbrawner.budgetserver.transaction.TransactionRepository
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.Authorization
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Integer.min
import javax.transaction.Transactional

@RestController
@RequestMapping("/categories")
@Api(value = "Categories", tags = ["Categories"], authorizations = [Authorization("basic")])
class CategoryController @Autowired constructor(
        private val accountRepository: AccountRepository,
        private val categoryRepository: CategoryRepository,
        private val transactionRepository: TransactionRepository
) {
    @Transactional
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getCategories", nickname = "getCategories", tags = ["Categories"])
    fun getCategories(accountId: Long, count: Int?, page: Int?): ResponseEntity<List<CategoryResponse>> {
        val account = accountRepository.findByUsersContainsAndId(getCurrentUser()!!, accountId).orElse(null)
                ?: return ResponseEntity.notFound().build()
        Hibernate.initialize(account.users)
        val pageRequest = PageRequest.of(min(0, page?.minus(1)?: 0), count?: 1000)
        return ResponseEntity.ok(categoryRepository.findAllByAccount(account, pageRequest).map { CategoryResponse(it) })
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getCategory", nickname = "getCategory", tags = ["Categories"])
    fun getCategory(@PathVariable id: Long): ResponseEntity<CategoryResponse> {
        val category = categoryRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        accountRepository.findByUsersContainsAndCategoriesContains(getCurrentUser()!!, category).orElse(null)
                ?: return ResponseEntity.notFound().build()
        return  ResponseEntity.ok(CategoryResponse(category))
    }

    @GetMapping("/{id}/balance", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getCategoryBalance", nickname = "getCategoryBalance", tags = ["Categories"])
    fun getCategoryBalance(@PathVariable id: Long): ResponseEntity<CategoryBalanceResponse> {
        val category = categoryRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        accountRepository.findByUsersContainsAndCategoriesContains(getCurrentUser()!!, category).orElse(null)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(CategoryBalanceResponse(category.id!!, transactionRepository.sumBalanceByCategoryId(category.id)))
    }

    @Transactional
    @PostMapping("/new", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "newCategory", nickname = "newCategory", tags = ["Categories"])
    fun newCategory(@RequestBody request: NewCategoryRequest): ResponseEntity<Any> {
        val account = accountRepository.findByUsersContainsAndId(getCurrentUser()!!, request.accountId).orElse(null)
                ?: return ResponseEntity.badRequest().body(ErrorResponse("Invalid account ID"))
        Hibernate.initialize(account.users)
        return ResponseEntity.ok(CategoryResponse(categoryRepository.save(Category(
                title = request.title,
                description = request.description,
                amount = request.amount,
                account = account
        ))))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "updateCategory", nickname = "updateCategory", tags = ["Categories"])
    fun updateCategory(@PathVariable id: Long, @RequestBody request: UpdateCategoryRequest): ResponseEntity<CategoryResponse> {
        var category = categoryRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        accountRepository.findByUsersContainsAndCategoriesContains(getCurrentUser()!!, category).orElse(null)
                ?: return ResponseEntity.notFound().build()
        request.title?.let { category = category.copy(title = it) }
        request.description?.let { category = category.copy(description = it) }
        request.amount?.let { category = category.copy(amount = it) }
        return ResponseEntity.ok(CategoryResponse(categoryRepository.save(category)))
    }

    @DeleteMapping("/{id}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "deleteCategory", nickname = "deleteCategory", tags = ["Categories"])
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Unit> {
        val category = categoryRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val account = accountRepository.findByUsersContainsAndCategoriesContains(getCurrentUser()!!, category).orElse(null)
                ?: return ResponseEntity.notFound().build()
        categoryRepository.delete(category)
        transactionRepository.findAllByAccountAndCategory(account, category).forEach { transaction ->
            transactionRepository.save(transaction.copy(category = null))
        }
        return ResponseEntity.ok().build()
    }
}