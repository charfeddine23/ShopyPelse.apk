package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.MarketPrice
import com.example.data.model.PriceHistory
import com.example.data.model.ShoppingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import com.example.data.model.SpendingRecord

@Database(
    entities = [ShoppingItem::class, MarketPrice::class, PriceHistory::class, SpendingRecord::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao
    abstract fun marketPriceDao(): MarketPriceDao
    abstract fun spendingDao(): SpendingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val seedMutex = Mutex()

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shopypulse_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.shoppingDao(), database.marketPriceDao(), database.spendingDao())
                }
            }
        }

        suspend fun populateDatabase(shoppingDao: ShoppingDao, marketPriceDao: MarketPriceDao, spendingDao: SpendingDao) {
            seedMutex.withLock {
                if (marketPriceDao.getMarketPriceCount() > 0) return@withLock
                shoppingDao.insertItems(DatabaseInitialData.initialShoppingItems)
                marketPriceDao.insertMarketPrices(DatabaseInitialData.initialMarketPrices)
                marketPriceDao.insertPriceHistories(DatabaseInitialData.initialPriceHistories)
                if (spendingDao.getSpendingRecordCount() == 0) {
                    spendingDao.insertSpendingRecords(DatabaseInitialData.initialSpendingRecords)
                }

                // Insert 5,000 realistic Algerian grocery and supermarket products
                val catalog5000 = CatalogDataGenerator.generate5000Products()
                val marketPrices5000 = CatalogDataGenerator.generateMarketPricesForCatalog(catalog5000)
                marketPriceDao.insertMarketPrices(marketPrices5000)
            }
        }
    }
}
