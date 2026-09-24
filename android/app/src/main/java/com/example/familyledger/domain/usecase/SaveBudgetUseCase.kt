package com.example.familyledger.domain.usecase

import com.example.familyledger.data.local.entity.Budget
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.data.repository.SettingsRepository
import com.example.familyledger.domain.budget.BudgetPolicy
import com.example.familyledger.domain.util.Money
import javax.inject.Inject

/**
 * 保存分类预算或月总预算（amountYuan ≤ 0 表示清除分类预算）。
 * category == [BudgetPolicy.CATEGORY_ALL] 或调用 [invokeTotal] 写总预算。
 */
class SaveBudgetUseCase @Inject constructor(
    private val records: RecordRepository,
    private val settings: SettingsRepository
) {
    suspend fun invokeTotal(amountYuan: Double): Result<Unit> = runCatching {
        settings.setMonthlyBudget(if (amountYuan > 0) amountYuan else 0.0)
    }

    suspend operator fun invoke(
        yearMonth: String,
        category: String,
        amountYuan: Double
    ): Result<Unit> = runCatching {
        if (category == BudgetPolicy.CATEGORY_ALL) {
            settings.setMonthlyBudget(if (amountYuan > 0) amountYuan else 0.0)
            return@runCatching
        }
        val cents = Money.yuanToCents(amountYuan)
        if (cents <= 0) {
            records.clearBudget(yearMonth, category)
            return@runCatching
        }
        val spent = records.categorySpent(yearMonth, category)
        val existing = records.getBudget(yearMonth, category)
        val notified = if (existing != null) {
            if (BudgetPolicy.shouldResetNotified(spent, cents)) false else existing.notified
        } else {
            false
        }
        records.upsertBudget(
            Budget(
                id = existing?.id ?: 0L,
                yearMonth = yearMonth,
                category = category,
                amount = cents,
                notified = notified
            )
        )
    }
}
