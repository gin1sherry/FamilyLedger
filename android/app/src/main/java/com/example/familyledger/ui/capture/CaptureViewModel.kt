package com.example.familyledger.ui.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyledger.data.model.Categories
import com.example.familyledger.data.model.ImageType
import com.example.familyledger.data.model.RecordDraft
import com.example.familyledger.data.model.RecognitionResult
import com.example.familyledger.data.remote.RecognitionErrorKind
import com.example.familyledger.data.remote.RecognitionException
import com.example.familyledger.data.repository.AiRepository
import com.example.familyledger.data.repository.PendingImageRepository
import com.example.familyledger.domain.usecase.AddRecordUseCase
import com.example.familyledger.domain.usecase.CheckBudgetAlertUseCase
import com.example.familyledger.domain.usecase.RecordError
import com.example.familyledger.domain.util.Money
import com.example.familyledger.domain.util.MonthRange
import com.example.familyledger.ui.budget.BudgetAlertBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CaptureStage { PICK, RECOGNIZING, CONFIRM, ERROR }

data class ConfirmItem(
    val selected: Boolean = true,
    val name: String = "",
    val priceText: String = "",
    val category: String = Categories.DEFAULT
)

data class CaptureUiState(
    val stage: CaptureStage = CaptureStage.PICK,
    val imageType: ImageType = ImageType.PRODUCT,
    val imageUri: Uri? = null,
    val recognition: RecognitionResult? = null,
    val errorKind: RecognitionErrorKind? = null,
    val errorMessage: String? = null,
    val name: String = "",
    val priceText: String = "",
    val category: String = Categories.DEFAULT,
    val dateMillis: Long = System.currentTimeMillis(),
    val brand: String = "",
    val store: String = "",
    val note: String = "",
    val extras: List<Pair<String, String>> = emptyList(),
    val confidence: Double? = null,
    val receiptItems: List<ConfirmItem> = emptyList(),
    val isReceipt: Boolean = false,
    val batchId: String? = null,
    val saving: Boolean = false,
    val formError: String? = null
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val pendingImages: PendingImageRepository,
    private val addRecord: AddRecordUseCase,
    private val checkBudget: CheckBudgetAlertUseCase,
    private val alertBus: BudgetAlertBus
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureUiState())
    val state: StateFlow<CaptureUiState> = _state.asStateFlow()

    fun setImageType(t: ImageType) = _state.update { it.copy(imageType = t) }

    fun onImagePicked(uri: Uri) {
        _state.update {
            it.copy(
                imageUri = uri,
                stage = CaptureStage.PICK,
                errorKind = null,
                errorMessage = null
            )
        }
    }

    fun startRecognize() {
        val uri = _state.value.imageUri ?: return
        val type = _state.value.imageType
        _state.update { it.copy(stage = CaptureStage.RECOGNIZING, errorKind = null, errorMessage = null) }
        viewModelScope.launch {
            aiRepository.recognize(uri, type)
                .onSuccess { applyRecognition(it) }
                .onFailure { e ->
                    val kind = (e as? RecognitionException)?.kind ?: RecognitionErrorKind.UNKNOWN
                    if (kind == RecognitionErrorKind.TIMEOUT || kind == RecognitionErrorKind.HTTP) {
                        // still surface as error; offline specifically when ConnectException-like via TIMEOUT message
                    }
                    pendingImages.save(uri.toString(), type.apiValue)
                    _state.update {
                        it.copy(
                            stage = CaptureStage.ERROR,
                            errorKind = kind,
                            errorMessage = e.message ?: "识别失败"
                        )
                    }
                }
        }
    }

    private fun applyRecognition(result: RecognitionResult) {
        val isReceipt = result.mode == "receipt" || result.items.isNotEmpty()
        val date = result.date ?: System.currentTimeMillis()
        if (isReceipt) {
            val items = result.items.map {
                ConfirmItem(
                    selected = true,
                    name = it.name.orEmpty(),
                    priceText = it.price?.let { p -> Money.formatCsv(Money.yuanToCents(p)) }.orEmpty(),
                    category = if (Categories.isValid(it.category ?: "")) it.category!! else Categories.DEFAULT
                )
            }
            _state.update {
                it.copy(
                    stage = CaptureStage.CONFIRM,
                    recognition = result,
                    isReceipt = true,
                    receiptItems = items,
                    dateMillis = date,
                    store = result.store.orEmpty(),
                    confidence = result.confidence,
                    batchId = "b_${System.currentTimeMillis()}",
                    extras = result.extras.map { (k, v) -> k to v }
                )
            }
        } else {
            val cat = result.category?.takeIf { Categories.isValid(it) } ?: Categories.DEFAULT
            _state.update {
                it.copy(
                    stage = CaptureStage.CONFIRM,
                    recognition = result,
                    isReceipt = false,
                    name = result.name.orEmpty(),
                    priceText = result.price?.let { p -> Money.formatCsv(Money.yuanToCents(p)) }.orEmpty(),
                    category = cat,
                    dateMillis = date,
                    brand = result.brand.orEmpty(),
                    store = result.store.orEmpty(),
                    note = result.note.orEmpty(),
                    confidence = result.confidence,
                    extras = result.extras.map { (k, v) -> k to v }
                )
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v, formError = null) }
    fun onPrice(v: String) = _state.update { it.copy(priceText = v, formError = null) }
    fun onCategory(v: String) = _state.update { it.copy(category = v) }
    fun onDate(v: Long) = _state.update { it.copy(dateMillis = v) }
    fun onBrand(v: String) = _state.update { it.copy(brand = v) }
    fun onStore(v: String) = _state.update { it.copy(store = v) }
    fun onNote(v: String) = _state.update { it.copy(note = v) }

    fun toggleReceiptItem(index: Int) {
        _state.update { s ->
            val items = s.receiptItems.toMutableList()
            val item = items[index]
            items[index] = item.copy(selected = !item.selected)
            s.copy(receiptItems = items)
        }
    }

    fun updateReceiptItem(index: Int, name: String? = null, price: String? = null, category: String? = null) {
        _state.update { s ->
            val items = s.receiptItems.toMutableList()
            val item = items[index]
            items[index] = item.copy(
                name = name ?: item.name,
                priceText = price ?: item.priceText,
                category = category ?: item.category
            )
            s.copy(receiptItems = items)
        }
    }

    fun removeReceiptItem(index: Int) {
        _state.update { s ->
            s.copy(receiptItems = s.receiptItems.filterIndexed { i, _ -> i != index })
        }
    }

    fun resetToPick() {
        _state.value = CaptureUiState(imageType = _state.value.imageType)
    }

    fun toManualFallback() {
        _state.update {
            it.copy(
                stage = CaptureStage.CONFIRM,
                isReceipt = false,
                recognition = null,
                confidence = null,
                formError = null
            )
        }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            try {
                if (s.isReceipt) {
                    val batch = s.batchId ?: "b_${System.currentTimeMillis()}"
                    val source = s.recognition?.source ?: "receipt"
                    var any = false
                    s.receiptItems.filter { it.selected }.forEach { item ->
                        if (item.name.isBlank()) return@forEach
                        val price = item.priceText.toDoubleOrNull() ?: return@forEach
                        val draft = RecordDraft(
                            name = item.name.trim(),
                            priceYuan = price,
                            date = s.dateMillis,
                            category = item.category,
                            store = s.store.ifBlank { s.recognition?.store },
                            paymentMethod = s.recognition?.paymentMethod,
                            orderNo = s.recognition?.orderNo,
                            source = source,
                            sourceBatchId = batch,
                            extras = s.extras
                        )
                        addRecord(draft).getOrThrow()
                        any = true
                        val alert = checkBudget(MonthRange.fromMillis(s.dateMillis).yearMonth, draft.category)
                        if (alert != null) alertBus.publish(alert)
                    }
                    if (!any) {
                        _state.update { it.copy(saving = false, formError = "至少勾选一条有效条目") }
                        return@launch
                    }
                } else {
                    val price = s.priceText.trim().toDoubleOrNull()
                    if (price == null) {
                        _state.update { it.copy(saving = false, formError = "请填写有效金额") }
                        return@launch
                    }
                    val source = s.recognition?.source ?: "manual"
                    val draft = RecordDraft(
                        name = s.name.trim(),
                        priceYuan = price,
                        date = s.dateMillis,
                        category = s.category,
                        brand = s.brand.ifBlank { null },
                        store = s.store.ifBlank { null },
                        note = s.note.ifBlank { null },
                        source = source,
                        extras = s.extras
                    )
                    addRecord(draft).onFailure { e ->
                        val msg = when (e) {
                            is RecordError -> e.message ?: "保存失败"
                            else -> e.message ?: "保存失败"
                        }
                        _state.update { it.copy(saving = false, formError = msg) }
                        return@launch
                    }
                    val alert = checkBudget(MonthRange.fromMillis(s.dateMillis).yearMonth, draft.category)
                    if (alert != null) alertBus.publish(alert)
                }
                pendingImages.clear()
                _state.update { it.copy(saving = false) }
                onDone()
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, formError = e.message ?: "保存失败") }
            }
        }
    }
}
