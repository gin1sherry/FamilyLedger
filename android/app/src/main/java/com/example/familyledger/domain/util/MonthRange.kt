package com.example.familyledger.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 自然月时间区间 [start, end) */
data class MonthRange(val start: Long, val end: Long, val yearMonth: String) {
    fun contains(millis: Long): Boolean = millis in start until end

    companion object {
        fun of(year: Int, month: Int): MonthRange {
            require(month in 1..12) { "month must be 1..12" }
            val s = LocalDate.of(year, month, 1)
            val e = s.plusMonths(1)
            val zone = ZoneId.systemDefault()
            return MonthRange(
                start = s.atStartOfDay(zone).toInstant().toEpochMilli(),
                end = e.atStartOfDay(zone).toInstant().toEpochMilli(),
                yearMonth = "%04d-%02d".format(year, month)
            )
        }

        fun fromYearMonth(yearMonth: String): MonthRange {
            val parts = yearMonth.split("-")
            require(parts.size == 2) { "yearMonth format yyyy-MM" }
            return of(parts[0].toInt(), parts[1].toInt())
        }

        fun fromMillis(millis: Long): MonthRange {
            val d = LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            return of(d.year, d.monthValue)
        }

        fun now(): MonthRange = fromMillis(System.currentTimeMillis())

        fun parseYearMonth(yearMonth: String): Pair<Int, Int> {
            val parts = yearMonth.split("-")
            return parts[0].toInt() to parts[1].toInt()
        }
    }
}
