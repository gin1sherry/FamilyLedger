package com.example.familyledger.domain.usecase

import com.example.familyledger.data.repository.RecordRepository
import com.example.familyledger.domain.util.MonthRange
import javax.inject.Inject

/**
 * 删除记录（动态字段级联）。随后重算预算 notified（低于 80% 则允许再次提醒）。
 */
class DeleteRecordUseCase @Inject constructor(
    private val repository: RecordRepository,
    private val checkBudget: CheckBudgetAlertUseCase
) {
    suspend operator fun invoke(recordId: Long): Result<Unit> {
        return try {
            val record = repository.getById(recordId)
                ?: return Result.failure(IllegalArgumentException("记录不存在"))
            val yearMonth = MonthRange.fromMillis(record.date).yearMonth
            val category = record.category
            repository.deleteRecord(recordId)
            // 读时聚合已下降；重置/复查 notified
            checkBudget(yearMonth, category)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
