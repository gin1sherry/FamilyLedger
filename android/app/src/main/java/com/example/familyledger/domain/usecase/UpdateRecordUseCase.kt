package com.example.familyledger.domain.usecase

import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.data.model.RecordDraft
import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.domain.util.Money
import com.example.familyledger.domain.util.MonthRange
import javax.inject.Inject

/** 编辑记录并重算预算提醒状态 */
class UpdateRecordUseCase @Inject constructor(
    private val repository: RecordRepository,
    private val checkBudget: CheckBudgetAlertUseCase
) {
    suspend operator fun invoke(updated: Record): Result<Unit> {
        val draft = RecordDraft(
            name = updated.name,
            priceYuan = Money.centsToYuan(updated.price),
            date = updated.date,
            category = updated.category,
            brand = updated.brand,
            spec = updated.spec,
            quantity = updated.quantity,
            unitPriceYuan = updated.unitPrice?.let { Money.centsToYuan(it) },
            discountYuan = updated.discount?.let { Money.centsToYuan(it) },
            store = updated.store,
            paymentMethod = updated.paymentMethod,
            orderNo = updated.orderNo,
            expiryDate = updated.expiryDate,
            note = updated.note,
            source = updated.source,
            sourceBatchId = updated.sourceBatchId
        )
        val err = RecordValidator.validate(draft)
        if (err != null) return Result.failure(err)
        return try {
            repository.updateRecord(updated)
            checkBudget(MonthRange.fromMillis(updated.date).yearMonth, updated.category)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
