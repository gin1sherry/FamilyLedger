package com.example.familyledger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.familyledger.data.local.entity.PendingImage
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingImageDao {
    @Insert
    suspend fun insert(item: PendingImage): Long

    @Query("SELECT * FROM pending_images WHERE status = 'pending' ORDER BY createdAt ASC")
    suspend fun listPending(): List<PendingImage>

    @Query("SELECT COUNT(*) FROM pending_images WHERE status = 'pending'")
    fun observePendingCount(): Flow<Int>

    @Query("UPDATE pending_images SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM pending_images WHERE id = :id")
    suspend fun delete(id: Long)
}
