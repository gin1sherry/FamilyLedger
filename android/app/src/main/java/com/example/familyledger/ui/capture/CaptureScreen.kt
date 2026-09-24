package com.example.familyledger.ui.capture

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.familyledger.data.model.Categories
import com.example.familyledger.data.model.ImageType
import com.example.familyledger.data.remote.RecognitionErrorKind
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onOpenSettings: () -> Unit = {},
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let(viewModel::onImagePicked) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) pendingCameraUri?.let(viewModel::onImagePicked)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera(context) { uri ->
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    fun openCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            launchCamera(context) { uri ->
                pendingCameraUri = uri
                takePictureLauncher.launch(uri)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state.stage) {
                            CaptureStage.CONFIRM -> if (state.isReceipt) "小票确认" else "确认识别结果"
                            else -> "拍照记账"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (state.stage) {
                            CaptureStage.CONFIRM, CaptureStage.ERROR -> viewModel.resetToPick()
                            else -> onBack()
                        }
                    }) {
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
            when (state.stage) {
                CaptureStage.PICK -> {
                    Text("图片类型", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.imageType == ImageType.PRODUCT,
                            onClick = { viewModel.setImageType(ImageType.PRODUCT) },
                            label = { Text("商品照") }
                        )
                        FilterChip(
                            selected = state.imageType == ImageType.RECEIPT,
                            onClick = { viewModel.setImageType(ImageType.RECEIPT) },
                            label = { Text("购物小票") }
                        )
                        FilterChip(
                            selected = state.imageType == ImageType.ORDER,
                            onClick = { viewModel.setImageType(ImageType.ORDER) },
                            label = { Text("订单截图") }
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        if (state.imageUri != null) {
                            AsyncImage(
                                model = state.imageUri,
                                contentDescription = "预览",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(40.dp))
                                Text("拍照，或从相册/截图选择", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { openCamera() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("相机")
                        }
                        OutlinedButton(
                            onClick = { pickImageLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = null)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("相册/截图")
                        }
                    }

                    if (state.imageUri != null) {
                        Button(
                            onClick = viewModel::startRecognize,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("开始识别") }
                    }

                    TextButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                        Text("配置 API Key…")
                    }
                    TextButton(onClick = viewModel::toManualFallback, modifier = Modifier.fillMaxWidth()) {
                        Text("转手动补录")
                    }
                }

                CaptureStage.RECOGNIZING -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("图片压缩 → 识别中…")
                    }
                }

                CaptureStage.ERROR -> {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                when (state.errorKind) {
                                    RecognitionErrorKind.NO_KEY -> "未配置 AI API Key"
                                    RecognitionErrorKind.TIMEOUT -> "识别超时"
                                    RecognitionErrorKind.HTTP -> "服务异常"
                                    RecognitionErrorKind.BAD_JSON -> "返回数据无效"
                                    RecognitionErrorKind.OFFLINE -> "当前无网络"
                                    else -> "识别失败"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                state.errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    if (state.errorKind == RecognitionErrorKind.NO_KEY) {
                        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                            Text("去设置填写 Key")
                        }
                    } else {
                        Button(onClick = viewModel::startRecognize, modifier = Modifier.fillMaxWidth()) {
                            Text("重试")
                        }
                    }
                    OutlinedButton(onClick = viewModel::toManualFallback, modifier = Modifier.fillMaxWidth()) {
                        Text("转手动补录")
                    }
                }

                CaptureStage.CONFIRM -> {
                    if (state.confidence != null) {
                        val pct = (state.confidence!! * 100).toInt()
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (pct < 60) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                if (pct < 60) "⚠ 置信度 $pct% — 可能不准，请检查" else "识别置信度 $pct%，请确认后保存",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (state.isReceipt) {
                        Text("小票条目（勾选保存）", style = MaterialTheme.typography.labelLarge)
                        state.receiptItems.forEachIndexed { index, item ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = item.selected,
                                            onCheckedChange = { viewModel.toggleReceiptItem(index) }
                                        )
                                        Text("条目 ${index + 1}", modifier = Modifier.weight(1f))
                                        TextButton(onClick = { viewModel.removeReceiptItem(index) }) {
                                            Text("删除")
                                        }
                                    }
                                    OutlinedTextField(
                                        value = item.name,
                                        onValueChange = { viewModel.updateReceiptItem(index, name = it) },
                                        label = { Text("商品名") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = item.priceText,
                                        onValueChange = { viewModel.updateReceiptItem(index, price = it) },
                                        label = { Text("实付") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = viewModel::onName,
                            label = { Text("商品名 *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.priceText,
                            onValueChange = viewModel::onPrice,
                            label = { Text("实付 *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("分类 *", style = MaterialTheme.typography.labelLarge)
                        Categories.ALL.chunked(5).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { cat ->
                                    FilterChip(
                                        selected = state.category == cat,
                                        onClick = { viewModel.onCategory(cat) },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val picked = Calendar.getInstance().apply { set(y, m, d) }
                                        viewModel.onDate(picked.timeInMillis)
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("日期：${dateFmt.format(Date(state.dateMillis))}") }

                        OutlinedTextField(state.brand, viewModel::onBrand, label = { Text("品牌") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(state.store, viewModel::onStore, label = { Text("店铺") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(state.note, viewModel::onNote, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())

                        if (state.extras.isNotEmpty()) {
                            Text("自定义字段", style = MaterialTheme.typography.labelLarge)
                            state.extras.forEach { (k, v) ->
                                Text("$k：$v", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    state.formError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = !state.saving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) { Text(if (state.saving) "保存中…" else "确认保存") }

                    TextButton(onClick = viewModel::resetToPick, modifier = Modifier.fillMaxWidth()) {
                        Text("重新拍摄")
                    }
                }
            }
        }
    }
}

private fun launchCamera(
    context: android.content.Context,
    onUri: (Uri) -> Unit
) {
    val dir = File(context.cacheDir, "capture").apply { mkdirs() }
    val file = File(dir, "img_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )
    onUri(uri)
}
