package com.example.familyledger.domain.util

object MonthLabel {
    fun of(yearMonth: String): String {
        val parts = yearMonth.split("-")
        if (parts.size != 2) return yearMonth
        return "${parts[0]}年${parts[1].toInt()}月"
    }

    fun shift(yearMonth: String, deltaMonths: Int): String {
        val (y, m) = MonthRange.parseYearMonth(yearMonth)
        var month = m + deltaMonths
        var year = y
        while (month < 1) { month += 12; year -= 1 }
        while (month > 12) { month -= 12; year += 1 }
        return "%04d-%02d".format(year, month)
    }
}
