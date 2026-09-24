package com.example.familyledger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 分类月度预算 — PRD 8.4 budgets；amount 单位：分 */
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["yearMonth", "category"], unique = true)]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearMonth: String, // "2026-09"
    val category: String,
    val amount: Long, // cents
    val notified: Boolean = false
)
