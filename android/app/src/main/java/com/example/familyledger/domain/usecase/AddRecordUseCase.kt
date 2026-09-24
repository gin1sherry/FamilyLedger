package com.example.familyledger.domain.usecase

import com.example.familyledger.data.model.RecordDraft
import com.example.familyledger.data.repository.RecordRepository
import javax.inject.Inject

/**
 * 保存一条消费记录（含动态字段）。
 * 小票多条请由调用方循环调用并共享 sourceBatchId。
 */
class AddRecordUseCase @Inject constructor(
    private val repository: RecordRepository
) {
    suspend operator fun invoke(draft: RecordDraft): Result<Long> {
        val err = RecordValidator.validate(draft)
        if (err != null) return Result.failure(err)
        return try {
            val id = repository.saveDraft(draft)
            Result.success(id)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
