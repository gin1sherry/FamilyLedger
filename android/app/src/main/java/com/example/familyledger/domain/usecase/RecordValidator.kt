package com.example.familyledger.domain.usecase

import com.example.familyledger.data.model.Categories
import com.example.familyledger.data.model.RecordDraft
import com.example.familyledger.domain.util.Money

sealed class RecordError(message: String) : Exception(message) {
    object NameBlank : RecordError("商品名称不能为空")
    object PriceInvalid : RecordError("实付金额无效")
    object CategoryInvalid : RecordError("分类无效")
    object DateInvalid : RecordError("日期无效")
}

/** 纯校验，入库前调用 */
object RecordValidator {
    fun validate(draft: RecordDraft): RecordError? {
        if (draft.name.isBlank()) return RecordError.NameBlank
        if (draft.priceYuan.isNaN() || draft.priceYuan.isInfinite() || draft.priceYuan < 0) {
            return RecordError.PriceInvalid
        }
        if (!Categories.isValid(draft.category)) return RecordError.CategoryInvalid
        if (draft.date <= 0L) return RecordError.DateInvalid
        if (draft.unitPriceYuan != null && draft.unitPriceYuan < 0) return RecordError.PriceInvalid
        if (draft.discountYuan != null && draft.discountYuan < 0) return RecordError.PriceInvalid
        return null
    }

    fun priceCents(draft: RecordDraft): Long = Money.yuanToCents(draft.priceYuan)
}
