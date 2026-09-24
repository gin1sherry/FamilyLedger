package com.example.familyledger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.familyledger.data.local.entity.DynamicField
import kotlinx.coroutines.flow.Flow

@Dao
interface DynamicFieldDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(field: DynamicField): Long

    @Update
    suspend fun update(field: DynamicField)

    @Query("DELETE FROM record_fields WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM record_fields WHERE recordId = :recordId")
    suspend fun deleteForRecord(recordId: Long)

    @Query("SELECT * FROM record_fields WHERE recordId = :recordId ORDER BY id")
    fun observeForRecord(recordId: Long): Flow<List<DynamicField>>

    @Query("SELECT * FROM record_fields WHERE recordId = :recordId ORDER BY id")
    suspend fun listForRecord(recordId: Long): List<DynamicField>

    @Query(
        """
        SELECT DISTINCT key FROM record_fields
        WHERE key LIKE '%' || :q || '%' OR value LIKE '%' || :q || '%'
        """
    )
    suspend fun searchKeys(q: String): List<String>

    @Query(
        """
        SELECT rf.recordId FROM record_fields rf
        WHERE rf.key LIKE '%' || :q || '%' OR rf.value LIKE '%' || :q || '%'
        """
    )
    fun searchRecordIds(q: String): Flow<List<Long>>
}
