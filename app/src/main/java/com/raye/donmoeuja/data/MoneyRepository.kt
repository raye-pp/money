package com.raye.donmoeuja.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

/** 기간 [start, end] (둘 다 포함)을 밀리초 범위로 */
fun periodRange(start: LocalDate, end: LocalDate): LongRange {
    val zone = ZoneId.systemDefault()
    val from = start.atStartOfDay(zone).toInstant().toEpochMilli()
    val to = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    return from..to
}

sealed interface CategoryResult {
    data object Ok : CategoryResult
    data class Error(val message: String) : CategoryResult
}

class MoneyRepository(
    private val db: AppDatabase,
    val attachments: AttachmentStore,
) {
    private val entryDao = db.entryDao()
    private val categoryDao = db.categoryDao()

    // ---- 내역 ----
    fun entriesInPeriod(start: LocalDate, end: LocalDate): Flow<List<EntryWithCategory>> {
        val r = periodRange(start, end)
        return entryDao.observeInRange(r.first, r.last)
    }

    fun entry(id: Long): Flow<EntryWithCategory?> = entryDao.observeById(id)

    suspend fun getEntry(id: Long): Entry? = entryDao.getById(id)

    suspend fun saveEntry(entry: Entry): Long =
        if (entry.id == 0L) entryDao.insert(entry) else entry.id.also { entryDao.update(entry) }

    suspend fun deleteEntry(entry: Entry) {
        entryDao.delete(entry)
        attachments.delete(entry.attachmentFile)
    }

    // ---- 통계 ----
    fun paymentTotals(start: LocalDate, end: LocalDate): Flow<List<PaymentTotal>> {
        val r = periodRange(start, end)
        return entryDao.observePaymentTotals(r.first, r.last)
    }

    fun categoryTotals(start: LocalDate, end: LocalDate): Flow<List<CategoryTotal>> {
        val r = periodRange(start, end)
        return entryDao.observeCategoryTotals(r.first, r.last)
    }

    fun incomeSum(start: LocalDate, end: LocalDate): Flow<Long> {
        val r = periodRange(start, end)
        return entryDao.observeIncomeSum(r.first, r.last)
    }

    fun expenseSum(start: LocalDate, end: LocalDate): Flow<Long> {
        val r = periodRange(start, end)
        return entryDao.observeExpenseSum(r.first, r.last)
    }

    // ---- 카테고리 ----
    val categories: Flow<List<Category>> = categoryDao.observeAll()

    suspend fun addCategory(rawName: String): CategoryResult {
        val name = rawName.trim()
        validateName(name, excludeId = 0)?.let { return it }
        categoryDao.insert(Category(name = name, sortOrder = categoryDao.maxSortOrder() + 1))
        return CategoryResult.Ok
    }

    suspend fun renameCategory(category: Category, rawName: String): CategoryResult {
        val name = rawName.trim()
        if (name == category.name) return CategoryResult.Ok
        validateName(name, excludeId = category.id)?.let { return it }
        categoryDao.update(category.copy(name = name))
        return CategoryResult.Ok
    }

    suspend fun usageCount(category: Category): Int = entryDao.countByCategory(category.id)

    /** 카테고리를 지우면 그 카테고리를 쓰던 내역은 '미분류'가 됨 */
    suspend fun deleteCategory(category: Category) {
        db.withTransaction {
            entryDao.clearCategory(category.id)
            categoryDao.delete(category)
        }
    }

    private suspend fun validateName(name: String, excludeId: Long): CategoryResult.Error? = when {
        name.isEmpty() -> CategoryResult.Error("카테고리 이름을 입력해 주세요")
        name.length > MAX_CATEGORY_NAME -> CategoryResult.Error("카테고리 이름은 ${MAX_CATEGORY_NAME}자까지예요")
        categoryDao.countByName(name, excludeId) > 0 -> CategoryResult.Error("이미 있는 카테고리예요")
        else -> null
    }

    companion object {
        const val MAX_CATEGORY_NAME = 10
    }
}
