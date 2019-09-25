package com.wbrawner.budgetserver.transaction

import com.wbrawner.budgetserver.ErrorResponse
import com.wbrawner.budgetserver.budget.BudgetRepository
import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.category.CategoryRepository
import com.wbrawner.budgetserver.getCurrentUser
import com.wbrawner.budgetserver.setToFirstOfMonth
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
import java.time.Instant
import java.util.*
import javax.transaction.Transactional

@RestController
@RequestMapping("/transactions")
@Api(value = "Transactions", tags = ["Transactions"], authorizations = [Authorization("basic")])
class TransactionController @Autowired constructor(
        private val budgetRepository: BudgetRepository,
        private val categoryRepository: CategoryRepository,
        private val transactionRepository: TransactionRepository
) {
    @Transactional
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getTransactions", nickname = "getTransactions", tags = ["Transactions"])
    fun getTransactions(
            @RequestParam("categoryId") categoryIds: Array<Long>? = null,
            @RequestParam("budgetId") budgetIds: Array<Long>? = null,
            @RequestParam("from") from: Date? = null,
            @RequestParam("to") to: Date? = null,
            @RequestParam count: Int?,
            @RequestParam page: Int?
    ): ResponseEntity<List<TransactionResponse>> {
        val budgets = if (budgetIds?.isNotEmpty() == true) {
            budgetRepository.findAllById(budgetIds.toList())
        } else {
            budgetRepository.findAllByUsersContainsOrOwner(getCurrentUser()!!)
        }.toList()
        val categories = if (categoryIds?.isNotEmpty() == true) {
            categoryRepository.findAllByBudgetInAndIdIn(budgets, categoryIds.toList())
        } else {
            categoryRepository.findAllByBudgetIn(budgets)
        }
        val pageRequest = PageRequest.of(min(0, page?.minus(1)?: 0), count?: 1000)
        return ResponseEntity.ok(transactionRepository.findAllByBudgetInAndCategoryInAndDateGreaterThan(
                budgets,
                categories,
                GregorianCalendar().setToFirstOfMonth().toInstant(),
                pageRequest
        ).map { TransactionResponse(it) })
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getTransaction", nickname = "getTransaction", tags = ["Transactions"])
    fun getTransaction(@PathVariable id: Long): ResponseEntity<TransactionResponse> {
        val transaction = transactionRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        budgetRepository.findByUsersContainsAndTransactionsContains(getCurrentUser()!!, transaction).orElse(null)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TransactionResponse(transaction))
    }

    @Transactional
    @PostMapping("/new", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "newTransaction", nickname = "newTransaction", tags = ["Transactions"])
    fun newTransaction(@RequestBody request: NewTransactionRequest): ResponseEntity<Any> {
        val budget = budgetRepository.findByUsersContainsAndId(getCurrentUser()!!, request.budgetId).orElse(null)
                ?: return ResponseEntity.badRequest().body(ErrorResponse("Invalid budget ID"))
        Hibernate.initialize(budget.users)
        val category: Category? = request.categoryId?.let {
            categoryRepository.findByBudgetAndId(budget, request.categoryId).orElse(null)
        }
        return ResponseEntity.ok(TransactionResponse(transactionRepository.save(Transaction(
                title = request.title,
                description = request.description,
                date = Instant.parse(request.date),
                amount = request.amount,
                category = category,
                expense = request.expense,
                budget = budget,
                createdBy = getCurrentUser()!!
        ))))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "updateTransaction", nickname = "updateTransaction", tags = ["Transactions"])
    fun updateTransaction(@PathVariable id: Long, @RequestBody request: UpdateTransactionRequest): ResponseEntity<TransactionResponse> {
        var transaction = transactionRepository.findById(id).orElse(null)?: return ResponseEntity.notFound().build()
        var budget = budgetRepository.findByUsersContainsAndTransactionsContains(getCurrentUser()!!, transaction)
                .orElse(null)?: return ResponseEntity.notFound().build()
        request.title?.let { transaction = transaction.copy(title = it) }
        request.description?.let { transaction = transaction.copy(description = it) }
        request.date?.let { transaction = transaction.copy(date = Instant.parse(it)) }
        request.amount?.let { transaction = transaction.copy(amount = it) }
        request.expense?.let { transaction = transaction.copy(expense = it) }
        request.budgetId?.let { budgetId ->
            budgetRepository.findByUsersContainsAndId(getCurrentUser()!!, budgetId).orElse(null)?.let {
                budget = it
                transaction = transaction.copy(budget = it, category = null)
            }
        }
        request.categoryId?.let {
            categoryRepository.findByBudgetAndId(budget, it).orElse(null)?.let { category ->
                transaction = transaction.copy(category = category)
            }
        }
        return ResponseEntity.ok(TransactionResponse(transactionRepository.save(transaction)))
    }

    @DeleteMapping("/{id}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "deleteTransaction", nickname = "deleteTransaction", tags = ["Transactions"])
    fun deleteTransaction(@PathVariable id: Long): ResponseEntity<Unit> {
        val transaction = transactionRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        // Check that the transaction belongs to an budget that the user has access to before deleting it
        budgetRepository.findByUsersContainsAndTransactionsContains(getCurrentUser()!!, transaction).orElse(null)
                ?: return ResponseEntity.notFound().build()
        transactionRepository.delete(transaction)
        return ResponseEntity.ok().build()
    }
}