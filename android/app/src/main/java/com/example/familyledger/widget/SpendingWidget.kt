package com.example.familyledger.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.familyledger.MainActivity
import com.example.familyledger.data.local.AppDatabase
import com.example.familyledger.domain.budget.BudgetPolicy
import com.example.familyledger.domain.util.Money
import com.example.familyledger.domain.util.MonthRange
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SpendingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val db = androidx.room.Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            AppDatabase.NAME
        ).fallbackToDestructiveMigration().build()

        val range = MonthRange.now()
        val monthRecords = db.recordDao().listInMonth(range.start, range.end)
        val spent = monthRecords.sumOf { it.price }
        val budgets = db.budgetDao().listForMonth(range.yearMonth)
            .filter { it.category != BudgetPolicy.CATEGORY_ALL && it.amount > 0 }

        val categories = budgets.map { b ->
            val catSpent = monthRecords.filter { it.category == b.category }.sumOf { it.price }
            Triple(b.category, catSpent, b.amount)
        }
        db.close()

        val monthLabel = SimpleDateFormat("yyyy年M月", Locale.getDefault()).format(Date())

        provideContent {
            GlanceTheme {
                WidgetUi(monthLabel = monthLabel, spent = spent, categories = categories)
            }
        }
    }

    @Composable
    private fun WidgetUi(
        monthLabel: String,
        spent: Long,
        categories: List<Triple<String, Long, Long>>
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.White))
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Text(
                text = monthLabel,
                style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 12.sp)
            )
            Text(
                text = "本月已花 " + Money.formatYuan(spent, withSymbol = true),
                style = TextStyle(
                    color = ColorProvider(Color(0xFFFF6900)),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            categories.take(3).forEach { (cat, cSpent, budget) ->
                val pct = BudgetPolicy.alertPercent(cSpent, budget)
                Text(
                    text = "$cat $pct%  ${Money.formatYuan(cSpent)} / ${Money.formatYuan(budget)}",
                    style = TextStyle(color = ColorProvider(Color.DarkGray), fontSize = 11.sp)
                )
            }
        }
    }
}

class SpendingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SpendingWidget()
}
