package com.example.familyledger.data.repository

import com.example.familyledger.data.local.dao.BudgetDao
import com.example.familyledger.data.local.dao.DynamicFieldDao
import com.example.familyledger.data.local.dao.PendingImageDao
import com.example.familyledger.data.local.dao.RecordDao
import com.example.familyledger.data.local.entity.Budget
import com.example.familyledger.data.local.entity.DynamicField
import com.example.familyledger.data.local.entity.PendingImage
import com.example.familyledger.data.local.entity.Record
import com.example.familyledger.data.model.RecordDraft
import com.example.familyledger.domain.util.Money
import com.example.familyledger.domain.util.MonthRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepository @Inject constructor(
    private val recordDao: RecordDao,
    private val fieldDao: DynamicFieldDao,
    private val budgetDao: BudgetDao,
    private val pendingDao: PendingImageDao
) {
    fun observeMonth(range: MonthRange): Flow<List<Record>> =
        recordDao.observeInMonth(range.start, range.end)

    fun observeMonthPaged(range: MonthRange, page: Int, pageSize: Int = 20): Flow<List<Record>> =
        recordDao.observeInMonthPaged(range.start, range.end, pageSize, page * pageSize)

    fun observeTotalSpent(range: MonthRange): Flow<Long> =
        recordDao.observeTotalSpent(range.start, range.end)

    fun observeCategorySpent(range: MonthRange, category: String): Flow<Long> =
        recordDao.observeCategorySpent(range.start, range.end, category)

    fun observeRecord(id: Long): Flow<Record?> = recordDao.observeById(id)

    fun observeFields(recordId: Long): Flow<List<DynamicField>> =
        fieldDao.observeForRecord(recordId)

    fun search(q: String): Flow<List<Record>> = recordDao.search(q)

    fun searchFieldRecordIds(q: String): Flow<List<Long>> = fieldDao.searchRecordIds(q)

    fun searchCombined(q: String): Flow<List<Record>> =
        combine(recordDao.search(q), fieldDao.searchRecordIds(q)) { base, ids ->
            base to ids
        }.transform { (base, ids) ->
            val baseIds = base.map { it.id }.toSet()
            val extraIds = ids.filter { it !in baseIds }
            val extras = if (extraIds.isEmpty()) emptyList() else recordDao.getByIds(extraIds)
            emit((base + extras).distinctBy { it.id }.sortedByDescending { it.date })
        }

    suspend fun getById(id: Long): Record? = recordDao.getById(id)

    suspend fun listAll(): List<Record> = recordDao.listAll()

    suspend fun listInMonth(range: MonthRange): List<Record> =
        recordDao.listInMonth(range.start, range.end)

    suspend fun saveDraft(draft: RecordDraft): Long {
        val now = System.currentTimeMillis()
        val record = Record(
            name = draft.name.trim(),
            price = Money.yuanToCents(draft.priceYuan),
            date = draft.date,
            category = draft.category,
            brand = draft.brand,
            spec = draft.spec,
            quantity = draft.quantity,
            unitPrice = draft.unitPriceYuan?.let { Money.yuanToCents(it) },
            discount = draft.discountYuan?.let { Money.yuanToCents(it) },
            store = draft.store,
            paymentMethod = draft.paymentMethod,
            orderNo = draft.orderNo,
            expiryDate = draft.expiryDate,
            note = draft.note,
            source = draft.source,
            sourceBatchId = draft.sourceBatchId,
            createdAt = now,
            updatedAt = now
        )
        val id = recordDao.insert(record)
        draft.extras.forEach { (k, v) ->
            if (k.isNotBlank()) {
                fieldDao.insert(
                    DynamicField(recordId = id, key = k, value = v, type = guessType(v))
                )
            }
        }
        return id
    }

    suspend fun updateRecord(record: Record) {
        recordDao.update(record.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteRecord(id: Long) {
        recordDao.deleteById(id)
    }

    suspend fun replaceExtras(recordId: Long, fields: List<Triple<String, String, String>>) {
        fieldDao.deleteForRecord(recordId)
        fields.filter { it.first.isNotBlank() }.forEach { (k, v, t) ->
            fieldDao.insert(DynamicField(recordId = recordId, key = k, value = v, type = t))
        }
    }

    suspend fun addField(recordId: Long, key: String, value: String, type: String = "text"): Long =
        fieldDao.insert(DynamicField(recordId = recordId, key = key, value = value, type = type))

    suspend fun updateField(field: DynamicField) = fieldDao.update(field)

    suspend fun deleteField(id: Long) = fieldDao.deleteById(id)

    // ---- Budget ----
    fun observeBudgets(yearMonth: String): Flow<List<Budget>> =
        budgetDao.observeForMonth(yearMonth)

    suspend fun getBudget(yearMonth: String, category: String): Budget? =
        budgetDao.get(yearMonth, category)

    suspend fun upsertBudget(budget: Budget): Long {
        return if (budget.id == 0L) {
            budgetDao.upsert(budget)
        } else {
            budgetDao.update(budget)
            budget.id
        }
    }

    suspend fun clearBudget(yearMonth: String, category: String) =
        budgetDao.deleteIfZero(yearMonth, category)

    suspend fun categorySpent(yearMonth: String, category: String): Long {
        val range = MonthRange.fromYearMonth(yearMonth)
        return recordDao.listInMonth(range.start, range.end)
            .filter { it.category == category }
            .sumOf { it.price }
    }

    suspend fun totalSpent(yearMonth: String): Long {
        val range = MonthRange.fromYearMonth(yearMonth)
        return recordDao.listInMonth(range.start, range.end).sumOf { it.price }
    }

    suspend fun markNotified(yearMonth: String, category: String) =
        budgetDao.markNotified(yearMonth, category)

    suspend fun resetNotified(yearMonth: String, category: String) =
        budgetDao.resetNotified(yearMonth, category)

    // ---- Pending images ----
    suspend fun insertPending(pendingImage: PendingImage): Long = pendingDao.insert(pendingImage)

    suspend fun listPending(): List<PendingImage> = pendingDao.listPending()

    fun observePendingCount(): Flow<Int> = pendingDao.observePendingCount()

    suspend fun completePending(id: Long, success: Boolean) =
        pendingDao.updateStatus(id, if (success) "done" else "failed")

    private fun guessType(v: String): String = when {
        v.toLongOrNull() != null || v.toDoubleOrNull() != null -> "number"
        v.equals("true", true) || v.equals("false", true) -> "boolean"
        v.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*")) -> "date"
        else -> "text"
    }
}
