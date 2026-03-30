package com.ydnab.ydnad.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {

    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()
    val monthlyTotals: Flow<List<MonthlyTotal>> = dao.getMonthlyTotals()
    val categoryTotals: Flow<List<CategoryTotal>> = dao.getCategoryTotals()

    suspend fun insert(transaction: Transaction): Long = dao.insert(transaction)

    suspend fun update(transaction: Transaction) = dao.update(transaction)

    suspend fun delete(transaction: Transaction) = dao.delete(transaction)

    suspend fun getById(id: Long): Transaction? = dao.getTransactionById(id)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun insertAll(transactions: List<Transaction>) = dao.insertAll(transactions)
}
