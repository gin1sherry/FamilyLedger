package com.example.familyledger.ui.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyledger.data.model.Categories
import com.example.familyledger.data.model.RecordDraft
import com.example.familyledger.domain.usecase.AddRecordUseCase
import com.example.familyledger.domain.usecase.RecordError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManualFormState(
    val name: String = "",
    val priceText: String = "",
    val category: String = Categories.DEFAULT,
    val dateMillis: Long = System.currentTimeMillis(),
    val brand: String = "",
    val store: String = "",
    val note: String = "",
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class ManualViewModel @Inject constructor(
    private val addRecord: AddRecordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ManualFormState())
    val state: StateFlow<ManualFormState> = _state.asStateFlow()

    fun onName(v: String) = _state.update { it.copy(name = v, error = null) }

    fun onPrice(v: String) = _state.update { it.copy(priceText = v, error = null) }

    fun onCategory(v: String) = _state.update { it.copy(category = v) }

    fun onDate(v: Long) = _state.update { it.copy(dateMillis = v) }

    fun onBrand(v: String) = _state.update { it.copy(brand = v) }

    fun onStore(v: String) = _state.update { it.copy(store = v) }

    fun onNote(v: String) = _state.update { it.copy(note = v) }

    fun save(onSuccess: () -> Unit) {
        val s = _state.value
        val price = s.priceText.trim().toDoubleOrNull()
        if (price == null) {
            _state.update { it.copy(error = "请填写有效的实付金额") }
            return
        }
        val draft = RecordDraft(
            name = s.name.trim(),
            priceYuan = price,
            date = s.dateMillis,
            category = s.category,
            brand = s.brand.trim().ifBlank { null },
            store = s.store.trim().ifBlank { null },
            note = s.note.trim().ifBlank { null },
            source = "manual"
        )
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            addRecord(draft)
                .onSuccess {
                    _state.update { it.copy(saving = false, saved = true) }
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
