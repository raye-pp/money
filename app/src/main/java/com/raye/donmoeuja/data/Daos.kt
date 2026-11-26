package com.raye.donmoeuja.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query(
        """
        SELECT e.*, c.name AS categoryName FROM entries e
        LEFT JOIN categories c ON e.categoryId = c.id
        WHERE e.occurredAt BETWEEN :from AND :to
        ORDER BY e.createdAt DESC, e.id DESC
        """
    )
    fun observeInRange(from: Long, to: Long): Flow<List<EntryWithCategory>>

    @Query(
        """
        SELECT e.*, c.name AS categoryName FROM entries e
        LEFT JOIN categories c ON e.categoryId = c.id
        ORDER BY e.occurredAt DESC, e.createdAt DESC, e.id DESC
        """
    )
    fun observeAll(): Flow<List<EntryWithCategory>>

    @Query(
        """
        SELECT e.*, c.name AS categoryName FROM entries e
        LEFT JOIN categories c ON e.categoryId = c.id
        WHERE e.id = :id
        """
    )
    fun observeById(id: Long): Flow<EntryWithCategory?>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: Long): Entry?

    @Insert
    suspend fun insert(entry: Entry): Long

    @Update
    suspend fun update(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    @Query(
        """
        SELECT paymentMethod, SUM(amount) AS total FROM entries
        WHERE type = 'EXPENSE' AND occurredAt BETWEEN :from AND :to
        GROUP BY paymentMethod
        ORDER BY total DESC
        """
    )
    fun observePaymentTotals(from: Long, to: Long): Flow<List<PaymentTotal>>

    @Query(
        """
        SELECT c.name AS categoryName, SUM(e.amount) AS total FROM entries e
        LEFT JOIN categories c ON e.categoryId = c.id
        WHERE e.type = 'EXPENSE' AND e.occurredAt BETWEEN :from AND :to
        GROUP BY e.categoryId
        HAVING SUM(e.amount) > 0
        ORDER BY total DESC
        """
    )
    fun observeCategoryTotals(from: Long, to: Long): Flow<List<CategoryTotal>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM entries WHERE type = 'INCOME' AND occurredAt BETWEEN :from AND :to")
    fun observeIncomeSum(from: Long, to: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM entries WHERE type = 'EXPENSE' AND occurredAt BETWEEN :from AND :to")
    fun observeExpenseSum(from: Long, to: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM entries WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int

    @Query("UPDATE entries SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun clearCategory(categoryId: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name AND id != :excludeId")
    suspend fun countByName(name: String, excludeId: Long): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM categories")
    suspend fun maxSortOrder(): Int

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
