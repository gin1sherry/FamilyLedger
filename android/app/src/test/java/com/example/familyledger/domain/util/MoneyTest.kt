package com.example.familyledger.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {
    @Test
    fun yuanToCents_rounds() {
        assertEquals(1250L, Money.yuanToCents(12.5))
        assertEquals(1L, Money.yuanToCents(0.01))
        assertEquals(0L, Money.yuanToCents(0.0))
        assertEquals(799900L, Money.yuanToCents(7999.0))
    }

    @Test
    fun yuanToCents_fromString() {
        assertEquals(1250L, Money.yuanToCents("12.50"))
        assertEquals(0L, Money.yuanToCents("abc"))
    }

    @Test
    fun formatCsv_twoDecimals() {
        assertEquals("12.50", Money.formatCsv(1250L))
        assertEquals("7999.00", Money.formatCsv(799900L))
        assertEquals("-3.05", Money.formatCsv(-305L))
    }

    @Test
    fun formatYuan_withSymbol() {
        assertEquals("¥12.50", Money.formatYuan(1250L, withSymbol = true))
        assertEquals("¥8", Money.formatYuan(800L, withSymbol = true))
        assertEquals("-¥3.05", Money.formatYuan(-305L, withSymbol = true))
    }
}
