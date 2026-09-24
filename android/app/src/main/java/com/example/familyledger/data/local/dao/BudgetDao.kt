package com.example.familyledger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.familyledger.data.local.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth")
    fun observeForMonth(yearMonth: String): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth AND category = :category LIMIT 1")
    suspend fun get(yearMonth: String, category: String): Budget?

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth")
    suspend fun listForMonth(yearMonth: String): List<Budget>

    @Query("DELETE FROM budgets WHERE yearMonth = :yearMonth AND category = :category AND amount <= 0")
    suspend fun deleteIfZero(yearMonth: String, category: String)

    @Query("UPDATE budgets SET notified = 0 WHERE yearMonth = :yearMonth AND category = :category")
    suspend fun resetNotified(yearMonth: String, category: String)

    @Query("UPDATE budgets SET notified = 1 WHERE yearMonth = :yearMonth AND category = :category")
    suspend fun markNotified(yearMonth: String, category: String)
}
