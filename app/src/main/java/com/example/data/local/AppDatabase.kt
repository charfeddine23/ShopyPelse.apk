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

@Database(
    entities = [ShoppingItem::class, MarketPrice::class, PriceHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao
    abstract fun marketPriceDao(): MarketPriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

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
                    populateDatabase(database.shoppingDao(), database.marketPriceDao())
                }
            }
        }

        suspend fun populateDatabase(shoppingDao: ShoppingDao, marketPriceDao: MarketPriceDao) {
            shoppingDao.insertItems(DatabaseInitialData.initialShoppingItems)
            marketPriceDao.insertMarketPrices(DatabaseInitialData.initialMarketPrices)
            marketPriceDao.insertPriceHistories(DatabaseInitialData.initialPriceHistories)

            // Insert 5,000 realistic Algerian grocery and supermarket products
            val catalog5000 = CatalogDataGenerator.generate5000Products()
            val marketPrices5000 = CatalogDataGenerator.generateMarketPricesForCatalog(catalog5000)
            marketPriceDao.insertMarketPrices(marketPrices5000)
        }
    }
}
