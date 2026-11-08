package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MarketPrice
import com.example.data.model.PriceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketPriceDao {
    @Query("SELECT * FROM market_prices WHERE barcode = :barcode ORDER BY priceDzd ASC")
    fun getPricesForBarcode(barcode: String): Flow<List<MarketPrice>>

    @Query("SELECT * FROM market_prices WHERE itemName LIKE '%' || :query || '%' OR itemNameAr LIKE '%' || :query || '%' ORDER BY priceDzd ASC")
    fun searchMarketPrices(query: String): Flow<List<MarketPrice>>

    @Query("SELECT * FROM market_prices ORDER BY priceDzd ASC")
    fun getAllMarketPrices(): Flow<List<MarketPrice>>

    @Query("SELECT * FROM market_prices WHERE barcode = :barcode ORDER BY priceDzd ASC LIMIT 1")
    suspend fun getBestPriceForBarcode(barcode: String): MarketPrice?

    @Query("SELECT * FROM market_prices WHERE barcode = :barcode AND storeName = :storeName LIMIT 1")
    suspend fun getPriceForBarcodeAndStore(barcode: String, storeName: String): MarketPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrice(price: MarketPrice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrices(prices: List<MarketPrice>)

    @Query("UPDATE market_prices SET priceDzd = :newPrice, verifiedCount = verifiedCount + 1, updatedAt = :updatedAt WHERE barcode = :barcode AND storeName = :storeName")
    suspend fun updatePriceReport(barcode: String, storeName: String, newPrice: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE market_prices SET priceDzd = :newPrice, updatedAt = :updatedAt WHERE barcode = :barcode")
    suspend fun updatePriceForBarcode(barcode: String, newPrice: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM price_history WHERE barcode = :barcode ORDER BY id ASC")
    fun getPriceHistory(barcode: String): Flow<List<PriceHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistories(histories: List<PriceHistory>)
}
