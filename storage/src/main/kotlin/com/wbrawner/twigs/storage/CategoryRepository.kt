package com.wbrawner.twigs.storage

import com.wbrawner.twigs.model.Category

interface CategoryRepository : Repository<Category> {
    fun findAll(
        budgetIds: List<String>,
        ids: List<String>? = null,
        expense: Boolean? = null,
        archived: Boolean? = null
    ): List<Category>
}