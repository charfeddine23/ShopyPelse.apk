package com.example

import com.example.data.local.DatabaseInitialData
import com.example.data.local.MarketPriceDao
import com.example.data.local.ShoppingDao
import com.example.data.local.SpendingDao
import com.example.data.model.MarketPrice
import com.example.data.model.PriceHistory
import com.example.data.model.ShoppingItem
import com.example.data.model.SpendingRecord
import com.example.data.repository.ShoppingRepository
import com.example.util.AppUpdateChecker
import com.example.util.LocalizationUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormatting() {
        val formattedAr = LocalizationUtil.formatDzd(1250.0, isArabic = true)
        val formattedEn = LocalizationUtil.formatDzd(1250.0, isArabic = false)

        assertTrue(formattedAr.contains("1,250") && formattedAr.contains("د.ج"))
        assertTrue(formattedEn.contains("1,250") && formattedEn.contains("DA"))
    }

    @Test
    fun testCategoryLocalization() {
        assertEquals("خضر وفواكه", LocalizationUtil.getCategoryLabel("Produce", isArabic = true))
        assertEquals("ألبان وأجبان", LocalizationUtil.getCategoryLabel("Dairy", isArabic = true))
        assertEquals("Produce", LocalizationUtil.getCategoryLabel("Produce", isArabic = false))
        assertEquals("مواد تنظيف", LocalizationUtil.getCategoryLabel("Household", isArabic = true))
        assertEquals("لحوم ودواجن", LocalizationUtil.getCategoryLabel("Butchery", isArabic = true))
    }

    @Test
    fun testInitialSpendingCategoriesAreValid() {
        assertTrue(DatabaseInitialData.initialSpendingRecords.isNotEmpty())
        DatabaseInitialData.initialSpendingRecords.forEach { rec ->
            assertTrue(LocalizationUtil.allCategories.contains(rec.topCategory))
        }
    }

    @Test
    fun testQrSerializationAndDeserialization() {
        val originalItem = ShoppingItem(
            name = "Elio Cooking Oil 5L",
            nameAr = "زيت المائدة إيليو 5 لتر",
            category = "Pantry",
            quantity = 2,
            unit = "pcs",
            barcode = "6130002000456",
            estimatedPriceDzd = 620.0
        )
        val payload = "SOUK|${originalItem.name};${originalItem.nameAr};${originalItem.category};${originalItem.quantity};${originalItem.unit};${originalItem.barcode};${originalItem.estimatedPriceDzd}"

        assertTrue(payload.startsWith("SOUK|"))
        val itemsData = payload.removePrefix("SOUK|").split(";")
        assertEquals("Elio Cooking Oil 5L", itemsData[0])
        assertEquals("زيت المائدة إيليو 5 لتر", itemsData[1])
        assertEquals("Pantry", itemsData[2])
        assertEquals(2, itemsData[3].toInt())
    }

    @Test
    fun testDeveloperAndAppInfo() {
        assertEquals("Charfeddine Ka", com.example.ui.viewmodel.ShoppingViewModel.DEVELOPER_NAME)
        assertEquals("v1.0 Build 1", com.example.ui.viewmodel.ShoppingViewModel.APP_VERSION)
    }

    @Test
    fun testUpdateVersionComparison() {
        assertEquals("https://github.com/charfeddine23/ShopyPelse.apk", AppUpdateChecker.UPDATE_URL)
        assertTrue(AppUpdateChecker.isNewerVersion("v1.1", "1.0"))
        assertTrue(AppUpdateChecker.isNewerVersion("2.0.0", "1.9.5"))
        assertFalse(AppUpdateChecker.isNewerVersion("v1.0", "1.0"))
        assertFalse(AppUpdateChecker.isNewerVersion("0.9", "1.0"))
    }

    @Test
    fun testTrustedUpdateUrlAllowlist() {
        assertTrue(AppUpdateChecker.isTrustedUpdateUrl("https://github.com/charfeddine23/ShopyPelse.apk"))
        assertTrue(AppUpdateChecker.isTrustedUpdateUrl("https://api.github.com/repos/charfeddine23/ShopyPelse/releases/latest"))
        assertTrue(AppUpdateChecker.isTrustedUpdateUrl("https://objects.githubusercontent.com/some/release.apk"))

        assertFalse(AppUpdateChecker.isTrustedUpdateUrl("http://github.com/charfeddine23/ShopyPelse.apk"))
        assertFalse(AppUpdateChecker.isTrustedUpdateUrl("https://evil.com/ShopyPelse.apk"))
        assertFalse(AppUpdateChecker.isTrustedUpdateUrl("https://github.com.evil.com/app.apk"))
        assertFalse(AppUpdateChecker.isTrustedUpdateUrl("javascript:alert(1)"))
    }

    @Test
    fun testQrSanitizationAndImportValidation() = runBlocking {
        val inserted = mutableListOf<ShoppingItem>()
        val repo = ShoppingRepository(
            shoppingDao = FakeShoppingDao(inserted),
            marketPriceDao = FakeMarketPriceDao(),
            spendingDao = FakeSpendingDao()
        )

        val malicious = ShoppingItem(
            name = "Milk;Drop~Table|Hack",
            nameAr = "حليب;تجربة|اختبار",
            category = "Dairy",
            quantity = 12000,
            unit = "pcs",
            barcode = "123;456",
            estimatedPriceDzd = -50.0
        )
        val encoded = repo.createQrShareString(listOf(malicious))
        assertFalse(encoded.removePrefix("SOUK|").contains("|"))
        assertFalse(encoded.contains("Milk;Drop"))

        val imported = repo.importFromQrString(encoded)
        assertEquals(1, imported)
        assertEquals(999, inserted.first().quantity)
        assertEquals(0.0, inserted.first().estimatedPriceDzd, 0.001)

        val invalidHeader = repo.importFromQrString("BAD|Milk;حليب;Dairy;2")
        assertEquals(0, invalidHeader)

        val emptyNameImport = repo.importFromQrString("SOUK|   ;حليب;Dairy;2")
        assertEquals(0, emptyNameImport)
    }

    private class FakeShoppingDao(private val store: MutableList<ShoppingItem>) : ShoppingDao {
        override fun getAllShoppingItems(): Flow<List<ShoppingItem>> = flowOf(store)
        override fun getFrequentItems(): Flow<List<ShoppingItem>> = flowOf(emptyList())
        override suspend fun getItemByBarcode(barcode: String): ShoppingItem? = null
        override suspend fun getItemById(id: Long): ShoppingItem? = null
        override suspend fun insertItem(item: ShoppingItem): Long {
            store.add(item)
            return store.size.toLong()
        }
        override suspend fun insertItems(items: List<ShoppingItem>) {
            store.addAll(items)
        }
        override suspend fun updateItem(item: ShoppingItem) {}
        override suspend fun deleteItem(item: ShoppingItem) {}
        override suspend fun updateCheckedStatus(id: Long, isChecked: Boolean) {}
        override suspend fun updateQuantity(id: Long, quantity: Int) {}
        override suspend fun deleteCheckedItems() {}
        override suspend fun clearAll() {
            store.clear()
        }
    }

    private class FakeMarketPriceDao : MarketPriceDao {
        override fun getPricesForBarcode(barcode: String): Flow<List<MarketPrice>> = flowOf(emptyList())
        override fun searchMarketPrices(query: String): Flow<List<MarketPrice>> = flowOf(emptyList())
        override fun getAllMarketPrices(): Flow<List<MarketPrice>> = flowOf(emptyList())
        override suspend fun getMarketPriceCount(): Int = 0
        override suspend fun getBestPriceForBarcode(barcode: String): MarketPrice? = null
        override suspend fun getPriceForBarcodeAndStore(barcode: String, storeName: String): MarketPrice? = null
        override suspend fun insertMarketPrice(price: MarketPrice): Long = 1L
        override suspend fun insertMarketPrices(prices: List<MarketPrice>) {}
        override suspend fun updatePriceReport(barcode: String, storeName: String, newPrice: Double, updatedAt: Long) {}
        override suspend fun updatePriceForBarcode(barcode: String, newPrice: Double, updatedAt: Long) {}
        override fun getPriceHistory(barcode: String): Flow<List<PriceHistory>> = flowOf(emptyList())
        override suspend fun insertPriceHistories(histories: List<PriceHistory>) {}
    }

    private class FakeSpendingDao : SpendingDao {
        override fun getAllSpendingRecords(): Flow<List<SpendingRecord>> = flowOf(emptyList())
        override suspend fun getSpendingRecordCount(): Int = 0
        override fun getSpendingForMonth(monthKey: String): Flow<List<SpendingRecord>> = flowOf(emptyList())
        override suspend fun insertSpendingRecord(record: SpendingRecord): Long = 1L
        override suspend fun insertSpendingRecords(records: List<SpendingRecord>) {}
        override suspend fun deleteSpendingRecord(id: Long) {}
        override suspend fun clearAll() {}
    }
}
