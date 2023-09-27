package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.db.DatabaseMetadata
import com.wbrawner.twigs.db.MetadataRepository
import com.wbrawner.twigs.storage.Repository

class FakeMetadataRepository : Repository<DatabaseMetadata>, MetadataRepository {
    var metadata = DatabaseMetadata()
    override fun runMigration(toVersion: Int) {
        metadata = metadata.copy(version = toVersion)
    }

    override suspend fun findAll(ids: List<String>?): List<DatabaseMetadata> = listOf(metadata)

    override suspend fun delete(item: DatabaseMetadata): Boolean = false

    override suspend fun save(item: DatabaseMetadata): DatabaseMetadata {
        metadata = item
        return metadata
    }
}