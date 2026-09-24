package com.example.familyledger.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyledger.data.local.entity.Budget
import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.data.model.Categories
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.data.repository.SettingsRepository
import com.example.familyledger.domain.budget.BudgetPolicy
import com.example.familyledger.domain.usecase.SaveBudgetUseCase
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

data class CategoryBudgetRow(
    val category: String,
    val budgetCents: Long,
    val spentCents: Long,
    val inputText: String
)

data class BudgetUiState(
    val yearMonth: String = MonthRange.now().yearMonth,
    val monthLabel: String = "",
    val monthlyBudgetInput: String = "",
    val monthlyBudgetCents: Long = 0L,
    val rows: List<CategoryBudgetRow> = emptyList(),
    val saving: Boolean = false,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val records: RecordRepository,
    private val settings: SettingsRepository,
    private val saveBudget: SaveBudgetUseCase
) : ViewModel() {

    private val yearMonth = MutableStateFlow(MonthRange.now().yearMonth)
    private val catInputs = MutableStateFlow<Map<String, String>>(emptyMap())
    private val monthlyInput = MutableStateFlow<String?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val saving = MutableStateFlow(false)

    private data class Loaded(
        val ym: String,
        val totalBudget: Long,
        val budgets: List<Budget>,
        val monthRecords: List<Record>
    )

    private val loaded: StateFlow<Loaded> = combine(
        yearMonth,
        settings.monthlyBudgetCents,
        yearMonth.flatMapLatest { ym ->
            combine(
                records.observeBudgets(ym),
                records.observeMonth(MonthRange.fromYearMonth(ym))
            ) { b, r -> b to r }
        }
    ) { ym, total, pair ->
        Loaded(ym, total, pair.first, pair.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loaded(MonthRange.now().yearMonth, 0, emptyList(), emptyList()))

    val uiState: StateFlow<BudgetUiState> = combine(
        loaded,
        monthlyInput,
        catInputs,
        message,
        saving
    ) { data, mInput, inps, msg, sav ->
        val budgetMap = data.budgets
            .filter { it.category != BudgetPolicy.CATEGORY_ALL }
            .associateBy { it.category }
        val spentByCat = data.monthRecords
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.price } }

        val rows = Categories.ALL.map { cat ->
            val b = budgetMap[cat]
            CategoryBudgetRow(
                category = cat,
                budgetCents = b?.amount ?: 0L,
                spentCents = spentByCat[cat] ?: 0L,
                inputText = inps[cat] ?: if (b != null) Money.formatCsv(b.amount) else ""
            )
        }

        BudgetUiState(
            yearMonth = data.ym,
            monthLabel = MonthLabel.of(data.ym),
            monthlyBudgetInput = mInput ?: if (data.totalBudget > 0) Money.formatCsv(data.totalBudget) else "",
            monthlyBudgetCents = data.totalBudget,
            rows = rows,
            saving = sav,
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetUiState())

    fun previousMonth() {
        yearMonth.update { MonthLabel.shift(it, -1) }
        catInputs.value = emptyMap()
        monthlyInput.value = null
    }

    fun nextMonth() {
        yearMonth.update { MonthLabel.shift(it, 1) }
        catInputs.value = emptyMap()
        monthlyInput.value = null
    }

    fun onMonthlyInput(v: String) = monthlyInput.update { v }

    fun onCategoryInput(cat: String, v: String) {
        catInputs.update { it + (cat to v) }
    }

    fun clearMessage() = message.update { null }

    fun saveAll(onDone: (() -> Unit)? = null) {
        val state = uiState.value
        saving.value = true
        message.value = null
        viewModelScope.launch {
            try {
                val monthly = state.monthlyBudgetInput.trim().toDoubleOrNull() ?: 0.0
                saveBudget.invokeTotal(monthly).getOrThrow()

                state.rows.forEach { row ->
                    val text = row.inputText
                    val amount = if (text.isBlank()) 0.0 else (text.toDoubleOrNull() ?: 0.0)
                    saveBudget(state.yearMonth, row.category, amount).getOrThrow()
                }

                monthlyInput.value = null
                catInputs.value = emptyMap()
                message.value = "预算已保存"
                onDone?.invoke()
            } catch (e: Exception) {
                message.value = e.message ?: "保存失败"
            } finally {
                saving.value = false
            }
        }
    }
}
