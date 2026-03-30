package com.ydnab.ydnad.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val category: String,
    val subCategory: String,
    val memo: String,
    val amount: Double
)
