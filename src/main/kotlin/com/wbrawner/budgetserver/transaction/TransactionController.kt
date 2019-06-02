package com.wbrawner.budgetserver.transaction

import com.wbrawner.budgetserver.ErrorResponse
import com.wbrawner.budgetserver.account.AccountRepository
import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.category.CategoryRepository
import com.wbrawner.budgetserver.getCurrentUser
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
import javax.transaction.Transactional

@RestController
@RequestMapping("/transactions")
@Api(value = "Transactions", tags = ["Transactions"], authorizations = [Authorization("basic")])
class TransactionController @Autowired constructor(
        private val accountRepository: AccountRepository,
        private val categoryRepository: CategoryRepository,
        private val transactionRepository: TransactionRepository
) {
    @Transactional
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getTransactions", nickname = "getTransactions", tags = ["Transactions"])
    fun getTransactions(accountId: Long, count: Int?, page: Int?): ResponseEntity<List<TransactionResponse>> {
        val account = accountRepository.findByUsersContainsAndId(getCurrentUser()!!, accountId).orElse(null)
                ?: return ResponseEntity.notFound().build()
        Hibernate.initialize(account.users)
        val pageRequest = PageRequest.of(min(0, page?.minus(1)?: 0), count?: 1000)
        return ResponseEntity.ok(transactionRepository.findAllByAccount(account, pageRequest).map { TransactionResponse(it) })
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getTransaction", nickname = "getTransaction", tags = ["Transactions"])
    fun getTransaction(@PathVariable id: Long): ResponseEntity<TransactionResponse> {
        val transaction = transactionRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        accountRepository.findByUsersContainsAndTransactionsContains(getCurrentUser()!!, transaction).orElse(null)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TransactionResponse(transaction))
    }

    @Transactional
    @PostMapping("/new", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "newTransaction", nickname = "newTransaction", tags = ["Transactions"])
    fun newTransaction(@RequestBody request: NewTransactionRequest): ResponseEntity<Any> {
        val account = accountRepository.findByUsersContainsAndId(getCurrentUser()!!, request.accountId).orElse(null)
                ?: return ResponseEntity.badRequest().body(ErrorResponse("Invalid account ID"))
        Hibernate.initialize(account.users)
        val category: Category? = request.categoryId?.let {
            categoryRepository.findByAccountAndId(account, request.categoryId).orElse(null)
        }
        return ResponseEntity.ok(TransactionResponse(transactionRepository.save(Transaction(
                title = request.title,
                description = request.description,
                date = Instant.parse(request.date),
                amount = request.amount,
                category = category,
                expense = request.expense,
                account = account,
                createdBy = getCurrentUser()!!
        ))))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "updateTransaction", nickname = "updateTransaction", tags = ["Transactions"])
    fun updateTransaction(@PathVariable id: Long, @RequestBody request: UpdateTransactionRequest): ResponseEntity<TransactionResponse> {
        var transaction = transactionRepository.findById(id).orElse(null)?: return ResponseEntity.notFound().build()
        var account = accountRepository.findByUsersContainsAndTransactionsContains(getCurrentUser()!!, transaction)
                .orElse(null)?: return ResponseEntity.notFound().build()
        request.title?.let { transaction = transaction.copy(title = it) }
        request.description?.let { transaction = transaction.copy(description = it) }
        request.date?.let { transaction = transaction.copy(date = Instant.parse(it)) }
        request.amount?.let { transaction = transaction.copy(amount = it) }
        request.expense?.let { transaction = transaction.copy(expense = it) }
        request.accountId?.let { accountId ->
            accountRepository.findByUsersContainsAndId(getCurrentUser()!!, accountId).orElse(null)?.let {
                account = it
                transaction = transaction.copy(account = it, category = null)
            }
        }
        request.categoryId?.let {
            categoryRepository.findByAccountAndId(account, it).orElse(null)?.let { category ->
                transaction = transaction.copy(category = category)
            }
        }
        return ResponseEntity.ok(TransactionResponse(transactionRepository.save(transaction)))
    }

    @DeleteMapping("/{id}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "deleteTransaction", nickname = "deleteTransaction", tags = ["Transactions"])
    fun deleteTransaction(@PathVariable id: Long): ResponseEntity<Unit> {
        val transaction = transactionRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        // Check that the transaction belongs to an account that the user has access to before deleting it
        accountRepository.findByUsersContainsAndTransactionsContains(getCurrentUser()!!, transaction).orElse(null)
                ?: return ResponseEntity.notFound().build()
        transactionRepository.delete(transaction)
        return ResponseEntity.ok().build()
    }
}