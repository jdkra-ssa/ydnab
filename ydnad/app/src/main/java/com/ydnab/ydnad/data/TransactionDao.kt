package com.ydnab.ydnad.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Query("""
        SELECT strftime('%Y-%m', date) as month, SUM(amount) as total
        FROM transactions
        GROUP BY month
        ORDER BY month DESC
    """)
    fun getMonthlyTotals(): Flow<List<MonthlyTotal>>

    @Query("""
        SELECT category, SUM(amount) as total
        FROM transactions
        GROUP BY category
        ORDER BY total DESC
    """)
    fun getCategoryTotals(): Flow<List<CategoryTotal>>
}

data class MonthlyTotal(val month: String, val total: Double)
data class CategoryTotal(val category: String, val total: Double)
