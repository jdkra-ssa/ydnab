package com.ydnab.ydnad.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ydnab.ydnad.data.AppDatabase
import com.ydnab.ydnad.data.Transaction
import com.ydnab.ydnad.data.TransactionApiService
import com.ydnab.ydnad.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository = TransactionRepository(
        AppDatabase.getDatabase(application).transactionDao()
    )

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    fun syncFromApi(
        startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
        endDate: LocalDate = LocalDate.now().plusMonths(1)
    ) = viewModelScope.launch {
        _isSyncing.value = true
        _syncError.value = null
        try {
            val fetched = TransactionApiService.fetchTransactions(startDate, endDate, limit = 10000)
            repository.deleteAll()
            repository.insertAll(fetched)
        } catch (e: Exception) {
            _syncError.value = e.message ?: "Sync failed"
        } finally {
            _isSyncing.value = false
        }
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    suspend fun getById(id: Long): Transaction? = repository.getById(id)

    /** Calls the remote API to create the entry, then saves it locally. Throws on failure. */
    suspend fun createEntry(transaction: Transaction) {
        val isIncome = transaction.category.equals("Income", ignoreCase = true)
        val inflow = if (isIncome) transaction.amount else 0.0
        val outflow = if (isIncome) 0.0 else transaction.amount
        TransactionApiService.createEntry(
            date = transaction.date,
            category = transaction.category,
            subCategory = transaction.subCategory,
            memo = transaction.memo,
            inflow = inflow,
            outflow = outflow
        )
        repository.insert(transaction)
    }
}
