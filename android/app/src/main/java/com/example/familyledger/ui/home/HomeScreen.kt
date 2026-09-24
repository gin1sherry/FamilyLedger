package com.example.familyledger.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.domain.util.Money
import com.example.familyledger.ui.components.BudgetAlertBanner
import com.example.familyledger.ui.components.HyperProgress
import com.example.familyledger.ui.theme.HyperColors

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onBudgetClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = viewModel::previousMonth,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上月")
                }
                Text(
                    text = state.monthLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = viewModel::nextMonth,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下月")
                }
                IconButton(
                    onClick = onBudgetClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "预算设置")
                }
                IconButton(
                    onClick = onCaptureClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(HyperColors.OrangeSoft.takeIf { !dark } ?: Color(0x33FF6A00))
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = "拍照记账",
                        tint = if (dark) Color(0xFFFF8A3D) else HyperColors.Orange
                    )
                }
            }

            BudgetAlertBanner(
                alert = state.alert,
                onDismiss = viewModel::dismissAlert
            )

            // Hero 汇总卡：浅橙渐变（澎湃感）
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            if (dark) {
                                Brush.linearGradient(
                                    listOf(Color(0xFF2A1A10), Color(0xFF1C1C1E))
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFF7F0), Color(0xFFFFFFFF))
                                )
                            }
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "本月已花",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = if (dark) Color(0x33FF6A00) else HyperColors.OrangeSoft
                        ) {
                            Text(
                                "预算设置",
                                modifier = Modifier
                                    .clickable(onClick = onBudgetClick)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (dark) Color(0xFFFF8A3D) else HyperColors.OrangeDeep,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = Money.formatYuan(state.totalSpentCents, withSymbol = true),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (state.budgetText.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.budgetText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                    if (state.monthlyBudgetCents > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val progress = (
                            state.totalSpentCents.toDouble() / state.monthlyBudgetCents
                        ).toFloat()
                        HyperProgress(
                            fraction = progress,
                            spent = state.totalSpentCents,
                            budget = state.monthlyBudgetCents
                        )
                    }

                    if (state.categoryProgress.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        state.categoryProgress.forEach { cp ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    cp.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(40.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row {
                                        Text(
                                            "${Money.formatYuan(cp.spentCents, withSymbol = true)} / ${Money.formatYuan(cp.budgetCents, withSymbol = true)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${cp.percent}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    HyperProgress(
                                        fraction = cp.spentCents.toFloat() / cp.budgetCents.coerceAtLeast(1),
                                        spent = cp.spentCents,
                                        budget = cp.budgetCents
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "最近消费",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
            )

            if (state.groups.isEmpty() && !state.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "这个月还没有记录\n点右下角记一笔",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.visibleGroups.forEach { group ->
                        item(key = "day_${group.dayLabel}") {
                            Text(
                                text = group.dayLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                            )
                        }
                        items(group.records, key = { it.id }) { record ->
                            RecordRow(record = record, onClick = { onRecordClick(record.id) })
                        }
                    }
                    if (state.hasMore) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.loadMore() }
                            ) {
                                Text(
                                    text = "加载更早记录",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddClick,
            containerColor = HyperColors.Orange,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            ),
            shape = RoundedCornerShape(99.dp),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = {
                Text("记一笔", fontWeight = FontWeight.SemiBold)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }
}

private fun Color.luminance(): Float =
    (0.299f * red + 0.587f * green + 0.114f * blue)

@Composable
private fun RecordRow(record: Record, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(HyperColors.OrangeSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = record.category.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HyperColors.Orange
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(record.category)
                        record.store?.let { append(" · "); append(it) }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = Money.formatYuan(record.price, withSymbol = true),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
