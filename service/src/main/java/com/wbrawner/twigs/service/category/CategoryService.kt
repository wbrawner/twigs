package com.wbrawner.twigs.service.category

import com.wbrawner.twigs.model.Category
import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.requirePermission
import com.wbrawner.twigs.storage.CategoryRepository
import com.wbrawner.twigs.storage.PermissionRepository
import io.ktor.http.*

interface CategoryService {
    suspend fun categories(
        budgetIds: List<String>,
        userId: String,
        expense: Boolean? = null,
        archived: Boolean? = null,
    ): List<CategoryResponse>

    suspend fun category(categoryId: String, userId: String): CategoryResponse

    suspend fun save(request: CategoryRequest, userId: String, categoryId: String? = null): CategoryResponse

    suspend fun delete(categoryId: String, userId: String)
}

class DefaultCategoryService(
    private val categoryRepository: CategoryRepository,
    private val permissionRepository: PermissionRepository
) : CategoryService {

    override suspend fun categories(
        budgetIds: List<String>,
        userId: String,
        expense: Boolean?,
        archived: Boolean?,
    ): List<CategoryResponse> {
        val validBudgetIds = permissionRepository.findAll(
            budgetIds = budgetIds,
            userId = userId
        ).map { it.budgetId }
        if (validBudgetIds.isEmpty()) {
            return emptyList()
        }
        return categoryRepository.findAll(
            budgetIds = budgetIds,
            expense = expense,
            archived = archived
        ).map { it.asResponse() }
    }

    override suspend fun category(categoryId: String, userId: String): CategoryResponse {
        val budgetIds = permissionRepository.findAll(userId = userId).map { it.budgetId }
        if (budgetIds.isEmpty()) {
            throw HttpException(HttpStatusCode.NotFound)
        }
        return categoryRepository.findAll(
            ids = listOf(categoryId),
            budgetIds = budgetIds
        )
            .map { it.asResponse() }
            .firstOrNull()
            ?: throw HttpException(HttpStatusCode.NotFound)
    }

    override suspend fun save(request: CategoryRequest, userId: String, categoryId: String?): CategoryResponse {
        val category = categoryId?.let {
            categoryRepository.findAll(ids = listOf(categoryId)).firstOrNull()
                ?: throw HttpException(HttpStatusCode.NotFound)
        } ?: run {
            if (request.title.isNullOrBlank()) {
                throw HttpException(HttpStatusCode.BadRequest, message = "title cannot be null or empty")
            }
            if (request.budgetId.isNullOrBlank()) {
                throw HttpException(HttpStatusCode.BadRequest, message = "budgetId cannot be null or empty")
            }
            Category(
                title = request.title,
                description = request.description,
                amount = request.amount ?: 0L,
                expense = request.expense ?: true,
                budgetId = request.budgetId
            )
        }
        permissionRepository.requirePermission(userId, category.budgetId, Permission.WRITE)
        return categoryRepository.save(
            category.copy(
                title = request.title?.ifBlank { category.title } ?: category.title,
                description = request.description ?: category.description,
                amount = request.amount ?: category.amount,
                expense = request.expense ?: category.expense,
                archived = request.archived ?: category.archived,
                budgetId = request.budgetId?.ifBlank { category.budgetId } ?: category.budgetId
            )
        ).asResponse()
    }

    override suspend fun delete(categoryId: String, userId: String) {
        val category = categoryRepository.findAll(ids = listOf(categoryId))
            .firstOrNull()
            ?: throw HttpException(HttpStatusCode.NotFound)
        permissionRepository.requirePermission(userId, category.budgetId, Permission.WRITE)
        categoryRepository.delete(category)
    }
}