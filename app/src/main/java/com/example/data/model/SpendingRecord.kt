package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spending_records")
data class SpendingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val monthKey: String, // e.g. "2026-11"
    val dateStr: String,  // e.g. "08/11/2026"
    val totalSpentDzd: Double,
    val itemsCount: Int,
    val storeName: String = "Uno Hypermarket",
    val storeNameAr: String = "هايبرماركت أونو",
    val itemsSummary: String = "",
    val topCategory: String = "Dairy"
)
