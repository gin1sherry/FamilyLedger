package com.example.familyledger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 消费记录主表 — 对应 PRD 8.2 records
 * price / unitPrice / discount 一律以「分」为单位的整数存储，避免 Double 浮点误差
 */
@Entity(
    tableName = "records",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category"]),
        Index(value = ["name"]),
        Index(value = ["sourceBatchId"])
    ]
)
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Long, // cents
    val date: Long,
    val category: String,
    val brand: String? = null,
    val spec: String? = null,
    val quantity: Double? = null,
    val unitPrice: Long? = null, // cents
    val discount: Long? = null, // cents
    val store: String? = null,
    val paymentMethod: String? = null,
    val orderNo: String? = null,
    val expiryDate: Long? = null,
    val note: String? = null,
    /** camera / receipt / screenshot / manual / pending */
    val source: String,
    /** 小票多条目共享批次 */
    val sourceBatchId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
