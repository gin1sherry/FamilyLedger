package com.example.familyledger.di

import android.content.Context
import androidx.room.Room
import com.example.familyledger.data.local.AppDatabase
import com.example.familyledger.data.local.dao.BudgetDao
import com.example.familyledger.data.local.dao.DynamicFieldDao
import com.example.familyledger.data.local.dao.PendingImageDao
import com.example.familyledger.data.local.dao.RecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideRecordDao(db: AppDatabase): RecordDao = db.recordDao()
    @Provides fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideDynamicFieldDao(db: AppDatabase): DynamicFieldDao = db.dynamicFieldDao()
    @Provides fun providePendingImageDao(db: AppDatabase): PendingImageDao = db.pendingImageDao()
}
