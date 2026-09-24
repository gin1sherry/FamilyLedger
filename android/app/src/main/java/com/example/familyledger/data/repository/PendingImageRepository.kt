package com.example.familyledger.data.repository

import com.example.familyledger.data.local.dao.PendingImageDao
import com.example.familyledger.data.local.entity.PendingImage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingImageRepository @Inject constructor(
    private val dao: PendingImageDao
) {
    fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    suspend fun save(filePath: String, imageType: String): Long =
        dao.insert(PendingImage(filePath = filePath, imageType = imageType))

    suspend fun listPending(): List<PendingImage> = dao.listPending()

    suspend fun clear() {
        dao.listPending().forEach { dao.delete(it.id) }
    }
}
