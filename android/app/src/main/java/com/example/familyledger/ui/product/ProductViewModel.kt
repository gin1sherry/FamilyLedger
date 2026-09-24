package com.example.familyledger.ui.product

import android.graphics.Canvas
import android.graphics.Paint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.domain.util.Money
import com.example.familyledger.domain.util.ProductMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductUiState(
    val title: String = "",
    val subtitle: String = "",
    val records: List<Record> = emptyList(),
    val prices: List<Long> = emptyList(),
    val dates: List<Long> = emptyList()
) {
    val showChart: Boolean get() = records.size >= 2
}

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val records: RecordRepository
) : ViewModel() {

    private val productKey = MutableStateFlow<String?>(null)
    private val seedName = MutableStateFlow("")

    val uiState: StateFlow<ProductUiState> = productKey
        .map { key ->
            if (key == null) return@map ProductUiState()
            val all = records.listAll()
                .filter { ProductMatcher.keyFor(it.name) == key }
                .sortedBy { it.date }
            val latest = all.lastOrNull()
            ProductUiState(
                title = latest?.name ?: seedName.value,
                subtitle = if (all.isEmpty()) "无记录"
                else "共 ${all.size} 次购买 · 最近 ${Money.formatYuan(latest!!.price, withSymbol = true)}",
                records = all,
                prices = all.map { it.price },
                dates = all.map { it.date }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductUiState())

    fun loadFromRecord(recordId: Long) {
        viewModelScope.launch {
            val r = records.getById(recordId) ?: return@launch
            seedName.value = r.name
            productKey.value = ProductMatcher.keyFor(r.name)
        }
    }
}

/** 简易折线绘制 */
object PriceChartPainter {
    fun draw(
        canvas: Canvas,
        prices: List<Long>,
        lineColor: Int,
        axisColor: Int,
        pointColor: Int
    ) {
        if (prices.size < 2) return
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val padL = 48f
        val padR = 16f
        val padT = 16f
        val padB = 28f
        val cw = w - padL - padR
        val ch = h - padT - padB
        var min = prices.min()
        var max = prices.max()
        if (min == max) {
            min = (min * 9) / 10
            max = max + max / 10 + 1
        }
        val range = (max - min).toFloat()

        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = axisColor
            strokeWidth = 1f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineColor
            strokeWidth = 3f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pointColor
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = axisColor
            textSize = 22f
        }

        for (i in 0..4) {
            val y = padT + ch * i / 4f
            canvas.drawLine(padL, y, padL + cw, y, axisPaint)
            val v = max - (max - min) * i / 4.0
            canvas.drawText(Money.formatYuan(v.toLong()), 4f, y + 8f, textPaint)
        }

        val step = cw / (prices.size - 1)
        val pts = FloatArray(prices.size * 2)
        prices.forEachIndexed { i, p ->
            val x = padL + step * i
            val y = padT + ch - ((p - min).toFloat() / range) * ch
            pts[i * 2] = x
            pts[i * 2 + 1] = y
        }
        canvas.drawLines(pts, linePaint) // may need path - use path
        // draw proper polyline
        val path = android.graphics.Path()
        path.moveTo(pts[0], pts[1])
        for (i in 1 until prices.size) {
            path.lineTo(pts[i * 2], pts[i * 2 + 1])
        }
        canvas.drawPath(path, linePaint)
        for (i in 0 until prices.size) {
            canvas.drawCircle(pts[i * 2], pts[i * 2 + 1], 8f, pointPaint)
        }
    }
}
