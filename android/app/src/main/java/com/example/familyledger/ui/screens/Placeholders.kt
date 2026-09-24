package com.example.familyledger.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.familyledger.BuildConfig
import com.example.familyledger.data.repository.SettingsRepository
import com.example.familyledger.ui.theme.HyperColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiFormState(
    val apiKey: String = "",
    val baseUrl: String = "",
    val model: String = "",
    val loaded: Boolean = false,
    val saving: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    private val _form = MutableStateFlow(AiFormState())
    val form: StateFlow<AiFormState> = _form.asStateFlow()

    init {
        viewModelScope.launch {
            val cfg = settings.aiConfig()
            _form.update {
                it.copy(
                    apiKey = cfg.apiKey,
                    baseUrl = cfg.baseUrl.ifBlank { BuildConfig.AI_BASE_URL },
                    model = cfg.model.ifBlank { BuildConfig.AI_MODEL },
                    loaded = true
                )
            }
        }
    }

    fun onApiKey(v: String) = _form.update { it.copy(apiKey = v, message = null) }
    fun onBaseUrl(v: String) = _form.update { it.copy(baseUrl = v, message = null) }
    fun onModel(v: String) = _form.update { it.copy(model = v, message = null) }

    fun saveAi() {
        val s = _form.value
        if (s.baseUrl.isBlank()) {
            _form.update { it.copy(message = "Base URL 不能为空", isError = true) }
            return
        }
        _form.update { it.copy(saving = true, message = null, isError = false) }
        viewModelScope.launch {
            try {
                settings.setAiConfig(
                    apiKey = s.apiKey.trim(),
                    baseUrl = s.baseUrl.trim(),
                    model = s.model.trim()
                )
                // 回读确认已写入
                val saved = settings.aiConfig()
                val keyOk = saved.apiKey == s.apiKey.trim() || s.apiKey.isBlank()
                _form.update {
                    it.copy(
                        saving = false,
                        apiKey = saved.apiKey,
                        baseUrl = saved.baseUrl,
                        model = saved.model,
                        message = if (keyOk || s.apiKey.isNotBlank()) {
                            "AI 配置已保存到本机"
                        } else {
                            "保存后未读到 Key，请重试"
                        },
                        isError = !keyOk && s.apiKey.isNotBlank()
                    )
                }
            } catch (e: Exception) {
                _form.update {
                    it.copy(
                        saving = false,
                        message = "保存失败：" + (e.message ?: "未知错误"),
                        isError = true
                    )
                }
            }
        }
    }

    fun clearMessage() = _form.update { it.copy(message = null, isError = false) }
}

@Composable
fun SettingsScreen(
    onBudgetClick: () -> Unit,
    onExportClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(form.message) {
        form.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineSmall)

        Text(
            text = "分类预算与月总预算",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBudgetClick)
                .padding(vertical = 12.dp)
        )
        Text(
            text = "数据导出（CSV）",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExportClick)
                .padding(vertical = 12.dp)
        )

        Text(
            "AI 识别配置",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "仅识别时联网；保存在本机 DataStore。留空 Key 则回退编译期 local.properties。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = HyperColors.Orange,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )

        OutlinedTextField(
            value = form.apiKey,
            onValueChange = viewModel::onApiKey,
            label = { Text("API Key") },
            singleLine = true,
            enabled = form.loaded && !form.saving,
            visualTransformation = PasswordVisualTransformation(),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.baseUrl,
            onValueChange = viewModel::onBaseUrl,
            label = { Text("Base URL（OpenAI 兼容）") },
            singleLine = true,
            enabled = form.loaded && !form.saving,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.model,
            onValueChange = viewModel::onModel,
            label = { Text("模型名") },
            singleLine = true,
            enabled = form.loaded && !form.saving,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        form.message?.let { msg ->
            Text(
                text = msg,
                color = if (form.isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = viewModel::saveAi,
            enabled = form.loaded && !form.saving,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(99.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HyperColors.Orange,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(if (form.saving) "保存中…" else "保存 AI 配置")
        }

        Text(
            text = "桌面小部件：长按桌面 → 小部件 → 家庭账本",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
