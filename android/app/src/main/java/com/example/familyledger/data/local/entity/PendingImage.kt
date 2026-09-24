package com.example.familyledger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 无网暂存的待识别图片元数据（文件存私有目录，识别后删除） */
@Entity(tableName = "pending_images")
data class PendingImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val imageType: String, // product / receipt / order
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "pending" // pending / done / failed
)
