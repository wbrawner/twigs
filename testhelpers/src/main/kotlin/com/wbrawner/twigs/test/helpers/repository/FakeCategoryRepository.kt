package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.Category
import com.wbrawner.twigs.storage.CategoryRepository

class FakeCategoryRepository : FakeRepository<Category>(), CategoryRepository {
    override fun findAll(
        budgetIds: List<String>,
        ids: List<String>?,
        expense: Boolean?,
        archived: Boolean?
    ): List<Category> = entities.filter {
        budgetIds.contains(it.budgetId)
                && ids?.contains(it.id) ?: true
                && it.expense == (expense ?: it.expense)
                && it.archived == (archived ?: it.archived)
    }
}