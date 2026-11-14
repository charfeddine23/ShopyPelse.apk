package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitialData
import com.example.data.local.MarketPriceDao
import com.example.data.local.ShoppingDao
import com.example.data.local.SpendingDao
import com.example.data.model.MarketPrice
import com.example.data.model.PriceHistory
import com.example.data.model.ShoppingItem
import com.example.data.model.SpendingRecord
import com.example.data.model.StoreBasketSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ShoppingRepository(
    private val shoppingDao: ShoppingDao,
    private val marketPriceDao: MarketPriceDao,
    private val spendingDao: SpendingDao
) {
    val allShoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAllShoppingItems()
    val allMarketPrices: Flow<List<MarketPrice>> = marketPriceDao.getAllMarketPrices()
    val allSpendingRecords: Flow<List<SpendingRecord>> = spendingDao.getAllSpendingRecords()

    suspend fun recordShoppingTrip(record: SpendingRecord): Long = spendingDao.insertSpendingRecord(record)

    suspend fun deleteSpendingRecord(id: Long) = spendingDao.deleteSpendingRecord(id)

    suspend fun ensureSpendingSeeded(alreadySeeded: Boolean): Boolean {
        if (alreadySeeded) return true
        return AppDatabase.seedMutex.withLock {
            if (spendingDao.getSpendingRecordCount() == 0) {
                spendingDao.insertSpendingRecords(DatabaseInitialData.initialSpendingRecords)
            }
            true
        }
    }

    suspend fun insertItem(item: ShoppingItem): Long {
        val detected = autoCategorize(item.name, item.nameAr)
        val category = if (item.category.isBlank() || item.category == "Other" || (item.category == "Produce" && detected != "Produce")) {
            detected
        } else {
            item.category
        }

        val sanitizedPrice = if (item.estimatedPriceDzd.isFinite()) item.estimatedPriceDzd.coerceAtLeast(0.0) else 0.0
        // If barcode exists, try to enrich estimated price from best store
        val estimatedPrice = if (item.barcode != null && sanitizedPrice <= 0.0) {
            val best = marketPriceDao.getBestPriceForBarcode(item.barcode)
            best?.priceDzd ?: 0.0
        } else {
            sanitizedPrice
        }

        return shoppingDao.insertItem(
            item.copy(
                name = item.name.trim(),
                nameAr = item.nameAr.trim().ifBlank { item.name.trim() },
                category = category,
                quantity = item.quantity.coerceIn(1, 999),
                estimatedPriceDzd = estimatedPrice
            )
        )
    }

    suspend fun insertItems(items: List<ShoppingItem>) {
        if (items.isNotEmpty()) {
            shoppingDao.insertItems(items)
        }
    }

    suspend fun sanitizeExistingItemCategories() {
        val items = shoppingDao.getAllShoppingItems().first()
        for (item in items) {
            if (item.category.isBlank() || item.category == "Other" || item.category == "Produce") {
                val detected = autoCategorize(item.name, item.nameAr)
                if (detected != item.category && (item.category == "Produce" && detected != "Produce" || item.category != "Produce")) {
                    shoppingDao.updateItem(item.copy(category = detected))
                }
            }
        }
    }

    suspend fun updateItem(item: ShoppingItem) = shoppingDao.updateItem(item)

    suspend fun deleteItem(item: ShoppingItem) = shoppingDao.deleteItem(item)

    suspend fun toggleChecked(id: Long, isChecked: Boolean) = shoppingDao.updateCheckedStatus(id, isChecked)

    suspend fun updateQuantity(id: Long, quantity: Int) = shoppingDao.updateQuantity(id, quantity.coerceIn(1, 999))

    suspend fun clearCheckedItems() = shoppingDao.deleteCheckedItems()

    suspend fun clearAllItems() = shoppingDao.clearAll()

    suspend fun getItemByBarcode(barcode: String): ShoppingItem? = shoppingDao.getItemByBarcode(barcode)

    fun getPricesForBarcode(barcode: String): Flow<List<MarketPrice>> =
        marketPriceDao.getPricesForBarcode(barcode)

    fun searchPrices(query: String): Flow<List<MarketPrice>> =
        marketPriceDao.searchMarketPrices(query)

    suspend fun getPriceHistory(barcode: String): Flow<List<PriceHistory>> =
        marketPriceDao.getPriceHistory(barcode)

    suspend fun ensureCatalogPopulated(alreadySeeded: Boolean): Boolean {
        if (alreadySeeded) return true
        return AppDatabase.seedMutex.withLock {
            val count = marketPriceDao.getMarketPriceCount()
            if (count < 4900) {
                val catalog5000 = com.example.data.local.CatalogDataGenerator.generate5000Products()
                val marketPrices5000 = com.example.data.local.CatalogDataGenerator.generateMarketPricesForCatalog(catalog5000)
                marketPriceDao.insertMarketPrices(marketPrices5000)
            }
            true
        }
    }

    suspend fun addNewProductPrice(
        name: String,
        nameAr: String,
        storeName: String,
        storeNameAr: String,
        priceDzd: Double,
        barcode: String? = null
    ): String {
        val safePrice = if (priceDzd.isFinite()) priceDzd.coerceAtLeast(1.0) else 50.0
        val finalBarcode = if (!barcode.isNullOrBlank()) barcode else (6130000000000L + (System.currentTimeMillis() % 1000000000L)).toString()
        val marketPrice = MarketPrice(
            barcode = finalBarcode,
            itemName = name.trim(),
            itemNameAr = nameAr.trim().ifBlank { name.trim() },
            storeName = storeName,
            storeNameAr = storeNameAr,
            priceDzd = safePrice,
            verifiedCount = 1
        )
        marketPriceDao.insertMarketPrice(marketPrice)
        return finalBarcode
    }

    suspend fun updateProductPrice(barcode: String, storeName: String?, newPrice: Double) {
        if (!newPrice.isFinite() || newPrice <= 0.0) return
        if (storeName.isNullOrBlank()) {
            marketPriceDao.updatePriceForBarcode(barcode, newPrice)
        } else {
            val existing = marketPriceDao.getPriceForBarcodeAndStore(barcode, storeName)
            if (existing != null) {
                marketPriceDao.updatePriceReport(barcode, storeName, newPrice)
            } else {
                marketPriceDao.updatePriceForBarcode(barcode, newPrice)
            }
        }
    }

    suspend fun reportCrowdsourcedPrice(barcode: String, storeName: String, storeNameAr: String, itemName: String, newPrice: Double) {
        if (!newPrice.isFinite() || newPrice <= 0.0) return
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
     * Evaluates all shopping items across known supermarkets on Dispatchers.Default
     * using pre-indexed HashMaps for O(1) lookup per item.
     */
    suspend fun computeBasketComparison(items: List<ShoppingItem>): List<StoreBasketSummary> = withContext(Dispatchers.Default) {
        if (items.isEmpty()) return@withContext emptyList()

        val allPrices = marketPriceDao.getAllMarketPrices().first()
        if (allPrices.isEmpty()) return@withContext emptyList()

        val stores = LinkedHashMap<String, String>()
        val byStoreAndBarcode = HashMap<String, HashMap<String, MarketPrice>>()
        val byStoreAndName = HashMap<String, HashMap<String, MarketPrice>>()

        for (p in allPrices) {
            stores.putIfAbsent(p.storeName, p.storeNameAr)
            byStoreAndBarcode.getOrPut(p.storeName) { HashMap() }.putIfAbsent(p.barcode, p)
            val nameMap = byStoreAndName.getOrPut(p.storeName) { HashMap() }
            nameMap.putIfAbsent(p.itemName.lowercase(), p)
            nameMap.putIfAbsent(p.itemNameAr.lowercase(), p)
        }

        val summaries = stores.map { (storeName, storeNameAr) ->
            val barcodeMap = byStoreAndBarcode[storeName]
            val nameMap = byStoreAndName[storeName]
            var storeTotal = 0.0
            var foundCount = 0

            for (item in items) {
                val priceObj = (item.barcode?.let { barcodeMap?.get(it) })
                    ?: nameMap?.get(item.name.lowercase())
                    ?: nameMap?.get(item.nameAr.lowercase())

                if (priceObj != null) {
                    storeTotal += (priceObj.priceDzd * item.quantity)
                    foundCount++
                } else {
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

        if (summaries.isEmpty()) return@withContext emptyList()

        val minTotal = summaries.minOf { it.totalDzd }
        val maxTotal = summaries.maxOf { it.totalDzd }

        summaries.map { summary ->
            val isCheapest = summary.totalDzd == minTotal && minTotal > 0.0
            val savings = if (maxTotal > summary.totalDzd) maxTotal - summary.totalDzd else 0.0
            summary.copy(isBestValue = isCheapest, savingsComparedToMaxDzd = savings)
        }.sortedBy { it.totalDzd }
    }

    /**
     * Auto-categorizes an item based on English, French, and Arabic grocery keywords.
     */
    fun autoCategorize(name: String, nameAr: String = ""): String {
        val text = "$name $nameAr".lowercase()
        return when {
            // Herbal Drinks & Teas
            text.contains("gingembre") || text.contains("زنجبيل") ||
            text.contains("menthe") || text.contains("نعناع") ||
            text.contains("thym") || text.contains("زعتر") || text.contains("زعيترة") ||
            text.contains("tisane") || text.contains("تيزانة") || text.contains("تيزانات") ||
            text.contains("camomille") || text.contains("بابونج") ||
            text.contains("verveine") || text.contains("لويزة") ||
            text.contains("infusion") || text.contains("زهورات") ||
            text.contains("anis") || text.contains("حبة حلاوة") || text.contains("يانسون") ||
            text.contains("tea") || text.contains("thé") || text.contains("شاي") || text.contains("تاي") || text.contains("أتاي") -> "HerbalDrinks"

            // Sweets & Nuts
            text.contains("amande") || text.contains("لوز") ||
            text.contains("pistache") || text.contains("فستق") ||
            text.contains("noisette") || text.contains("بندق") ||
            text.contains("cacahuete") || text.contains("cacahuète") || text.contains("كاوكاو") ||
            text.contains("noix") || text.contains("جوز") ||
            text.contains("cajou") || text.contains("كاجو") ||
            text.contains("sesame") || text.contains("جلجلان") || text.contains("سمسم") ||
            text.contains("nut") || text.contains("مكسرات") ||
            text.contains("bonbon") || text.contains("حلوى") || text.contains("سكاكر") ||
            text.contains("chocolat") || text.contains("chocolate") || text.contains("شوكولا") || text.contains("شوكولاتة") ||
            text.contains("biscuit") || text.contains("بسكويت") || text.contains("gaufrette") || text.contains("غوفرات") ||
            text.contains("halwa") || text.contains("halva") || text.contains("حلوة الترك") || text.contains("شامية") ||
            text.contains("gateau") || text.contains("gâteau") || text.contains("قاطو") || text.contains("حلويات") ||
            text.contains("makroud") || text.contains("مقروط") || text.contains("baklawa") || text.contains("بقلاوة") ||
            text.contains("qalb louz") || text.contains("قلب اللوز") || text.contains("zlabia") || text.contains("زلابية") ||
            text.contains("caramel") || text.contains("كراميل") || text.contains("nougat") || text.contains("نوغة") ||
            text.contains("sablé") || text.contains("صابلي") -> "SweetsNuts"

            // Dairy & Cheese
            text.contains("milk") || text.contains("lait") || text.contains("حليب") ||
            text.contains("cheese") || text.contains("fromage") || text.contains("جبن") || text.contains("جبنة") || text.contains("أجبان") || text.contains("فرماج") ||
            text.contains("yaourt") || text.contains("yogurt") || text.contains("ياغورت") ||
            text.contains("butter") || text.contains("beurre") || text.contains("زبدة") ||
            text.contains("crème") || text.contains("creme") || text.contains("cream") || text.contains("قشطة") || text.contains("كريمة") ||
            text.contains("leben") || text.contains("laban") || text.contains("لبن") ||
            text.contains("rayeb") || text.contains("رايب") ||
            text.contains("camembert") || text.contains("gruyere") || text.contains("gruyère") || text.contains("غرويير") ||
            text.contains("mozzarella") || text.contains("موزاريلا") || text.contains("edam") || text.contains("cheddar") || text.contains("شيدر") ||
            text.contains("berbere") || text.contains("berbère") || text.contains("بربر") ||
            text.contains("tartino") || text.contains("تارتينو") ||
            text.contains("kiri") || text.contains("كيري") ||
            text.contains("vache qui rit") || text.contains("البقرة الضاحكة") ||
            text.contains("egg") || text.contains("oeuf") || text.contains("بيض") -> "Dairy"

            // Bakery
            text.contains("bread") || text.contains("pain") || text.contains("خبز") ||
            text.contains("baguette") || text.contains("باقيت") || text.contains("باكيط") ||
            text.contains("croissant") || text.contains("كرواسون") ||
            text.contains("brioche") || text.contains("بريوش") ||
            text.contains("toast") || text.contains("توست") ||
            text.contains("biscotte") || text.contains("بسكوت") ||
            text.contains("matlouh") || text.contains("مطلوع") ||
            text.contains("kesra") || text.contains("كسرة") ||
            text.contains("msemen") || text.contains("مسمن") ||
            text.contains("mahjouba") || text.contains("محاجب") ||
            text.contains("galette") || text.contains("بغرير") -> "Bakery"

            // Butchery & Meat & Fish
            text.contains("chicken") || text.contains("poulet") || text.contains("دجاج") || text.contains("بولي") ||
            text.contains("meat") || text.contains("viande") || text.contains("لحم") || text.contains("لحوم") ||
            text.contains("beef") || text.contains("boeuf") || text.contains("bœuf") || text.contains("بقر") || text.contains("بقري") || text.contains("عجل") ||
            text.contains("lamb") || text.contains("agneau") || text.contains("mouton") || text.contains("خروف") || text.contains("غنم") || text.contains("غنمي") ||
            text.contains("turkey") || text.contains("dinde") || text.contains("داند") ||
            text.contains("steak") || text.contains("ستيك") ||
            text.contains("escalope") || text.contains("سكالوب") ||
            text.contains("kefta") || text.contains("kofta") || text.contains("كفتة") || text.contains("مفروم") || text.contains("مرحي") ||
            text.contains("merguez") || text.contains("مرقاز") || text.contains("saucisse") || text.contains("نقانق") ||
            text.contains("cachir") || text.contains("kashir") || text.contains("كاشير") || text.contains("كشير") ||
            text.contains("fish") || text.contains("poisson") || text.contains("سمك") || text.contains("حوت") ||
            text.contains("sardine") || text.contains("سردين") ||
            text.contains("tuna") || text.contains("thon") || text.contains("طون") || text.contains("تونة") ||
            text.contains("crevette") || text.contains("جمبري") || text.contains("كروفيت") -> "Butchery"

            // Beverages
            text.contains("water") || text.contains("eau") || text.contains("ماء") || text.contains("مياه") ||
            text.contains("juice") || text.contains("jus") || text.contains("عصير") ||
            text.contains("soda") || text.contains("gazouz") || text.contains("غازوز") || text.contains("مشروب") ||
            text.contains("cola") || text.contains("كولا") ||
            text.contains("fanta") || text.contains("فانتا") ||
            text.contains("pepsi") || text.contains("بيبسي") ||
            text.contains("hamoud") || text.contains("حمود") || text.contains("بوعلام") ||
            text.contains("ifri") || text.contains("إفري") || text.contains("افري") ||
            text.contains("ramy") || text.contains("رامي") ||
            text.contains("rouiba") || text.contains("رويبة") ||
            text.contains("ngaous") || text.contains("نقاوس") ||
            text.contains("coffee") || text.contains("café") || text.contains("cafe") || text.contains("قهوة") || text.contains("نسكافيه") || text.contains("بن") -> "Beverages"

            // Household & Cleaning
            text.contains("soap") || text.contains("savon") || text.contains("صابون") ||
            text.contains("detergent") || text.contains("lessive") || text.contains("مسحوق") || text.contains("غسيل") ||
            text.contains("javel") || text.contains("جافيل") || text.contains("كلور") ||
            text.contains("isis") || text.contains("إيزيس") || text.contains("ايزيس") ||
            text.contains("omo") || text.contains("اومو") ||
            text.contains("ariel") || text.contains("أرييل") || text.contains("ارييل") ||
            text.contains("pril") || text.contains("بريل") ||
            text.contains("shampoo") || text.contains("shampoing") || text.contains("شامبو") ||
            text.contains("toothpaste") || text.contains("dentifrice") || text.contains("معجون أسنان") ||
            text.contains("colgate") || text.contains("كولجيت") || text.contains("signal") || text.contains("سيغنال") ||
            text.contains("sponge") || text.contains("eponge") || text.contains("إسفنجة") || text.contains("حبلة") ||
            text.contains("essuie-tout") || text.contains("مناديل") || text.contains("ورق مرحاض") -> "Household"

            // Produce (Fruits and Vegetables)
            text.contains("tomato") || text.contains("tomate") || text.contains("طماطم") || text.contains("طماطيش") ||
            text.contains("potato") || text.contains("pomme de terre") || text.contains("بطاطا") ||
            text.contains("onion") || text.contains("oignon") || text.contains("بصل") ||
            text.contains("garlic") || text.contains("ail") || text.contains("ثوم") ||
            text.contains("apple") || text.contains("pomme") || text.contains("تفاح") ||
            text.contains("banana") || text.contains("banane") || text.contains("موز") || text.contains("بنان") ||
            text.contains("orange") || text.contains("برتقال") || text.contains("تشينة") ||
            text.contains("lemon") || text.contains("citron") || text.contains("ليمون") || text.contains("قارص") ||
            text.contains("strawberry") || text.contains("fraise") || text.contains("فراولة") || text.contains("فريز") ||
            text.contains("peach") || text.contains("pêche") || text.contains("peche") || text.contains("خوخ") ||
            text.contains("apricot") || text.contains("abricot") || text.contains("مشمش") ||
            text.contains("grape") || text.contains("raisin") || text.contains("عنب") ||
            text.contains("watermelon") || text.contains("pastèque") || text.contains("pasteque") || text.contains("دلاع") ||
            text.contains("melon") || text.contains("بطيخ") || text.contains("شمام") ||
            text.contains("carrot") || text.contains("carotte") || text.contains("جزر") || text.contains("زرودية") ||
            text.contains("cucumber") || text.contains("concombre") || text.contains("خيار") ||
            text.contains("lettuce") || text.contains("salade") || text.contains("خس") || text.contains("سلاطة") || text.contains("سلطة") ||
            text.contains("courgette") || text.contains("zucchini") || text.contains("كوسة") || text.contains("جريوات") || text.contains("قرعة") ||
            text.contains("aubergine") || text.contains("باذنجان") ||
            text.contains("poivron") || text.contains("فلفل") || text.contains("حار") ||
            text.contains("haricot") || text.contains("لوبيا خضراء") ||
            text.contains("petit pois") || text.contains("جلبانة") || text.contains("بسلة") ||
            text.contains("spinach") || text.contains("épinard") || text.contains("سبانخ") || text.contains("سلق") ||
            text.contains("navet") || text.contains("لفت") ||
            text.contains("betterave") || text.contains("شمندر") || text.contains("بيطراف") ||
            text.contains("date") || text.contains("deglet") || text.contains("تمر") || text.contains("تمور") || text.contains("دقلة") ||
            text.contains("olive") || text.contains("زيتون") ||
            text.contains("fig") || text.contains("figue") || text.contains("تين") || text.contains("كرموس") -> "Produce"

            // Pantry (Pasta, Couscous, Flour, Rice, Sugar, Oil, Spices, Canned, etc.)
            text.contains("couscous") || text.contains("كسكس") || text.contains("طعام") ||
            text.contains("pasta") || text.contains("pâte") || text.contains("pates") || text.contains("pâtes") ||
            text.contains("macaroni") || text.contains("ماكارونة") || text.contains("مقرونة") || text.contains("معكرونة") || text.contains("مكرونة") ||
            text.contains("spaghetti") || text.contains("سباغيتي") ||
            text.contains("rice") || text.contains("riz") || text.contains("أرز") || text.contains("روز") ||
            text.contains("flour") || text.contains("farine") || text.contains("فرينة") ||
            text.contains("semolina") || text.contains("semoule") || text.contains("سميد") || text.contains("دقيق") ||
            text.contains("sugar") || text.contains("sucre") || text.contains("سكر") ||
            text.contains("salt") || text.contains("sel") || text.contains("ملح") ||
            text.contains("oil") || text.contains("huile") || text.contains("زيت") ||
            text.contains("concentré") || text.contains("طماطم مصبرة") || text.contains("كونسونطري") ||
            text.contains("vinaigre") || text.contains("خل") ||
            text.contains("mayonnaise") || text.contains("مايونيز") ||
            text.contains("moutarde") || text.contains("خردل") ||
            text.contains("harissa") || text.contains("هريسة") ||
            text.contains("lentille") || text.contains("lentil") || text.contains("عدس") ||
            text.contains("pois chiche") || text.contains("chickpea") || text.contains("حمص") ||
            text.contains("fève") || text.contains("feve") || text.contains("فول") ||
            text.contains("epice") || text.contains("épice") || text.contains("توابل") || text.contains("بهارات") ||
            text.contains("poivre") || text.contains("فلفل أسود") || text.contains("فلفل كحل") ||
            text.contains("cumin") || text.contains("كمون") ||
            text.contains("ras el hanout") || text.contains("رأس الحانوت") ||
            text.contains("curcuma") || text.contains("كركم") ||
            text.contains("frik") || text.contains("فريك") || text.contains("شربة") -> "Pantry"

            else -> "Pantry"
        }
    }

    private fun sanitizeQrField(value: String): String =
        value.replace(";", " ")
            .replace("~", " ")
            .replace("|", " ")
            .replace("\r", " ")
            .replace("\n", " ")
            .trim()
            .take(120)

    /**
     * Serializes items for QR offline sharing
     * Format: SOUK|name;nameAr;category;quantity;unit;barcode;estimatedPrice
     */
    fun createQrShareString(items: List<ShoppingItem>): String {
        val payload = items.take(250).joinToString("~") { item ->
            val name = sanitizeQrField(item.name)
            val nameAr = sanitizeQrField(item.nameAr)
            val category = sanitizeQrField(item.category).ifBlank { "Pantry" }
            val unit = sanitizeQrField(item.unit).ifBlank { "pcs" }
            val barcode = sanitizeQrField(item.barcode ?: "")
            val qty = item.quantity.coerceIn(1, 999)
            val price = if (item.estimatedPriceDzd.isFinite()) item.estimatedPriceDzd.coerceIn(0.0, 10_000_000.0) else 0.0
            "$name;$nameAr;$category;$qty;$unit;$barcode;$price"
        }
        return "SOUK|$payload"
    }

    suspend fun importFromQrString(qrData: String): Int {
        val trimmed = qrData.trim()
        if (!trimmed.startsWith("SOUK|") || trimmed.length > 64_000) return 0
        val payload = trimmed.substringAfter("SOUK|")
        if (payload.isBlank()) return 0
        val itemTokens = payload.split("~").take(250)
        var count = 0
        for (token in itemTokens) {
            val parts = token.split(";")
            if (parts.size >= 4) {
                val rawName = parts[0].trim().take(120)
                if (rawName.isBlank()) continue
                val rawNameAr = parts.getOrNull(1)?.trim()?.take(120)?.ifBlank { rawName } ?: rawName
                val rawCategory = parts.getOrNull(2)?.trim()?.take(40)?.ifBlank { "Pantry" } ?: "Pantry"
                val safeQty = (parts.getOrNull(3)?.trim()?.toIntOrNull() ?: 1).coerceIn(1, 999)
                val safeUnit = parts.getOrNull(4)?.trim()?.take(24)?.ifBlank { "pcs" } ?: "pcs"
                val safeBarcode = parts.getOrNull(5)?.trim()?.take(64)?.ifBlank { null }
                val rawPrice = parts.getOrNull(6)?.trim()?.toDoubleOrNull() ?: 0.0
                val safePrice = if (rawPrice.isFinite()) rawPrice.coerceIn(0.0, 10_000_000.0) else 0.0

                val item = ShoppingItem(
                    name = rawName,
                    nameAr = rawNameAr,
                    category = rawCategory,
                    quantity = safeQty,
                    unit = safeUnit,
                    barcode = safeBarcode,
                    estimatedPriceDzd = safePrice,
                    isChecked = false
                )
                insertItem(item)
                count++
            }
        }
        return count
    }
}
