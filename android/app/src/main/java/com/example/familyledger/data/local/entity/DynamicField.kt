package com.example.familyledger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 第三层：动态自定义字段 — PRD 8.3 record_fields */
@Entity(
    tableName = "record_fields",
    foreignKeys = [
        ForeignKey(
            entity = Record::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recordId"), Index(value = ["recordId", "key"], unique = true)]
)
data class DynamicField(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long,
    val key: String,
    val value: String,
    /** text / number / date / boolean */
    val type: String = "text"
)
