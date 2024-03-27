package com.wbrawner.twigs.service.recurringtransaction

import com.wbrawner.twigs.asFrequency
import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.RecurringTransaction
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.requirePermission
import com.wbrawner.twigs.storage.PermissionRepository
import com.wbrawner.twigs.storage.RecurringTransactionRepository
import com.wbrawner.twigs.toInstant
import io.ktor.http.*
import java.time.Instant

interface RecurringTransactionService {
    suspend fun recurringTransactions(
        budgetId: String,
        userId: String,
    ): List<RecurringTransactionResponse>

    suspend fun recurringTransaction(recurringTransactionId: String, userId: String): RecurringTransactionResponse

    suspend fun save(
        request: RecurringTransactionRequest,
        userId: String,
        recurringTransactionId: String? = null
    ): RecurringTransactionResponse

    suspend fun delete(recurringTransactionId: String, userId: String)
}

class DefaultRecurringTransactionService(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val permissionRepository: PermissionRepository
) : RecurringTransactionService {
    override suspend fun recurringTransactions(
        budgetId: String,
        userId: String
    ): List<RecurringTransactionResponse> {
        permissionRepository.requirePermission(userId, budgetId, Permission.READ)
        return recurringTransactionRepository.findAll(budgetId = budgetId)
            .map { it.asResponse() }
    }

    override suspend fun recurringTransaction(
        recurringTransactionId: String,
        userId: String
    ): RecurringTransactionResponse {
        val recurringTransaction = recurringTransactionRepository.findAll(ids = listOf(recurringTransactionId))
            .firstOrNull()
            ?: throw HttpException(HttpStatusCode.NotFound)
        permissionRepository.requirePermission(userId, recurringTransaction.budgetId, Permission.READ)
        return recurringTransaction.asResponse()
    }

    override suspend fun save(
        request: RecurringTransactionRequest,
        userId: String,
        recurringTransactionId: String?
    ): RecurringTransactionResponse {
        val recurringTransaction = recurringTransactionId?.let {
            recurringTransactionRepository.findAll(ids = listOf(it))
                .firstOrNull()
                ?.also { recurringTransaction ->
                    permissionRepository.requirePermission(userId, recurringTransaction.budgetId, Permission.WRITE)
                }
                ?: throw HttpException(HttpStatusCode.NotFound)
        } ?: run {
            if (request.title.isNullOrBlank()) {
                throw HttpException(HttpStatusCode.BadRequest, message = "title cannot be null or empty")
            }
            if (request.budgetId.isNullOrBlank()) {
                throw HttpException(HttpStatusCode.BadRequest, message = "budgetId cannot be null or empty")
            }
            RecurringTransaction(
                title = request.title,
                description = request.description,
                amount = request.amount ?: 0L,
                expense = request.expense ?: true,
                budgetId = request.budgetId,
                categoryId = request.categoryId,
                createdBy = userId,
                start = request.start?.toInstant() ?: Instant.now(),
                finish = request.finish?.toInstant(),
                frequency = request.frequency.asFrequency()
            )
        }
        permissionRepository.requirePermission(userId, recurringTransaction.budgetId, Permission.WRITE)
        return recurringTransactionRepository.save(
            recurringTransaction.copy(
                title = request.title?.ifBlank { recurringTransaction.title } ?: recurringTransaction.title,
                description = request.description ?: recurringTransaction.description,
                amount = request.amount ?: recurringTransaction.amount,
                expense = request.expense ?: recurringTransaction.expense,
                budgetId = request.budgetId?.ifBlank { recurringTransaction.budgetId } ?: recurringTransaction.budgetId,
                categoryId = request.categoryId ?: recurringTransaction.categoryId,
                start = request.start?.toInstant() ?: recurringTransaction.start,
                finish = request.finish?.toInstant() ?: recurringTransaction.finish,
                frequency = request.frequency.asFrequency()
            )
        ).asResponse()
    }

    override suspend fun delete(recurringTransactionId: String, userId: String) {
        val recurringTransaction = recurringTransactionRepository.findAll(ids = listOf(recurringTransactionId))
            .firstOrNull()
            ?: throw HttpException(HttpStatusCode.NotFound)
        permissionRepository.requirePermission(userId, recurringTransaction.budgetId, Permission.WRITE)
        recurringTransactionRepository.delete(recurringTransaction)
    }
}