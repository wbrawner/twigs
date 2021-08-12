package com.wbrawner.twigs.storage

/**
 * Base interface for an entity repository that provides basic CRUD methods
 *
 * @param T The type of the object supported by this repository
 */
interface Repository<T> {
    suspend fun findAll(ids: List<String>? = null): List<T>
    suspend fun save(item: T): T
    suspend fun delete(item: T): Boolean
}
