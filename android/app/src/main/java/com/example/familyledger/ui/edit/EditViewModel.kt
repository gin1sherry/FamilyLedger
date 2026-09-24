package com.example.familyledger.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.data.model.Categories
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.domain.usecase.BudgetAlert
import com.example.familyledger.domain.usecase.CheckBudgetAlertUseCase
import com.example.familyledger.domain.usecase.RecordError
import com.example.familyledger.domain.usecase.UpdateRecordUseCase
import com.example.familyledger.domain.util.Money
import com.example.familyledger.domain.util.MonthRange
import com.example.familyledger.ui.budget.BudgetAlertBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditFormState(
    val loaded: Boolean = false,
    val name: String = "",
    val priceText: String = "",
    val category: String = Categories.DEFAULT,
    val dateMillis: Long = System.currentTimeMillis(),
    val brand: String = "",
    val store: String = "",
    val note: String = "",
    val source: String = "manual",
    val saving: Boolean = false,
    val error: String? = null,
    val deleted: Boolean = false,
    val budgetAlert: BudgetAlert? = null
)

@HiltViewModel
class EditViewModel @Inject constructor(
    private val records: RecordRepository,
    private val updateRecord: UpdateRecordUseCase,
    private val checkBudget: CheckBudgetAlertUseCase,
    private val alertBus: BudgetAlertBus,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {
    private val recordId: Long = savedStateHandle.get<Long>("recordId") ?: -1L

    private val _state = MutableStateFlow(EditFormState())
    val state: StateFlow<EditFormState> = _state.asStateFlow()

    private var original: Record? = null

    init {
        viewModelScope.launch {
            val r = records.getById(recordId)
            if (r == null) {
                _state.update { it.copy(loaded = true, error = "记录不存在") }
            } else {
                original = r
                _state.update {
                    it.copy(
                        loaded = true,
                        name = r.name,
                        priceText = Money.formatCsv(r.price),
                        category = r.category,
                        dateMillis = r.date,
                        brand = r.brand.orEmpty(),
                        store = r.store.orEmpty(),
                        note = r.note.orEmpty(),
                        source = r.source
                    )
                }
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v, error = null) }
    fun onPrice(v: String) = _state.update { it.copy(priceText = v, error = null) }
    fun onCategory(v: String) = _state.update { it.copy(category = v) }
    fun onDate(v: Long) = _state.update { it.copy(dateMillis = v) }
    fun onBrand(v: String) = _state.update { it.copy(brand = v) }
    fun onStore(v: String) = _state.update { it.copy(store = v) }
    fun onNote(v: String) = _state.update { it.copy(note = v) }

    fun save(onSuccess: () -> Unit) {
        val base = original ?: return
        val s = _state.value
        val price = s.priceText.trim().toDoubleOrNull()
        if (price == null) {
            _state.update { it.copy(error = "请填写有效的实付金额") }
            return
        }
        val updated = base.copy(
            name = s.name.trim(),
            price = Money.yuanToCents(price),
            date = s.dateMillis,
            category = s.category,
            brand = s.brand.trim().ifBlank { null },
            store = s.store.trim().ifBlank { null },
            note = s.note.trim().ifBlank { null },
            updatedAt = System.currentTimeMillis()
        )
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            updateRecord(updated)
                .onSuccess {
                    val alert = checkBudget(
                        MonthRange.fromMillis(updated.date).yearMonth,
                        updated.category
                    )
                    if (alert != null) alertBus.publish(alert)
                    _state.update { it.copy(saving = false, budgetAlert = alert) }
                    onSuccess()
                }
                .onFailure { e ->
                    val msg = when (e) {
                        is RecordError -> e.message ?: "保存失败"
                        else -> e.message ?: "保存失败"
                    }
                    _state.update { it.copy(saving = false, error = msg) }
                }
        }
    }
}
