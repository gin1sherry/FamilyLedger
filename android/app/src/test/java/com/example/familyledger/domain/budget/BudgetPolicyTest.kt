package com.example.familyledger.domain.budget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetPolicyTest {
    @Test
    fun shouldAlert_at_80_percent() {
        // 1600 / 2000 = 80%
        assertTrue(BudgetPolicy.shouldAlert(160_000L, 200_000L))
        assertFalse(BudgetPolicy.shouldAlert(159_999L, 200_000L))
        assertTrue(BudgetPolicy.shouldAlert(200_000L, 200_000L))
    }

    @Test
    fun shouldAlert_zero_budget() {
        assertFalse(BudgetPolicy.shouldAlert(100_000L, 0L))
    }

    @Test
    fun resetNotified_when_below_threshold() {
        assertTrue(BudgetPolicy.shouldResetNotified(100_000L, 200_000L))
        assertFalse(BudgetPolicy.shouldResetNotified(160_000L, 200_000L))
    }

    @Test
    fun levels() {
        assertEquals(BudgetPolicy.Level.OK, BudgetPolicy.level(50_000L, 200_000L))
        assertEquals(BudgetPolicy.Level.WARN, BudgetPolicy.level(130_000L, 200_000L))
        assertEquals(BudgetPolicy.Level.CRITICAL, BudgetPolicy.level(160_000L, 200_000L))
    }

    @Test
    fun percent() {
        assertEquals(80, BudgetPolicy.alertPercent(160_000L, 200_000L))
        assertEquals(0, BudgetPolicy.alertPercent(1L, 0L))
    }
}
