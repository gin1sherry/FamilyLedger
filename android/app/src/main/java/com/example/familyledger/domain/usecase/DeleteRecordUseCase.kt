package com.example.familyledger.domain.usecase

import com.example.familyledger.data.repository.RecordRepository
import javax.inject.Inject

/** 删除记录；动态字段由外键级联删除。预算为读时聚合，无需在此改 budgets。 */
class DeleteRecordUseCase @Inject constructor(
    private val repository: RecordRepository
) {
    suspend operator fun invoke(recordId: Long): Result<Unit> {
        return try {
            repository.deleteRecord(recordId)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
