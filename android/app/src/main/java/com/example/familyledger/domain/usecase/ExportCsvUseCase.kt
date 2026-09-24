package com.example.familyledger.domain.usecase

import android.content.Context
import com.example.familyledger.data.local.dao.DynamicFieldDao
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.domain.util.Money
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** CSV 导出 — UTF-8 BOM，全部记录，动态字段并集列 */
class ExportCsvUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val records: RecordRepository,
    private val fieldDao: DynamicFieldDao
) {
    suspend operator fun invoke(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val all = records.listAll().sortedBy { it.date }
            if (all.isEmpty()) return@withContext Result.failure(IllegalStateException("暂无记录"))

            val dynKeys = mutableListOf<String>()
            all.forEach { r ->
                fieldDao.listForRecord(r.id).forEach { f ->
                    if (f.key !in dynKeys) dynKeys.add(f.key)
                }
            }

            val base = listOf(
                "记录ID", "日期", "商品名称", "实付金额", "分类", "品牌", "规格",
                "数量", "单价", "折扣", "店铺", "支付方式", "订单号", "保质期", "备注", "来源"
            )
            val headers = base + dynKeys
            val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val sb = StringBuilder()
            sb.append('﻿')
            sb.appendLine(headers.joinToString(",") { csvEscape(it) })

            all.forEach { r ->
                val dyn = fieldDao.listForRecord(r.id).associate { it.key to it.value }
                val cols = mutableListOf(
                    r.id.toString(),
                    dateFmt.format(Date(r.date)),
                    r.name,
                    Money.formatCsv(r.price),
                    r.category,
                    r.brand ?: "",
                    r.spec ?: "",
                    r.quantity?.toString() ?: "",
                    r.unitPrice?.let { Money.formatCsv(it) } ?: "",
                    r.discount?.let { Money.formatCsv(it) } ?: "",
                    r.store ?: "",
                    r.paymentMethod ?: "",
                    r.orderNo ?: "",
                    r.expiryDate?.let { dayFmt.format(Date(it)) } ?: "",
                    r.note ?: "",
                    r.source
                )
                dynKeys.forEach { k -> cols.add(dyn[k] ?: "") }
                sb.appendLine(cols.joinToString(",") { csvEscape(it) })
            }

            val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "export")
            dir.mkdirs()
            val file = File(
                dir,
                "家庭账本_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".csv"
            )
            file.writeText(sb.toString(), Charsets.UTF_8)
            Result.success(file)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private fun csvEscape(v: String): String {
        return if (v.contains(',') || v.contains('"') || v.contains('\n') || v.contains('\r')) {
            "\"" + v.replace("\"", "\"\"") + "\""
        } else v
    }
}
