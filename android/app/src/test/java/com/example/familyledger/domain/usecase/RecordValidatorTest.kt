package com.example.familyledger.domain.usecase

import com.example.familyledger.data.model.RecordDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class RecordValidatorTest {
    private fun draft(
        name: String = "伊利纯牛奶",
        price: Double = 12.5,
        category: String = "日用",
        date: Long = 1_700_000_000_000L
    ) = RecordDraft(
        name = name,
        priceYuan = price,
        date = date,
        category = category,
        source = "manual"
    )

    @Test
    fun ok() {
        assertNull(RecordValidator.validate(draft()))
        assertEquals(1250L, RecordValidator.priceCents(draft()))
    }

    @Test
    fun blank_name() {
        assertEquals(RecordError.NameBlank, RecordValidator.validate(draft(name = "  ")))
    }

    @Test
    fun negative_price() {
        assertEquals(RecordError.PriceInvalid, RecordValidator.validate(draft(price = -1.0)))
    }

    @Test
    fun bad_category() {
        assertEquals(RecordError.CategoryInvalid, RecordValidator.validate(draft(category = "未知")))
    }

    @Test
    fun bad_date() {
        assertNotNull(RecordValidator.validate(draft(date = 0L)))
    }

    @Test
    fun receipt_batch_fields_ok() {
        val d = draft().copy(
            source = "receipt",
            sourceBatchId = "b1",
            extras = listOf("产地" to "四川")
        )
        assertNull(RecordValidator.validate(d))
    }
}
