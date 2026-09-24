package com.example.familyledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.familyledger.data.local.dao.BudgetDao
import com.example.familyledger.data.local.dao.DynamicFieldDao
import com.example.familyledger.data.local.dao.PendingImageDao
import com.example.familyledger.data.local.dao.RecordDao
import com.example.familyledger.data.local.entity.Budget
import com.example.familyledger.data.local.entity.DynamicField
import com.example.familyledger.data.local.entity.PendingImage
import com.example.familyledger.data.local.entity.Record

@Database(
    entities = [Record::class, DynamicField::class, Budget::class, PendingImage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
    abstract fun budgetDao(): BudgetDao
    abstract fun dynamicFieldDao(): DynamicFieldDao
    abstract fun pendingImageDao(): PendingImageDao

    companion object {
        const val NAME = "family_ledger.db"
    }
}
