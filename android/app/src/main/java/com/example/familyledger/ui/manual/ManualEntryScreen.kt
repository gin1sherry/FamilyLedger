package com.example.familyledger.ui.manual

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.familyledger.data.model.Categories
import com.example.familyledger.ui.theme.HyperColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ManualViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dateFmt = rememberDateFmt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("手动记账") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onName,
                label = { Text("商品名称 *") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.priceText,
                onValueChange = viewModel::onPrice,
                label = { Text("实付金额 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val picked = Calendar.getInstance().apply {
                                set(y, m, d)
                                // keep time-of-day from current selection if same day later
                                if (state.dateMillis > 0) {
                                    val old = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                                    set(java.util.Calendar.HOUR_OF_DAY, old.get(java.util.Calendar.HOUR_OF_DAY))
                                    set(java.util.Calendar.MINUTE, old.get(java.util.Calendar.MINUTE))
                                }
                            }
                            viewModel.onDate(picked.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("日期：${dateFmt.format(Date(state.dateMillis))}")
            }

            Text("分类 *", style = MaterialTheme.typography.labelLarge)
            val rows = Categories.ALL.chunked(5)
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { cat ->
                        FilterChip(
                            selected = state.category == cat,
                            onClick = { viewModel.onCategory(cat) },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(99.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HyperColors.OrangeSoft,
                                selectedLabelColor = HyperColors.OrangeDeep
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("扩展信息（选填）", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = state.brand,
                onValueChange = viewModel::onBrand,
                label = { Text("品牌") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.store,
                onValueChange = viewModel::onStore,
                label = { Text("店铺") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNote,
                label = { Text("备注") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    viewModel.save {
                        onSaved()
                    }
                },
                enabled = !state.saving,
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HyperColors.Orange,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (state.saving) "保存中…" else "保存", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun rememberDateFmt(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
