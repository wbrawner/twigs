package com.wbrawner.twigs.service.transaction

import com.wbrawner.twigs.endOfMonth
import com.wbrawner.twigs.firstOfMonth
import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.Transaction
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.requirePermission
import com.wbrawner.twigs.storage.CategoryRepository
import com.wbrawner.twigs.storage.PermissionRepository
import com.wbrawner.twigs.storage.TransactionRepository
import com.wbrawner.twigs.toInstant
import com.wbrawner.twigs.toInstantOrNull
import io.ktor.http.*
import java.time.Instant

interface TransactionService {
    suspend fun transactions(
        budgetIds: List<String>,
        categoryIds: List<String>?,
        from: Instant?,
        to: Instant?,
        expense: Boolean?,
        userId: String,
    ): List<TransactionResponse>

    suspend fun transaction(transactionId: String, userId: String): TransactionResponse

    suspend fun sum(
        budgetId: String?,
        categoryId: String?,
        from: Instant?,
        to: Instant?,
        userId: String,
    ): Long

    suspend fun save(
        request: TransactionRequest,
        userId: String,
        transactionId: String? = null
    ): TransactionResponse

    suspend fun delete(transactionId: String, userId: String)
}

class DefaultTransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val permissionRepository: PermissionRepository
) : TransactionService {
    override suspend fun transactions(
        budgetIds: List<String>,
        categoryIds: List<String>?,
        from: Instant?,
        to: Instant?,
        expense: Boolean?,
        userId: String
    ): List<TransactionResponse> {
        permissionRepository.requirePermission(userId, budgetIds, Permission.READ)
        return transactionRepository.findAll(
            budgetIds = budgetIds,
            categoryIds = categoryIds,
            from = from,
            to = to,
            expense = expense
        ).map { it.asResponse() }
    }

    override suspend fun transaction(
        transactionId: String,
        userId: String
    ): TransactionResponse {
        val transaction = transactionRepository.findAll(ids = listOf(transactionId))
            .firstOrNull()
            ?: throw HttpException(HttpStatusCode.NotFound)
        permissionRepository.requirePermission(userId, transaction.budgetId, Permission.READ)
        return transaction.asResponse()
    }

    override suspend fun sum(
        budgetId: String?,
        categoryId: String?,
        from: Instant?,
        to: Instant?,
        userId: String
    ): Long {
        if (budgetId.isNullOrBlank() && categoryId.isNullOrBlank()) {
            throw HttpException(HttpStatusCode.BadRequest, message = "budgetId or categoryId must be provided to sum")
        }
        if (budgetId?.isNotBlank() == true && categoryId?.isNotBlank() == true) {
            throw HttpException(
                HttpStatusCode.BadRequest,
                message = "budgetId and categoryId cannot be provided together"
            )
        }
        return if (!categoryId.isNullOrBlank()) {
            val category = categoryRepository.findAll(ids = listOf(categoryId)).firstOrNull()
                ?: throw HttpException(HttpStatusCode.NotFound)
            permissionRepository.requirePermission(
                userId = userId,
                budgetId = category.budgetId,
                permission = Permission.READ
            )
            transactionRepository.sumByCategory(category.id, from ?: firstOfMonth, to ?: endOfMonth)
        } else if (!budgetId.isNullOrBlank()) {
            permissionRepository.requirePermission(userId = userId, budgetId = budgetId, permission = Permission.READ)
            transactionRepository.sumByBudget(budgetId, from ?: firstOfMonth, to ?: endOfMonth)
        } else {
            error("Somehow we didn't return either a budget or category sum")
        }
    }

    override suspend fun save(
        request: TransactionRequest,
        userId: String,
        transactionId: String?
    ): TransactionResponse {
        val transaction = transactionId?.let {
            transactionRepository.findAll(ids = listOf(it))
                .firstOrNull()
                ?.also { transaction ->
                    permissionRepository.requirePermission(userId, transaction.budgetId, Permission.WRITE)
                }
                ?: throw HttpException(HttpStatusCode.NotFound)
        } ?: run {
            if (request.title.isNullOrBlank()) {
                throw HttpException(HttpStatusCode.BadRequest, message = "title cannot be null or empty")
            }
            if (request.budgetId.isNullOrBlank()) {
                throw HttpException(HttpStatusCode.BadRequest, message = "budgetId cannot be null or empty")
            }
            if (request.date?.toInstantOrNull() == null) {
                throw HttpException(HttpStatusCode.BadRequest, message = "invalid date")
            }
            Transaction(
                title = request.title,
                description = request.description,
                amount = request.amount ?: 0L,
                expense = request.expense ?: true,
                budgetId = request.budgetId,
                categoryId = request.categoryId,
                date = request.date.toInstant(),
                createdBy = userId,
            )
        }
        permissionRepository.requirePermission(userId, request.budgetId ?: transaction.budgetId, Permission.WRITE)
        return transactionRepository.save(
            transaction.copy(
                title = request.title?.ifBlank { transaction.title } ?: transaction.title,
                description = request.description ?: transaction.description,
                amount = request.amount ?: transaction.amount,
                expense = request.expense ?: transaction.expense,
                budgetId = request.budgetId?.ifBlank { transaction.budgetId } ?: transaction.budgetId,
                categoryId = request.categoryId ?: transaction.categoryId,
                date = request.date?.toInstantOrNull() ?: transaction.date
            )
        ).asResponse()
    }

    override suspend fun delete(transactionId: String, userId: String) {
        val transaction = transactionRepository.findAll(ids = listOf(transactionId))
            .firstOrNull()
            ?: throw HttpException(HttpStatusCode.NotFound)
        permissionRepository.requirePermission(userId, transaction.budgetId, Permission.WRITE)
        transactionRepository.delete(transaction)
    }
}