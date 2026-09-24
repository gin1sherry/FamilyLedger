package com.example.familyledger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.data.repository.SettingsRepository
import com.example.familyledger.domain.util.Money
import com.example.familyledger.domain.util.MonthLabel
import com.example.familyledger.domain.util.MonthRange
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
    val dayLabel: String, // MM-DD
    val records: List<Record>
)

data class HomeUiState(
    val yearMonth: String = MonthRange.now().yearMonth,
    val monthLabel: String = "",
    val totalSpentCents: Long = 0L,
    val monthlyBudgetCents: Long = 0L,
    val groups: List<DayGroup> = emptyList(),
    val visibleCount: Int = PAGE_SIZE,
    val loading: Boolean = true
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
    settings: SettingsRepository
) : ViewModel() {

    private val yearMonth = MutableStateFlow(MonthRange.now().yearMonth)
    private val visibleCount = MutableStateFlow(HomeUiState.PAGE_SIZE)

    val uiState: StateFlow<HomeUiState> = combine(
        yearMonth,
        visibleCount,
        yearMonth.flatMapLatest { ym ->
            records.observeMonth(MonthRange.fromYearMonth(ym))
        },
        yearMonth.flatMapLatest { ym ->
            records.observeTotalSpent(MonthRange.fromYearMonth(ym))
        },
        settings.monthlyBudgetCents
    ) { ym, count, list, spent, budget ->
        val groups = list.groupBy { dayKey(it.date) }
            .toSortedMap(compareByDescending { it })
            .map { (day, items) -> DayGroup(day, items) }
        HomeUiState(
            yearMonth = ym,
            monthLabel = MonthLabel.of(ym),
            totalSpentCents = spent,
            monthlyBudgetCents = budget,
            groups = groups,
            visibleCount = count,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun previousMonth() = yearMonth.update { MonthLabel.shift(it, -1) }

    fun nextMonth() = yearMonth.update { MonthLabel.shift(it, 1) }

    fun loadMore() {
        visibleCount.update { it + HomeUiState.PAGE_SIZE }
    }

    fun delete(id: Long) {
        viewModelScope.launch { records.deleteRecord(id) }
    }

    private fun dayKey(millis: Long): String {
        val c = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        return "%02d-%02d".format(c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }
}
