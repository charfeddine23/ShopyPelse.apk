package com.example.data.repository

import com.example.data.local.MarketPriceDao
import com.example.data.local.ShoppingDao
import com.example.data.model.MarketPrice
import com.example.data.model.PriceHistory
import com.example.data.model.ShoppingItem
import com.example.data.model.StoreBasketSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ShoppingRepository(
    private val shoppingDao: ShoppingDao,
    private val marketPriceDao: MarketPriceDao
) {
    val allShoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAllShoppingItems()
    val allMarketPrices: Flow<List<MarketPrice>> = marketPriceDao.getAllMarketPrices()

    suspend fun insertItem(item: ShoppingItem): Long {
        val category = if (item.category.isBlank() || item.category == "Other") {
            autoCategorize(item.name)
        } else item.category

        // If barcode exists, try to enrich estimated price from best store
        val estimatedPrice = if (item.barcode != null && item.estimatedPriceDzd <= 0.0) {
            val best = marketPriceDao.getBestPriceForBarcode(item.barcode)
            best?.priceDzd ?: 0.0
        } else {
            item.estimatedPriceDzd
        }

        return shoppingDao.insertItem(
            item.copy(category = category, estimatedPriceDzd = estimatedPrice)
        )
    }

    suspend fun updateItem(item: ShoppingItem) = shoppingDao.updateItem(item)

    suspend fun deleteItem(item: ShoppingItem) = shoppingDao.deleteItem(item)

    suspend fun toggleChecked(id: Long, isChecked: Boolean) = shoppingDao.updateCheckedStatus(id, isChecked)

    suspend fun updateQuantity(id: Long, quantity: Int) = shoppingDao.updateQuantity(id, quantity)

    suspend fun clearCheckedItems() = shoppingDao.deleteCheckedItems()

    suspend fun getItemByBarcode(barcode: String): ShoppingItem? = shoppingDao.getItemByBarcode(barcode)

    fun getPricesForBarcode(barcode: String): Flow<List<MarketPrice>> =
        marketPriceDao.getPricesForBarcode(barcode)

    fun searchPrices(query: String): Flow<List<MarketPrice>> =
        marketPriceDao.searchMarketPrices(query)

    suspend fun getPriceHistory(barcode: String): Flow<List<PriceHistory>> =
        marketPriceDao.getPriceHistory(barcode)

    suspend fun ensureCatalogPopulated() {
        val currentPrices = marketPriceDao.getAllMarketPrices().first()
        if (currentPrices.size < 2500) {
            val catalog3000 = com.example.data.local.CatalogDataGenerator.generate3000Products()
            val marketPrices3000 = com.example.data.local.CatalogDataGenerator.generateMarketPricesForCatalog(catalog3000)
            marketPriceDao.insertMarketPrices(marketPrices3000)
        }
    }

    suspend fun reportCrowdsourcedPrice(barcode: String, storeName: String, storeNameAr: String, itemName: String, newPrice: Double) {
        val existing = marketPriceDao.getPriceForBarcodeAndStore(barcode, storeName)
        if (existing != null) {
            marketPriceDao.updatePriceReport(barcode, storeName, newPrice)
        } else {
            marketPriceDao.insertMarketPrice(
                MarketPrice(
                    barcode = barcode,
                    itemName = itemName,
                    itemNameAr = itemName,
                    storeName = storeName,
                    storeNameAr = storeNameAr,
                    priceDzd = newPrice,
                    verifiedCount = 1
                )
            )
        }
    }

    /**
     * Total Basket Price Comparison:
     * Evaluates all shopping items across known supermarkets.
     * Highlights the cheapest store with the Emerald Green "Best Value" badge.
     */
    suspend fun computeBasketComparison(items: List<ShoppingItem>): List<StoreBasketSummary> {
        if (items.isEmpty()) return emptyList()

        val allPrices = marketPriceDao.getAllMarketPrices().first()
        val storeNames = allPrices.map { it.storeName to it.storeNameAr }.distinct()

        val summaries = storeNames.map { (storeName, storeNameAr) ->
            var storeTotal = 0.0
            var foundCount = 0

            for (item in items) {
                // Find store-specific price for this item's barcode or item name
                val priceObj = allPrices.firstOrNull { p ->
                    p.storeName == storeName && (
                        (item.barcode != null && p.barcode == item.barcode) ||
                        p.itemName.equals(item.name, ignoreCase = true) ||
                        p.itemNameAr.equals(item.nameAr, ignoreCase = true)
                    )
                }

                if (priceObj != null) {
                    storeTotal += (priceObj.priceDzd * item.quantity)
                    foundCount++
                } else {
                    // Fallback to estimated price if not explicitly in store catalog
                    storeTotal += (item.estimatedPriceDzd * item.quantity)
                }
            }

            StoreBasketSummary(
                storeName = storeName,
                storeNameAr = storeNameAr,
                totalDzd = storeTotal,
                itemCountFound = foundCount,
                totalItems = items.size,
                isBestValue = false
            )
        }

        if (summaries.isEmpty()) return emptyList()

        val minTotal = summaries.minOf { it.totalDzd }
        val maxTotal = summaries.maxOf { it.totalDzd }

        return summaries.map { summary ->
            val isCheapest = summary.totalDzd == minTotal && minTotal > 0.0
            val savings = if (maxTotal > summary.totalDzd) maxTotal - summary.totalDzd else 0.0
            summary.copy(isBestValue = isCheapest, savingsComparedToMaxDzd = savings)
        }.sortedBy { it.totalDzd }
    }

    /**
     * Auto-categorizes an item based on English, French, and Arabic grocery keywords.
     */
    fun autoCategorize(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("milk") || lower.contains("lait") || lower.contains("حليب") ||
            lower.contains("cheese") || lower.contains("fromage") || lower.contains("جبن") ||
            lower.contains("yaourt") || lower.contains("yogurt") || lower.contains("لبن") ||
            lower.contains("egg") || lower.contains("oeuf") || lower.contains("بيض") -> "Dairy"

            lower.contains("tomato") || lower.contains("tomate") || lower.contains("طماطم") ||
            lower.contains("potato") || lower.contains("pomme de terre") || lower.contains("بطاطا") ||
            lower.contains("onion") || lower.contains("oignon") || lower.contains("بصل") ||
            lower.contains("apple") || lower.contains("pomme") || lower.contains("تفاح") ||
            lower.contains("banana") || lower.contains("banane") || lower.contains("موز") ||
            lower.contains("orange") || lower.contains("برتقال") || lower.contains("lemon") || lower.contains("citron") || lower.contains("ليمون") -> "Produce"

            lower.contains("bread") || lower.contains("pain") || lower.contains("خبز") ||
            lower.contains("baguette") || lower.contains("باقيت") || lower.contains("croissant") || lower.contains("كرواسون") ||
            lower.contains("brioche") || lower.contains("بريوش") -> "Bakery"

            lower.contains("chicken") || lower.contains("poulet") || lower.contains("دجاج") ||
            lower.contains("meat") || lower.contains("viande") || lower.contains("لحم") ||
            lower.contains("steak") || lower.contains("escalope") || lower.contains("كفتة") ||
            lower.contains("fish") || lower.contains("poisson") || lower.contains("سمك") -> "Butchery"

            lower.contains("amande") || lower.contains("لوز") || lower.contains("pistache") || lower.contains("فستق") ||
            lower.contains("noisette") || lower.contains("بندق") || lower.contains("cacahuete") || lower.contains("كاوكاو") ||
            lower.contains("noix") || lower.contains("جوز") || lower.contains("nut") || lower.contains("مكسرات") ||
            lower.contains("bonbon") || lower.contains("حلوى") || lower.contains("chocolat") || lower.contains("شوكولا") ||
            lower.contains("halwa") || lower.contains("حلوة الترك") || lower.contains("gateau") || lower.contains("حلويات") -> "SweetsNuts"

            lower.contains("gingembre") || lower.contains("زنجبيل") || lower.contains("menthe") || lower.contains("نعناع") ||
            lower.contains("thym") || lower.contains("زعتر") || lower.contains("tisane") || lower.contains("تيزانة") ||
            lower.contains("camomille") || lower.contains("بابونج") || lower.contains("verveine") || lower.contains("لويزة") ||
            lower.contains("tea") || lower.contains("thé") || lower.contains("شاي") -> "HerbalDrinks"

            lower.contains("water") || lower.contains("eau") || lower.contains("ماء") ||
            lower.contains("juice") || lower.contains("jus") || lower.contains("عصير") ||
            lower.contains("soda") || lower.contains("gazouz") || lower.contains("مشروب") ||
            lower.contains("cola") || lower.contains("fanta") || lower.contains("حمود") || lower.contains("hamoud") -> "Beverages"

            lower.contains("soap") || lower.contains("savon") || lower.contains("صابون") ||
            lower.contains("detergent") || lower.contains("lessive") || lower.contains("مسحوق") ||
            lower.contains("isis") || lower.contains("omoe") || lower.contains("ariel") ||
            lower.contains("shampoo") || lower.contains("شامبو") || lower.contains("javel") || lower.contains("جافيل") -> "Household"

            else -> "Pantry"
        }
    }

    /**
     * Serializes items for QR offline sharing
     * Format: SOUK|name;nameAr;category;quantity;unit;barcode;estimatedPrice
     */
    fun createQrShareString(items: List<ShoppingItem>): String {
        val payload = items.joinToString("~") { item ->
            "${item.name};${item.nameAr};${item.category};${item.quantity};${item.unit};${item.barcode ?: ""};${item.estimatedPriceDzd}"
        }
        return "SOUK|$payload"
    }

    suspend fun importFromQrString(qrData: String): Int {
        if (!qrData.startsWith("SOUK|")) return 0
        val payload = qrData.substringAfter("SOUK|")
        val itemTokens = payload.split("~")
        var count = 0
        for (token in itemTokens) {
            val parts = token.split(";")
            if (parts.size >= 4) {
                val item = ShoppingItem(
                    name = parts[0],
                    nameAr = parts.getOrNull(1)?.ifBlank { parts[0] } ?: parts[0],
                    category = parts.getOrNull(2) ?: "Pantry",
                    quantity = parts.getOrNull(3)?.toIntOrNull() ?: 1,
                    unit = parts.getOrNull(4)?.ifBlank { "pcs" } ?: "pcs",
                    barcode = parts.getOrNull(5)?.ifBlank { null },
                    estimatedPriceDzd = parts.getOrNull(6)?.toDoubleOrNull() ?: 0.0,
                    isChecked = false
                )
                insertItem(item)
                count++
            }
        }
        return count
    }
}
