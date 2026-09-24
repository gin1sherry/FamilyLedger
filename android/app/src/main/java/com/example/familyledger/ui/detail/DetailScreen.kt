package com.example.familyledger.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.domain.usecase.DeleteRecordUseCase
import com.example.familyledger.domain.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val records: RecordRepository,
    private val deleteRecord: DeleteRecordUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val recordId: Long = savedStateHandle.get<Long>("recordId") ?: -1L

    val record: StateFlow<Record?> = records.observeRecord(recordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _deleteState = MutableStateFlowHelper()
    val busy: StateFlow<Boolean> = _deleteState.busy
    val error: StateFlow<String?> = _deleteState.error

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _deleteState.busy.value = true
            deleteRecord(recordId)
                .onSuccess {
                    _deleteState.busy.value = false
                    onDeleted()
                }
                .onFailure { e ->
                    _deleteState.busy.value = false
                    _deleteState.error.value = e.message ?: "删除失败"
                }
        }
    }

    fun clearError() {
        _deleteState.error.value = null
    }
}

private class MutableStateFlowHelper {
    val busy = kotlinx.coroutines.flow.MutableStateFlow(false)
    val error = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    onProduct: (Long) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val record by viewModel.record.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LaunchedEffect(record) {
        if (record == null && !busy) {
            // may mean deleted — handled by onDeleted nav; initial null before load is ok
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除这条记录？") },
            text = { Text("删除后汇总与预算将自动重算，不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.delete(onDeleted = onDeleted)
                    }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    error?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("操作失败") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("好") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        val r = record
        if (r == null) {
            Text(
                text = if (busy) "处理中…" else "记录不存在或已删除",
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(r.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    Money.formatYuan(r.price, withSymbol = true),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text("分类：${r.category}", style = MaterialTheme.typography.bodyLarge)
                Text("时间：${fmt.format(Date(r.date))}", style = MaterialTheme.typography.bodyMedium)
                r.store?.let { Text("店铺：$it", style = MaterialTheme.typography.bodyMedium) }
                r.brand?.let { Text("品牌：$it", style = MaterialTheme.typography.bodyMedium) }
                r.note?.let { Text("备注：$it", style = MaterialTheme.typography.bodyMedium) }
                Text(
                    "来源：${r.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onProduct(r.id) },
                        enabled = !busy
                    ) { Text("价格走势") }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        enabled = !busy
                    ) { Text("删除") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onEdit(r.id) },
                        enabled = !busy
                    ) { Text("编辑") }
                }
            }
        }
    }
}
