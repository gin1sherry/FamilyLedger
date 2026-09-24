package com.example.familyledger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyledger.data.local.entity.Budget
import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.data.repository.SettingsRepository
import com.example.familyledger.domain.budget.BudgetPolicy
import com.example.familyledger.domain.usecase.BudgetAlert
import com.example.familyledger.domain.util.Money
import com.example.familyledger.domain.util.MonthLabel
import com.example.familyledger.domain.util.MonthRange
import com.example.familyledger.ui.budget.BudgetAlertBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayGroup(
    val dayLabel: String,
    val records: List<Record>
)

data class CategoryProgress(
    val category: String,
    val spentCents: Long,
    val budgetCents: Long
) {
    val percent: Int get() = BudgetPolicy.alertPercent(spentCents, budgetCents)
}

data class HomeUiState(
    val yearMonth: String = MonthRange.now().yearMonth,
    val monthLabel: String = "",
    val totalSpentCents: Long = 0L,
    val monthlyBudgetCents: Long = 0L,
    val categoryProgress: List<CategoryProgress> = emptyList(),
    val groups: List<DayGroup> = emptyList(),
    val visibleCount: Int = PAGE_SIZE,
    val loading: Boolean = true,
    val alert: BudgetAlert? = null
) {
    val hasMore: Boolean get() = groups.sumOf { it.records.size } > visibleCount
    val budgetText: String
        get() = if (monthlyBudgetCents > 0) {
            "/ ${Money.formatYuan(monthlyBudgetCents, withSymbol = true)}"
        } else {
            ""
        }

    val visibleGroups: List<DayGroup>
        get() {
            var remain = visibleCount
            return groups.mapNotNull { g ->
                if (remain <= 0) return@mapNotNull null
                val take = minOf(remain, g.records.size)
                remain -= take
                g.copy(records = g.records.take(take))
            }
        }

    companion object {
        const val PAGE_SIZE = 20
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val records: RecordRepository,
    settings: SettingsRepository,
    alertBus: BudgetAlertBus
) : ViewModel() {

    private val yearMonth = MutableStateFlow(MonthRange.now().yearMonth)
    private val visibleCount = MutableStateFlow(HomeUiState.PAGE_SIZE)
    private val alert = MutableStateFlow<BudgetAlert?>(null)

    init {
        viewModelScope.launch {
            alertBus.alerts.collect { alert.value = it }
        }
    }

    private data class MonthData(
        val list: List<Record>,
        val spent: Long,
        val budgets: List<Budget>
    )

    val uiState: StateFlow<HomeUiState> = combine(
        yearMonth,
        visibleCount,
        settings.monthlyBudgetCents,
        alert,
        yearMonth.flatMapLatest { ym ->
            combine(
                records.observeMonth(MonthRange.fromYearMonth(ym)),
                records.observeTotalSpent(MonthRange.fromYearMonth(ym)),
                records.observeBudgets(ym)
            ) { list, spent, budgets -> MonthData(list, spent, budgets) }
        }
    ) { ym, count, budget, alertVal, data ->
        val groups = data.list.groupBy { dayKey(it.date) }
            .toSortedMap(compareByDescending { it })
            .map { (day, items) -> DayGroup(day, items) }

        val spentByCat = data.list.groupBy { it.category }
            .mapValues { (_, rs) -> rs.sumOf { it.price } }

        val catProgress = data.budgets
            .filter { it.amount > 0 && it.category != BudgetPolicy.CATEGORY_ALL }
            .map { b ->
                CategoryProgress(
                    category = b.category,
                    spentCents = spentByCat[b.category] ?: 0L,
                    budgetCents = b.amount
                )
            }
            .sortedByDescending { it.percent }

        HomeUiState(
            yearMonth = ym,
            monthLabel = MonthLabel.of(ym),
            totalSpentCents = data.spent,
            monthlyBudgetCents = budget,
            categoryProgress = catProgress,
            groups = groups,
            visibleCount = count,
            loading = false,
            alert = alertVal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun previousMonth() = yearMonth.update { MonthLabel.shift(it, -1) }

    fun nextMonth() = yearMonth.update { MonthLabel.shift(it, 1) }

    fun loadMore() {
        visibleCount.update { it + HomeUiState.PAGE_SIZE }
    }

    fun dismissAlert() {
        alert.value = null
    }

    private fun dayKey(millis: Long): String {
        val c = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        return "%02d-%02d".format(c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }
}
