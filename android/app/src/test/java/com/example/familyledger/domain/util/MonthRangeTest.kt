package com.example.familyledger.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthRangeTest {
    @Test
    fun of_september_2026() {
        val r = MonthRange.of(2026, 9)
        assertEquals("2026-09", r.yearMonth)
        assertTrue(r.contains(r.start))
        assertFalse(r.contains(r.end))
        assertTrue(r.contains(r.end - 1))
    }

    @Test
    fun fromYearMonth() {
        val jan = MonthRange.fromYearMonth("2026-01")
        assertEquals("2026-01", jan.yearMonth)
        assertFalse(jan.contains(jan.end))
        val dec = MonthRange.fromYearMonth("2025-12")
        assertEquals("2025-12", dec.yearMonth)
        assertTrue(dec.end > dec.start)
    }

    @Test
    fun parse() {
        val (y, m) = MonthRange.parseYearMonth("2026-09")
        assertEquals(2026, y)
        assertEquals(9, m)
    }
}
