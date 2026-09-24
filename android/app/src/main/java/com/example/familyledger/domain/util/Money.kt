package com.example.familyledger.domain.util

/**
 * 金额一律以「分」为整数存储；展示与录入使用元。
 */
object Money {
    fun yuanToCents(yuan: Double): Long = Math.round(yuan * 100.0)

    fun yuanToCents(yuan: String): Long {
        val v = yuan.trim().toDoubleOrNull() ?: return 0L
        return yuanToCents(v)
    }

    fun centsToYuan(cents: Long): Double = cents / 100.0

    /** 不带货币符号，最多两位小数 */
    fun formatYuan(cents: Long, withSymbol: Boolean = false): String {
        val abs = kotlin.math.abs(cents)
        val y = abs / 100
        val c = abs % 100
        val body = if (c == 0L) y.toString() else "%d.%02d".format(y, c)
        val sign = if (cents < 0) "-" else ""
        return if (withSymbol) "${sign}¥$body" else "$sign$body"
    }

    /** CSV 用：固定两位小数，不带 ¥ */
    fun formatCsv(cents: Long): String {
        val abs = kotlin.math.abs(cents)
        val body = "%d.%02d".format(abs / 100, abs % 100)
        return if (cents < 0) "-$body" else body
    }
}
