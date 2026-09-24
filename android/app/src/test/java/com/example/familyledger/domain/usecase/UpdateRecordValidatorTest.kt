package com.example.familyledger.domain.usecase

import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.data.local.entity.Record as RecordEntity
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 删除/编辑路径的校验与回算策略相关纯逻辑占位说明见 BudgetPolicyTest。
 * 此处覆盖 Record 在编辑前的字段合法性（与 UpdateRecordUseCase 同源校验）。
 */
class UpdateRecordValidatorTest {
    private fun sample() = RecordEntity(
        id = 1,
        name = "测试商品",
        price = 1250L,
        date = 1_700_000_000_000L,
        category = "日用",
        source = "manual"
    )

    @Test
    fun valid_record_passes() {
        val draft = com.example.familyledger.data.model.RecordDraft(
            name = sample().name,
            priceYuan = 12.5,
            date = sample().date,
            category = sample().category,
            source = sample().source
        )
        assertNull(RecordValidator.validate(draft))
    }

    @Test
    fun blank_name_fails() {
        val draft = com.example.familyledger.data.model.RecordDraft(
            name = " ",
            priceYuan = 1.0,
            date = 1L,
            category = "其他"
        )
        assertTrue(RecordValidator.validate(draft) is RecordError.NameBlank)
    }
}
