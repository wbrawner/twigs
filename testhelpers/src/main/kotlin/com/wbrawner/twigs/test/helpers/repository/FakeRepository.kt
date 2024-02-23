package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.Identifiable
import com.wbrawner.twigs.storage.Repository

abstract class FakeRepository<T : Identifiable> : Repository<T> {
    open val entities = mutableListOf<T>()

    override suspend fun findAll(ids: List<String>?): List<T> = if (ids == null) {
        entities
    } else {
        entities.filter { ids.contains(it.id) }
    }

    override suspend fun save(item: T): T {
        entities.removeIf { it.id == item.id }
        entities.add(item)
        return item
    }

    override suspend fun delete(item: T): Boolean = entities.removeIf { it.id == item.id }
}