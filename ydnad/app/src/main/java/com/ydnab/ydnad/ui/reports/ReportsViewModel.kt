package com.ydnab.ydnad.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ydnab.ydnad.data.AppDatabase
import com.ydnab.ydnad.data.CategoryTotal
import com.ydnab.ydnad.data.MonthlyTotal
import com.ydnab.ydnad.data.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(
        AppDatabase.getDatabase(application).transactionDao()
    )

    val monthlyTotals: StateFlow<List<MonthlyTotal>> = repository.monthlyTotals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryTotals: StateFlow<List<CategoryTotal>> = repository.categoryTotals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
