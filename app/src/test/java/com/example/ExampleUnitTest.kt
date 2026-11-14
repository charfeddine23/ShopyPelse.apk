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
import com.example.ui.components.SingleFingerPullUpDetector
import com.example.ui.components.TwoFingerSwipeDetector
import com.example.ui.components.TwoFingerSwipeDirection
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.util.AppUpdateChecker
import com.example.util.LocalizationUtil
import com.example.util.ProductGroup
import com.example.util.ProductVariant
import com.example.util.SmartSearchUtil
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
        assertEquals("https://github.com/charfeddine23/Shopy", AppUpdateChecker.UPDATE_URL)
        assertTrue(AppUpdateChecker.isNewerVersion("v1.1", "1.0"))
        assertTrue(AppUpdateChecker.isNewerVersion("2.0.0", "1.9.5"))
        assertFalse(AppUpdateChecker.isNewerVersion("v1.0", "1.0"))
        assertFalse(AppUpdateChecker.isNewerVersion("0.9", "1.0"))
    }

    @Test
    fun testTrustedUpdateUrlAllowlist() {
        assertTrue(AppUpdateChecker.isTrustedUpdateUrl("https://github.com/charfeddine23/Shopy"))
        assertTrue(AppUpdateChecker.isTrustedUpdateUrl("https://api.github.com/repos/charfeddine23/Shopy/releases/latest"))
        assertTrue(AppUpdateChecker.isTrustedUpdateUrl("https://objects.githubusercontent.com/some/release.apk"))
        assertTrue(AppUpdateChecker.isTrustedUpdateUrl("https://release-assets.githubusercontent.com/some/release.apk"))
        assertTrue(AppUpdateChecker.isTrustedUpdateUrl("https://github-production-release-asset-2e65be.s3.amazonaws.com/12345/app.apk"))

        assertFalse(AppUpdateChecker.isTrustedUpdateUrl("http://github.com/charfeddine23/Shopy"))
        assertFalse(AppUpdateChecker.isTrustedUpdateUrl("https://evil.com/Shopy.apk"))
        assertFalse(AppUpdateChecker.isTrustedUpdateUrl("https://mybucket.s3.amazonaws.com/evil.apk"))
        assertFalse(AppUpdateChecker.isTrustedUpdateUrl("https://github.com.evil.com/app.apk"))
        assertFalse(AppUpdateChecker.isTrustedUpdateUrl("javascript:alert(1)"))
    }

    @Test
    fun testTwoFingerSwipeDetectorAndPreference() {
        assertEquals(
            TwoFingerSwipeDirection.UP,
            TwoFingerSwipeDetector.evaluateVerticalSwipe(
                dy1 = -140f,
                dy2 = -130f,
                dx1 = 10f,
                dx2 = -5f,
                swipeThresholdPx = 100f,
                minPerFingerDyPx = 40f
            )
        )
        assertEquals(
            TwoFingerSwipeDirection.DOWN,
            TwoFingerSwipeDetector.evaluateVerticalSwipe(
                dy1 = 150f,
                dy2 = 125f,
                dx1 = 12f,
                dx2 = 8f,
                swipeThresholdPx = 100f,
                minPerFingerDyPx = 40f
            )
        )
        // Opposing vertical directions (pinch) must not trigger
        assertEquals(
            null,
            TwoFingerSwipeDetector.evaluateVerticalSwipe(
                dy1 = -130f,
                dy2 = 130f,
                dx1 = 0f,
                dx2 = 0f,
                swipeThresholdPx = 100f,
                minPerFingerDyPx = 40f
            )
        )
        // Primarily horizontal swipe must not trigger
        assertEquals(
            null,
            TwoFingerSwipeDetector.evaluateVerticalSwipe(
                dy1 = -110f,
                dy2 = -110f,
                dx1 = 180f,
                dx2 = 190f,
                swipeThresholdPx = 100f,
                minPerFingerDyPx = 40f
            )
        )
        // Too small movement must not trigger
        assertEquals(
            null,
            TwoFingerSwipeDetector.evaluateVerticalSwipe(
                dy1 = -50f,
                dy2 = -55f,
                dx1 = 0f,
                dx2 = 0f,
                swipeThresholdPx = 100f,
                minPerFingerDyPx = 40f
            )
        )

        val repo = ShoppingRepository(
            shoppingDao = FakeShoppingDao(mutableListOf()),
            marketPriceDao = FakeMarketPriceDao(),
            spendingDao = FakeSpendingDao()
        )
        val vm = ShoppingViewModel(repo)
        assertTrue(vm.isTwoFingerFullScreenSwipeEnabled.value)
        vm.setTwoFingerFullScreenSwipeEnabled(false)
        assertFalse(vm.isTwoFingerFullScreenSwipeEnabled.value)
        vm.setTwoFingerFullScreenSwipeEnabled(true)
        assertTrue(vm.isTwoFingerFullScreenSwipeEnabled.value)
    }

    @Test
    fun testSingleFingerPullUpDetector() {
        val slop = 18f
        // Straight up and slight <=14 deg deviation pass
        assertTrue(SingleFingerPullUpDetector.isDominantUpwardDrag(dx = 12f, dy = -60f, touchSlopPx = slop))
        // Diagonal (~22 deg) must NOT trigger strict pull-up
        assertFalse(SingleFingerPullUpDetector.isDominantUpwardDrag(dx = 25f, dy = -60f, touchSlopPx = slop))
        assertFalse(SingleFingerPullUpDetector.isDominantUpwardDrag(dx = 0f, dy = -10f, touchSlopPx = slop))
        assertFalse(SingleFingerPullUpDetector.isDominantUpwardDrag(dx = 0f, dy = 60f, touchSlopPx = slop))
        assertFalse(SingleFingerPullUpDetector.isDominantUpwardDrag(dx = 90f, dy = -60f, touchSlopPx = slop))

        assertTrue(SingleFingerPullUpDetector.isAbandonedForOtherGesture(dx = 5f, dy = 50f, touchSlopPx = slop))
        assertTrue(SingleFingerPullUpDetector.isAbandonedForOtherGesture(dx = 60f, dy = -30f, touchSlopPx = slop))
        assertTrue(SingleFingerPullUpDetector.isAbandonedForOtherGesture(dx = 25f, dy = -60f, touchSlopPx = slop))
        assertFalse(SingleFingerPullUpDetector.isAbandonedForOtherGesture(dx = 10f, dy = -60f, touchSlopPx = slop))

        assertFalse(SingleFingerPullUpDetector.isTriggerThresholdReached(dx = 0f, dy = -80f, thresholdPx = 120f))
        assertFalse(SingleFingerPullUpDetector.isTriggerThresholdReached(dx = 45f, dy = -130f, thresholdPx = 120f))
        assertTrue(SingleFingerPullUpDetector.isTriggerThresholdReached(dx = 15f, dy = -120f, thresholdPx = 120f))
        assertTrue(SingleFingerPullUpDetector.isTriggerThresholdReached(dx = 0f, dy = -180f, thresholdPx = 120f))

        assertEquals(0f, SingleFingerPullUpDetector.computeVisualPullOffsetPx(dy = 40f, maxVisualPullPx = 90f), 0.01f)
        assertEquals(42f, SingleFingerPullUpDetector.computeVisualPullOffsetPx(dy = -100f, maxVisualPullPx = 90f), 0.01f)
        assertEquals(90f, SingleFingerPullUpDetector.computeVisualPullOffsetPx(dy = -350f, maxVisualPullPx = 90f), 0.01f)
    }

    @Test
    fun testTutorialStateAndCompletion() {
        val repo = ShoppingRepository(
            shoppingDao = FakeShoppingDao(mutableListOf()),
            marketPriceDao = FakeMarketPriceDao(),
            spendingDao = FakeSpendingDao()
        )
        val vm = ShoppingViewModel(repo)
        assertFalse(vm.hasSeenTutorial.value)
        assertTrue(vm.showTutorialOverlay.value)

        vm.completeTutorial()
        assertTrue(vm.hasSeenTutorial.value)
        assertFalse(vm.showTutorialOverlay.value)

        vm.openTutorial()
        assertTrue(vm.showTutorialOverlay.value)
        vm.completeTutorial()
        assertFalse(vm.showTutorialOverlay.value)
    }

    @Test
    fun testSmartSearchRankingAndFuzzyMatching() {
        val candiaPlain = ProductVariant(
            barcode = "6130001",
            itemName = "Candia Milk",
            itemNameAr = "حليب كونديا",
            storeName = "Uno",
            storeNameAr = "أونو",
            priceDzd = 135.0,
            allPrices = emptyList()
        )
        val chocoDrink = ProductVariant(
            barcode = "6130002",
            itemName = "Kids Dessert with Candia Milk Powder",
            itemNameAr = "حلوى بالحليب المجفف كونديا",
            storeName = "Ardis",
            storeNameAr = "أرديس",
            priceDzd = 220.0,
            allPrices = emptyList()
        )
        val riceVariant = ProductVariant(
            barcode = "6130003",
            itemName = "Basmati Rice 1kg",
            itemNameAr = "أرز بسمتي 1 كلغ",
            storeName = "Uno",
            storeNameAr = "أونو",
            priceDzd = 380.0,
            allPrices = emptyList()
        )
        val oilVariant = ProductVariant(
            barcode = "6130004",
            itemName = "Elio Cooking Oil 5L",
            itemNameAr = "زيت المائدة إيليو 5 لتر",
            storeName = "Carrefour",
            storeNameAr = "كارفور",
            priceDzd = 620.0,
            allPrices = emptyList()
        )

        val dessertGroup = ProductGroup(
            id = "dessert",
            nameAr = "حلويات بالحليب",
            nameEn = "Milk Desserts",
            category = "Dairy",
            variants = listOf(chocoDrink),
            minPrice = 220.0,
            maxPrice = 220.0
        )
        val candiaGroup = ProductGroup(
            id = "milk",
            nameAr = "حليب كونديا",
            nameEn = "Candia Milk",
            category = "Dairy",
            variants = listOf(chocoDrink, candiaPlain),
            minPrice = 135.0,
            maxPrice = 220.0
        )
        val riceGroup = ProductGroup(
            id = "rice",
            nameAr = "أرز بسمتي",
            nameEn = "Basmati Rice",
            category = "Pantry",
            variants = listOf(riceVariant),
            minPrice = 380.0,
            maxPrice = 380.0
        )
        val oilGroup = ProductGroup(
            id = "oil",
            nameAr = "زيت المائدة",
            nameEn = "Cooking Oil",
            category = "Pantry",
            variants = listOf(oilVariant),
            minPrice = 620.0,
            maxPrice = 620.0
        )

        val allGroups = listOf(dessertGroup, riceGroup, oilGroup, candiaGroup)

        // Exact match must appear FIRST even if placed last in input list
        val exactResults = SmartSearchUtil.searchAndRankGroups(allGroups, "Candia Milk")
        assertEquals("milk", exactResults.first().id)
        // Best matching variant inside group is also ordered first
        assertEquals("6130001", exactResults.first().variants.first().barcode)

        // Arabic normalization ("ارز" without hamza matches "أرز")
        val arabicNormResults = SmartSearchUtil.searchAndRankGroups(allGroups, "ارز")
        assertEquals(1, arabicNormResults.size)
        assertEquals("rice", arabicNormResults.first().id)

        // Typo tolerance ("canida" transposed letters matches "Candia Milk" first)
        val fuzzyResults = SmartSearchUtil.searchAndRankGroups(allGroups, "canida")
        assertTrue(fuzzyResults.isNotEmpty())
        assertEquals("milk", fuzzyResults.first().id)

        // Multi-token search ("زيت 5 لتر" out of adjacent order)
        val tokenResults = SmartSearchUtil.searchAndRankGroups(allGroups, "زيت 5 لتر")
        assertEquals(1, tokenResults.size)
        assertEquals("oil", tokenResults.first().id)

        // Exact variant ranking directly
        val rankedVariants = SmartSearchUtil.rankVariants(listOf(chocoDrink, candiaPlain), "Candia Milk")
        assertEquals("6130001", rankedVariants.first().barcode)
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
