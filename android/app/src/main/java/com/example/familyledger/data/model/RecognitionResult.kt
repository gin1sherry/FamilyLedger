package com.example.familyledger.data.model

/** AI 识别返回 — 对应 PRD 4.2.6 / 4.2.7 三层字段 */
data class RecognitionResult(
    val mode: String = "product", // product | receipt
    val name: String? = null,
    val price: Double? = null,
    val date: Long? = null,
    val category: String? = null,
    val brand: String? = null,
    val spec: String? = null,
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val discount: Double? = null,
    val store: String? = null,
    val paymentMethod: String? = null,
    val orderNo: String? = null,
    val expiryDate: Long? = null,
    val note: String? = null,
    val confidence: Double? = null,
    /** 未覆盖字段 → 第三层 */
    val extras: Map<String, String> = emptyMap(),
    /** 小票多条目 */
    val items: List<ReceiptItem> = emptyList(),
    val total: Double? = null,
    val source: String = "camera"
) {
    val isLowConfidence: Boolean get() = confidence != null && confidence < 0.6
}

data class ReceiptItem(
    val name: String? = null,
    val price: Double? = null,
    val category: String? = null,
    val brand: String? = null,
    val quantity: Double? = null
)

/** 保存到 Room 的中间模型 */
data class RecordDraft(
    val name: String,
    val priceYuan: Double,
    val date: Long,
    val category: String,
    val brand: String? = null,
    val spec: String? = null,
    val quantity: Double? = null,
    val unitPriceYuan: Double? = null,
    val discountYuan: Double? = null,
    val store: String? = null,
    val paymentMethod: String? = null,
    val orderNo: String? = null,
    val expiryDate: Long? = null,
    val note: String? = null,
    val source: String = "manual",
    val sourceBatchId: String? = null,
    val extras: List<Pair<String, String>> = emptyList()
)
