package com.example.familyledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.familyledger.data.local.entity.Record
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: Record): Long

    @Update
    suspend fun update(record: Record)

    @Delete
    suspend fun delete(record: Record)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: Long): Record?

    @Query("SELECT * FROM records WHERE id = :id")
    fun observeById(id: Long): Flow<Record?>

    @Query(
        """
        SELECT * FROM records
        WHERE date >= :start AND date < :end
        ORDER BY date DESC, id DESC
        """
    )
    fun observeInMonth(start: Long, end: Long): Flow<List<Record>>

    @Query(
        """
        SELECT * FROM records
        WHERE date >= :start AND date < :end
        ORDER BY date DESC, id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeInMonthPaged(start: Long, end: Long, limit: Int, offset: Int): Flow<List<Record>>

    @Query(
        """
        SELECT * FROM records
        WHERE date >= :start AND date < :end
        ORDER BY date DESC, id DESC
        """
    )
    suspend fun listInMonth(start: Long, end: Long): List<Record>

    @Query(
        """
        SELECT COALESCE(SUM(price), 0) FROM records
        WHERE date >= :start AND date < :end AND category = :category
        """
    )
    fun observeCategorySpent(start: Long, end: Long, category: String): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(price), 0) FROM records
        WHERE date >= :start AND date < :end
        """
    )
    fun observeTotalSpent(start: Long, end: Long): Flow<Long>

    @Query(
        """
        SELECT * FROM records
        WHERE name LIKE '%' || :q || '%'
           OR IFNULL(store, '') LIKE '%' || :q || '%'
           OR IFNULL(note, '') LIKE '%' || :q || '%'
           OR category LIKE '%' || :q || '%'
        ORDER BY date DESC
        """
    )
    fun search(q: String): Flow<List<Record>>

    @Query("SELECT * FROM records ORDER BY date ASC")
    suspend fun listAll(): List<Record>

    @Query("SELECT * FROM records WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<Record>
}
