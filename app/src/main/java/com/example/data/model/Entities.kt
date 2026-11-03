package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val nameAr: String = name,
    val category: String, // "Produce", "Dairy", "Pantry", "Bakery", "Butchery", "Household", "Beverages"
    val quantity: Int = 1,
    val unit: String = "pcs", // "pcs", "kg", "L", "pack"
    val isChecked: Boolean = false,
    val barcode: String? = null,
    val estimatedPriceDzd: Double = 0.0,
    val isFrequent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "market_prices")
data class MarketPrice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val itemName: String,
    val itemNameAr: String = itemName,
    val storeName: String,
    val storeNameAr: String = storeName,
    val priceDzd: Double,
    val isPromo: Boolean = false,
    val promoNote: String? = null,
    val verifiedCount: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_history")
data class PriceHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val storeName: String,
    val priceDzd: Double,
    val dateLabel: String // e.g. "Aug 2026", "Sep 2026", "Oct 2026"
)

data class StoreBasketSummary(
    val storeName: String,
    val storeNameAr: String,
    val totalDzd: Double,
    val itemCountFound: Int,
    val totalItems: Int,
    val isBestValue: Boolean,
    val savingsComparedToMaxDzd: Double = 0.0
)
