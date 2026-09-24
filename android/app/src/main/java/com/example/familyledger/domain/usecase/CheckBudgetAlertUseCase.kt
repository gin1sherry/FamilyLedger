package com.example.familyledger.domain.usecase

import com.example.familyledger.data.local.entity.Budget
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.data.repository.SettingsRepository
import com.example.familyledger.domain.budget.BudgetPolicy
import com.example.familyledger.domain.util.MonthRange
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class BudgetAlert(
    val yearMonth: String,
    val category: String?, // null = 总预算
    val spentCents: Long,
    val budgetCents: Long,
    val percent: Int
)

/**
 * 记录变更后检查分类预算与月总预算是否达到 80%，需要时返回横幅并标记 notified。
 */
class CheckBudgetAlertUseCase @Inject constructor(
    private val records: RecordRepository,
    private val settings: SettingsRepository
) {
    suspend operator fun invoke(yearMonth: String, category: String): BudgetAlert? {
        checkCategory(yearMonth, category)?.let { return it }
        return checkTotal(yearMonth)
    }

    private suspend fun checkCategory(yearMonth: String, category: String): BudgetAlert? {
        val catBudget = records.getBudget(yearMonth, category) ?: return null
        if (catBudget.amount <= 0) return null
        val spent = records.categorySpent(yearMonth, category)
        return when {
            BudgetPolicy.shouldAlert(spent, catBudget.amount) && !catBudget.notified -> {
                records.markNotified(yearMonth, category)
                BudgetAlert(
                    yearMonth = yearMonth,
                    category = category,
                    spentCents = spent,
                    budgetCents = catBudget.amount,
                    percent = BudgetPolicy.alertPercent(spent, catBudget.amount)
                )
            }
            BudgetPolicy.shouldResetNotified(spent, catBudget.amount) && catBudget.notified -> {
                records.resetNotified(yearMonth, category)
                null
            }
            else -> null
        }
    }

    private suspend fun checkTotal(yearMonth: String): BudgetAlert? {
        val totalBudget = settings.monthlyBudgetCents.first()
        if (totalBudget <= 0) return null
        val range = MonthRange.fromYearMonth(yearMonth)
        val totalSpent = records.observeTotalSpent(range).first()
        var row = records.getBudget(yearMonth, BudgetPolicy.CATEGORY_ALL)
        if (row == null) {
            records.upsertBudget(
                Budget(
                    yearMonth = yearMonth,
                    category = BudgetPolicy.CATEGORY_ALL,
                    amount = 0L,
                    notified = false
                )
            )
            row = records.getBudget(yearMonth, BudgetPolicy.CATEGORY_ALL)
        }
        val notified = row?.notified == true
        return when {
            BudgetPolicy.shouldAlert(totalSpent, totalBudget) && !notified -> {
                records.markNotified(yearMonth, BudgetPolicy.CATEGORY_ALL)
                BudgetAlert(
                    yearMonth = yearMonth,
                    category = null,
                    spentCents = totalSpent,
                    budgetCents = totalBudget,
                    percent = BudgetPolicy.alertPercent(totalSpent, totalBudget)
                )
            }
            BudgetPolicy.shouldResetNotified(totalSpent, totalBudget) && notified -> {
                records.resetNotified(yearMonth, BudgetPolicy.CATEGORY_ALL)
                null
            }
            else -> null
        }
    }
}
