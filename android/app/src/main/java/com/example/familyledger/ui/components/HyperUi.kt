package com.example.familyledger.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.familyledger.domain.budget.BudgetPolicy
import com.example.familyledger.domain.usecase.BudgetAlert
import com.example.familyledger.domain.util.Money
import com.example.familyledger.ui.theme.HyperColors

@Composable
fun HyperCard(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = { content() }
    )
}

@Composable
fun HyperProgress(
    fraction: Float,
    spent: Long,
    budget: Long,
    modifier: Modifier = Modifier
) {
    val level = BudgetPolicy.level(spent, budget)
    val color = when (level) {
        BudgetPolicy.Level.OK -> HyperColors.Ok
        BudgetPolicy.Level.WARN -> HyperColors.Warn
        BudgetPolicy.Level.CRITICAL -> HyperColors.Danger
    }
    val f = fraction.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(99.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(f)
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(color)
        )
    }
}

@Composable
fun BudgetAlertBanner(
    alert: BudgetAlert?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (alert == null) return
    val title = if (alert.category == null) {
        "月总预算已达 ${alert.percent}%"
    } else {
        "${alert.category}预算已达 ${alert.percent}%"
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "（${Money.formatYuan(alert.spentCents, withSymbol = true)} / ${Money.formatYuan(alert.budgetCents, withSymbol = true)}）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "关闭")
            }
        }
    }
}
