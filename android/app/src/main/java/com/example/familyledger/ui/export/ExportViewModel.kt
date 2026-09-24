package com.example.familyledger.ui.export

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyledger.domain.usecase.ExportCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ExportUiState(
    val exporting: Boolean = false,
    val message: String? = null,
    val file: File? = null
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportCsv: ExportCsvUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    fun clear() = _state.update { ExportUiState() }

    fun export() {
        _state.update { it.copy(exporting = true, message = null) }
        viewModelScope.launch {
            exportCsv()
                .onSuccess { file ->
                    _state.update {
                        it.copy(exporting = false, message = "导出成功：${file.name}", file = file)
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(exporting = false, message = e.message ?: "导出失败")
                    }
                }
        }
    }

    fun shareIntent(): Intent? {
        val file = _state.value.file ?: return null
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
