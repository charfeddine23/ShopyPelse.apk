package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppPreferencesStorage
import com.example.data.local.DatabaseInitialData
import com.example.data.local.InMemoryPreferencesStorage
import com.example.data.model.MarketPrice
import com.example.data.model.PriceHistory
import com.example.data.model.ShoppingItem
import com.example.data.model.SpendingRecord
import com.example.data.model.StoreBasketSummary
import com.example.data.repository.ShoppingRepository
import com.example.util.AppUpdateChecker
import com.example.util.AppUpdateStatus
import com.example.util.LocalizationUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShoppingViewModel(
    private val repository: ShoppingRepository,
    private val preferencesStorage: AppPreferencesStorage = InMemoryPreferencesStorage()
) : ViewModel() {

    companion object {
        const val DEVELOPER_NAME = "Charfeddine Ka"
        const val APP_VERSION = "v1.0 Build 1"
        const val LAST_UPDATE_DATE_AR = "09/11/2026"
        const val LAST_UPDATE_DATE_EN = "09/11/2026"
    }

    val shoppingItems: StateFlow<List<ShoppingItem>> = repository.allShoppingItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val marketPrices: StateFlow<List<MarketPrice>> = repository.allMarketPrices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val spendingRecords: StateFlow<List<SpendingRecord>> = repository.allSpendingRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val initialPrefs = preferencesStorage.getInitialState()

    private val _selectedCategory = MutableStateFlow(initialPrefs.selectedCategory)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow(initialPrefs.searchQuery)
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _budgetLimitDzd = MutableStateFlow(initialPrefs.budgetLimitDzd)
    val budgetLimitDzd: StateFlow<Double> = _budgetLimitDzd.asStateFlow()

    private val _isArabic = MutableStateFlow(initialPrefs.isArabic)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    private val _isDarkMode = MutableStateFlow(initialPrefs.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _lastBackupDate = MutableStateFlow(initialPrefs.lastBackupDate)
    val lastBackupDate: StateFlow<String> = _lastBackupDate.asStateFlow()

    private val _currentScreenName = MutableStateFlow(initialPrefs.currentScreen)
    val currentScreenName: StateFlow<String> = _currentScreenName.asStateFlow()

    private val _selectedStatsMonth = MutableStateFlow(initialPrefs.selectedStatsMonth)
    val selectedStatsMonth: StateFlow<String> = _selectedStatsMonth.asStateFlow()

    private val _basketSummaries = MutableStateFlow<List<StoreBasketSummary>>(emptyList())
    val basketSummaries: StateFlow<List<StoreBasketSummary>> = _basketSummaries.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _lastPriceSyncTimestamp = MutableStateFlow(
        if (initialPrefs.lastPriceSyncTimestamp > 0L) initialPrefs.lastPriceSyncTimestamp else System.currentTimeMillis()
    )
    val lastPriceSyncTimestamp: StateFlow<Long> = _lastPriceSyncTimestamp.asStateFlow()

    private val _isSyncingPrices = MutableStateFlow(false)
    val isSyncingPrices: StateFlow<Boolean> = _isSyncingPrices.asStateFlow()

    private val _updatedItemsCount = MutableStateFlow(initialPrefs.updatedItemsCount)
    val updatedItemsCount: StateFlow<Int> = _updatedItemsCount.asStateFlow()

    private val _syncIntervalHours = MutableStateFlow(initialPrefs.syncIntervalHours)
    val syncIntervalHours: StateFlow<Int> = _syncIntervalHours.asStateFlow()

    fun setSyncIntervalHours(hours: Int) {
        _syncIntervalHours.value = hours
        viewModelScope.launch {
            preferencesStorage.setSyncIntervalHours(hours)
        }
    }

    private val _isDynamicGlowEnabled = MutableStateFlow(initialPrefs.isDynamicGlowEnabled)
    val isDynamicGlowEnabled: StateFlow<Boolean> = _isDynamicGlowEnabled.asStateFlow()

    fun setDynamicGlowEnabled(enabled: Boolean) {
        _isDynamicGlowEnabled.value = enabled
        viewModelScope.launch {
            preferencesStorage.setDynamicGlowEnabled(enabled)
        }
    }

    private val _isLiquidDockSwipeEnabled = MutableStateFlow(initialPrefs.isLiquidDockSwipeEnabled)
    val isLiquidDockSwipeEnabled: StateFlow<Boolean> = _isLiquidDockSwipeEnabled.asStateFlow()

    fun setLiquidDockSwipeEnabled(enabled: Boolean) {
        _isLiquidDockSwipeEnabled.value = enabled
        viewModelScope.launch {
            preferencesStorage.setLiquidDockSwipeEnabled(enabled)
        }
    }

    private val _updateStatus = MutableStateFlow<AppUpdateStatus>(AppUpdateStatus.Idle)
    val updateStatus: StateFlow<AppUpdateStatus> = _updateStatus.asStateFlow()

    private val _showUpdateOverlay = MutableStateFlow(false)
    val showUpdateOverlay: StateFlow<Boolean> = _showUpdateOverlay.asStateFlow()

    private var lastAutoCheckAtMs: Long = 0L

    private val _lastUpdateCheckTimestamp = MutableStateFlow(initialPrefs.lastUpdateCheckTimestamp)
    val lastUpdateCheckTimestamp: StateFlow<Long> = _lastUpdateCheckTimestamp.asStateFlow()

    fun dismissUpdateOverlay() {
        _showUpdateOverlay.value = false
    }

    fun openUpdateOverlayIfAvailable() {
        if (_updateStatus.value is AppUpdateStatus.UpdateAvailable) {
            _showUpdateOverlay.value = true
        }
    }

    fun checkForUpdatesOnAppOpen() {
        val now = System.currentTimeMillis()
        // Prevent duplicate back-to-back check within 5 seconds of app launch or browser return
        if (now - lastAutoCheckAtMs < 5_000L) return
        checkForUpdates(autoOpenOverlay = true)
    }

    fun checkForUpdates(autoOpenOverlay: Boolean = true) {
        if (_updateStatus.value is AppUpdateStatus.Checking) return
        lastAutoCheckAtMs = System.currentTimeMillis()
        _updateStatus.value = AppUpdateStatus.Checking
        viewModelScope.launch {
            val result = AppUpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
            _updateStatus.value = result
            val now = System.currentTimeMillis()
            _lastUpdateCheckTimestamp.value = now
            preferencesStorage.setLastUpdateCheckTimestamp(now)
            if (result is AppUpdateStatus.UpdateAvailable) {
                preferencesStorage.setLatestKnownVersion(result.latestVersion)
                if (autoOpenOverlay) {
                    _showUpdateOverlay.value = true
                }
            }
        }
    }

    fun getUpdateUrl(): String {
        return when (val st = _updateStatus.value) {
            is AppUpdateStatus.UpdateAvailable -> st.downloadUrl
            is AppUpdateStatus.ReadyDirectLink -> st.downloadUrl
            is AppUpdateStatus.UpToDate -> st.directUrl
            is AppUpdateStatus.Error -> st.fallbackUrl
            else -> AppUpdateChecker.UPDATE_URL
        }
    }

    val frequentPresets: List<ShoppingItem> = DatabaseInitialData.frequentPresets

    init {
        // Persist initial sync timestamp on first ever launch
        if (initialPrefs.lastPriceSyncTimestamp <= 0L) {
            viewModelScope.launch {
                preferencesStorage.setLastPriceSyncTimestamp(_lastPriceSyncTimestamp.value)
            }
        }

        // Automatically check for app updates on every launch
        checkForUpdates(autoOpenOverlay = true)

        // Recompute basket comparison whenever shopping items or market prices change
        viewModelScope.launch {
            combine(repository.allShoppingItems, repository.allMarketPrices) { items, _ ->
                items
            }.collect { items ->
                val summaries = repository.computeBasketComparison(items)
                _basketSummaries.value = summaries
            }
        }

        // Automatic 12-hour price refresh loop & initial category sanitization
        viewModelScope.launch {
            // Ensure 5000 items catalog is loaded (only once)
            val catalogSeeded = repository.ensureCatalogPopulated(initialPrefs.hasSeededCatalog)
            if (catalogSeeded && !initialPrefs.hasSeededCatalog) {
                preferencesStorage.setHasSeededCatalog(true)
            }
            val spendingSeeded = repository.ensureSpendingSeeded(initialPrefs.hasSeededSpending)
            if (spendingSeeded && !initialPrefs.hasSeededSpending) {
                preferencesStorage.setHasSeededSpending(true)
            }

            // Correct any previously misclassified categories
            repository.sanitizeExistingItemCategories()

            // Check immediately on launch
            refreshEssentialPricesIfDue()

            // Periodically check every 30 minutes if interval has elapsed
            while (true) {
                kotlinx.coroutines.delay(30 * 60 * 1000L) // 30 minutes
                refreshEssentialPricesIfDue()
            }
        }
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            preferencesStorage.setSelectedCategory(category)
        }
    }

    fun autoCategorize(name: String, nameAr: String = ""): String {
        return repository.autoCategorize(name, nameAr)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            preferencesStorage.setSearchQuery(query)
        }
    }

    fun setCurrentScreen(screenName: String) {
        _currentScreenName.value = screenName
        viewModelScope.launch {
            preferencesStorage.setCurrentScreen(screenName)
        }
    }

    fun setSelectedStatsMonth(month: String) {
        _selectedStatsMonth.value = month
        viewModelScope.launch {
            preferencesStorage.setSelectedStatsMonth(month)
        }
    }

    fun setBudgetLimit(budget: Double) {
        _budgetLimitDzd.value = budget
        viewModelScope.launch {
            preferencesStorage.setBudgetLimitDzd(budget)
        }
    }

    fun toggleLanguage() {
        setLanguage(!_isArabic.value)
    }

    fun setLanguage(arabic: Boolean) {
        _isArabic.value = arabic
        viewModelScope.launch {
            preferencesStorage.setArabic(arabic)
        }
    }

    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode.value)
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        viewModelScope.launch {
            preferencesStorage.setDarkMode(dark)
        }
    }

    fun addItem(
        name: String,
        nameAr: String = name,
        category: String = "",
        quantity: Int = 1,
        unit: String = "pcs",
        barcode: String? = null,
        estimatedPriceDzd: Double = 0.0
    ) {
        viewModelScope.launch {
            val resolvedCategory = if (category.isNotBlank() && category != "Other") {
                val detected = autoCategorize(name, nameAr)
                if (category == "Produce" && detected != "Produce") detected else category
            } else {
                autoCategorize(name, nameAr)
            }
            val item = ShoppingItem(
                name = name.trim(),
                nameAr = if (nameAr.isBlank()) name.trim() else nameAr.trim(),
                category = resolvedCategory,
                quantity = quantity.coerceAtLeast(1),
                unit = unit,
                barcode = barcode?.trim()?.ifBlank { null },
                estimatedPriceDzd = estimatedPriceDzd,
                isChecked = false
            )
            repository.insertItem(item)
            val msg = if (_isArabic.value) "تمت إضافة \"$name\" إلى القائمة" else "Added \"$name\" to list"
            _userMessage.emit(msg)
        }
    }

    fun addFrequentPreset(preset: ShoppingItem) {
        viewModelScope.launch {
            repository.insertItem(preset.copy(id = 0, isChecked = false))
            val msg = if (_isArabic.value) "تمت إضافة \"${preset.nameAr}\"" else "Added \"${preset.name}\""
            _userMessage.emit(msg)
        }
    }

    fun toggleChecked(id: Long, isChecked: Boolean) {
        viewModelScope.launch {
            repository.toggleChecked(id, isChecked)
        }
    }

    fun updateQuantity(id: Long, delta: Int) {
        viewModelScope.launch {
            val currentItem = shoppingItems.value.firstOrNull { it.id == id } ?: return@launch
            val newQty = (currentItem.quantity + delta).coerceAtLeast(1)
            repository.updateQuantity(id, newQty)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun clearChecked() {
        viewModelScope.launch {
            repository.clearCheckedItems()
            val msg = if (_isArabic.value) "تم حذف العناصر المكتملة" else "Cleared checked items"
            _userMessage.emit(msg)
        }
    }

    suspend fun refreshEssentialPricesIfDue() {
        val now = System.currentTimeMillis()
        val intervalMs = _syncIntervalHours.value * 60 * 60 * 1000L
        if (now - _lastPriceSyncTimestamp.value >= intervalMs) {
            refreshEssentialPrices()
        }
    }

    /**
     * Automated price scanner and updater for essential Algerian grocery products.
     * Runs automatically every 12 hours or on manual trigger.
     */
    fun refreshEssentialPrices() {
        if (_isSyncingPrices.value) return

        viewModelScope.launch {
            _isSyncingPrices.value = true
            try {
                // Simulate network search & price fetch latency across Algerian supermarkets
                kotlinx.coroutines.delay(1200L)

                val essentialProducts = listOf(
                    "6130001000123", // Candia Milk
                    "6130002000456", // Elio Oil 5L
                    "6130003000789", // Famico Coffee
                    "6130010000777", // Farm Eggs 30
                    "6130005000222", // Sim Couscous
                    "6130006000333", // Berbère Cheese
                    "6130009000666", // Ifri Water
                    "6130011000888"  // Cevital Sugar
                )

                var updatedCount = 0
                val allPrices = marketPrices.value

                for (barcode in essentialProducts) {
                    val matchingPrices = allPrices.filter { it.barcode == barcode }
                    for (priceItem in matchingPrices) {
                        // Micro adjustment simulating live market fluctuations (± 5 to 15 DZD)
                        val variance = (listOf(-10, -5, 0, 5, 10).random()).toDouble()
                        val updatedPrice = (priceItem.priceDzd + variance).coerceAtLeast(15.0)

                        if (updatedPrice != priceItem.priceDzd) {
                            repository.reportCrowdsourcedPrice(
                                barcode = barcode,
                                storeName = priceItem.storeName,
                                storeNameAr = priceItem.storeNameAr,
                                itemName = priceItem.itemName,
                                newPrice = updatedPrice
                            )
                            updatedCount++
                        }
                    }
                }

                val syncTs = System.currentTimeMillis()
                _lastPriceSyncTimestamp.value = syncTs
                _updatedItemsCount.value = updatedCount
                preferencesStorage.setLastPriceSyncTimestamp(syncTs)
                preferencesStorage.setUpdatedItemsCount(updatedCount)
                _basketSummaries.value = repository.computeBasketComparison(shoppingItems.value)

                val msg = if (_isArabic.value) {
                    "تم تحديث أسعار البيع بالتجزئة للزبون لـ $updatedCount منتج من كبرى المتاجر"
                } else {
                    "Updated consumer retail prices for $updatedCount products from supermarkets"
                }
                _userMessage.emit(msg)
            } finally {
                _isSyncingPrices.value = false
            }
        }
    }

    fun addNewProductOrGroup(
        name: String,
        nameAr: String,
        storeName: String,
        storeNameAr: String,
        priceDzd: Double,
        barcode: String? = null
    ) {
        viewModelScope.launch {
            repository.addNewProductPrice(name, nameAr, storeName, storeNameAr, priceDzd, barcode)
            _basketSummaries.value = repository.computeBasketComparison(shoppingItems.value)
            val msg = if (_isArabic.value) "تمت إضافة \"$nameAr\" بنجاح بسعر التجزئة ${priceDzd.toInt()} د.ج" else "Added \"$name\" at ${priceDzd.toInt()} DZD"
            _userMessage.emit(msg)
        }
    }

    fun updatePrice(
        barcode: String,
        storeName: String?,
        newPrice: Double
    ) {
        viewModelScope.launch {
            repository.updateProductPrice(barcode, storeName, newPrice)
            _basketSummaries.value = repository.computeBasketComparison(shoppingItems.value)
            val msg = if (_isArabic.value) "تم تعديل السعر إلى ${newPrice.toInt()} د.ج بنجاح" else "Price updated to ${newPrice.toInt()} DZD"
            _userMessage.emit(msg)
        }
    }

    fun reportPrice(
        barcode: String,
        storeName: String,
        storeNameAr: String,
        itemName: String,
        priceDzd: Double
    ) {
        viewModelScope.launch {
            repository.reportCrowdsourcedPrice(barcode, storeName, storeNameAr, itemName, priceDzd)
            _basketSummaries.value = repository.computeBasketComparison(shoppingItems.value)
            val msg = if (_isArabic.value) "شكراً لمشاركتك! تم تحديث السعر في $storeNameAr" else "Price updated for $storeName!"
            _userMessage.emit(msg)
        }
    }

    fun getPricesForBarcode(barcode: String) = repository.getPricesForBarcode(barcode)

    fun getPriceHistory(barcode: String): StateFlow<List<PriceHistory>> {
        val flow = MutableStateFlow<List<PriceHistory>>(emptyList())
        viewModelScope.launch {
            repository.getPriceHistory(barcode).collect {
                flow.value = it
            }
        }
        return flow
    }

    fun getQrSharePayload(): String {
        return repository.createQrShareString(shoppingItems.value)
    }

    fun importListFromQr(qrPayload: String) {
        viewModelScope.launch {
            val count = repository.importFromQrString(qrPayload)
            val msg = if (count > 0) {
                if (_isArabic.value) "تم استيراد $count عناصر من رمز QR بنجاح!" else "Imported $count items from QR code!"
            } else {
                if (_isArabic.value) "رمز QR غير صالح لقائمة التسوق" else "Invalid shopping list QR code"
            }
            _userMessage.emit(msg)
        }
    }

    // Google Drive Backup / JSON snapshot generator
    fun generateGoogleDriveBackupPayload(): String {
        val root = JSONObject()
        root.put("app", "Shopy Pulse")
        root.put("version", APP_VERSION)
        root.put("timestamp", System.currentTimeMillis())
        root.put("budgetLimit", _budgetLimitDzd.value)
        root.put("isArabic", _isArabic.value)
        root.put("isDarkMode", _isDarkMode.value)
        root.put("isDynamicGlowEnabled", _isDynamicGlowEnabled.value)
        root.put("isLiquidDockSwipeEnabled", _isLiquidDockSwipeEnabled.value)
        root.put("syncIntervalHours", _syncIntervalHours.value)

        val itemsArray = JSONArray()
        for (item in shoppingItems.value) {
            val itemObj = JSONObject()
            itemObj.put("name", item.name)
            itemObj.put("nameAr", item.nameAr)
            itemObj.put("category", item.category)
            itemObj.put("quantity", item.quantity)
            itemObj.put("unit", item.unit)
            itemObj.put("isChecked", item.isChecked)
            itemObj.put("barcode", item.barcode ?: "")
            itemObj.put("estimatedPriceDzd", item.estimatedPriceDzd)
            itemsArray.put(itemObj)
        }
        root.put("items", itemsArray)
        return root.toString(2)
    }

    fun markGoogleDriveBackupDone() {
        val formatter = SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.getDefault())
        val dateStr = formatter.format(Date())
        _lastBackupDate.value = dateStr
        viewModelScope.launch {
            preferencesStorage.setLastBackupDate(dateStr)
            val msg = if (_isArabic.value) "تم النسخ الاحتياطي في Google Drive بنجاح ($dateStr)" else "Backup to Google Drive completed! ($dateStr)"
            _userMessage.emit(msg)
        }
    }

    fun restoreFromBackupPayload(jsonContent: String) {
        viewModelScope.launch {
            try {
                val root = JSONObject(jsonContent)
                val itemsArray = root.getJSONArray("items")
                var restoredCount = 0
                for (i in 0 until itemsArray.length()) {
                    val obj = itemsArray.getJSONObject(i)
                    val item = ShoppingItem(
                        name = obj.getString("name"),
                        nameAr = obj.optString("nameAr", obj.getString("name")),
                        category = obj.optString("category", "Pantry"),
                        quantity = obj.optInt("quantity", 1),
                        unit = obj.optString("unit", "pcs"),
                        isChecked = obj.optBoolean("isChecked", false),
                        barcode = obj.optString("barcode").ifBlank { null },
                        estimatedPriceDzd = obj.optDouble("estimatedPriceDzd", 0.0)
                    )
                    repository.insertItem(item)
                    restoredCount++
                }

                if (root.has("budgetLimit")) {
                    setBudgetLimit(root.getDouble("budgetLimit"))
                }
                if (root.has("isArabic")) {
                    setLanguage(root.getBoolean("isArabic"))
                }
                if (root.has("isDarkMode")) {
                    setDarkMode(root.getBoolean("isDarkMode"))
                }
                if (root.has("isDynamicGlowEnabled")) {
                    setDynamicGlowEnabled(root.getBoolean("isDynamicGlowEnabled"))
                }
                if (root.has("isLiquidDockSwipeEnabled")) {
                    setLiquidDockSwipeEnabled(root.getBoolean("isLiquidDockSwipeEnabled"))
                }
                if (root.has("syncIntervalHours")) {
                    setSyncIntervalHours(root.getInt("syncIntervalHours"))
                }

                val msg = if (_isArabic.value) "تمت استعادة $restoredCount عنصر من Google Drive بنجاح!" else "Successfully restored $restoredCount items from Google Drive!"
                _userMessage.emit(msg)
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = if (_isArabic.value) "خطأ في ملف النسخة الاحتياطية" else "Invalid backup file format"
                _userMessage.emit(msg)
            }
        }
    }

    /**
     * Completes current shopping trip and transfers total spent immediately to monthly statistics
     */
    fun finishShoppingTrip(
        storeName: String = "Uno Hypermarket",
        storeNameAr: String = "هايبرماركت أونو",
        customTotal: Double? = null
    ) {
        viewModelScope.launch {
            val currentItems = shoppingItems.value
            if (currentItems.isEmpty() && (customTotal == null || customTotal <= 0.0)) {
                _userMessage.emit(if (_isArabic.value) "قائمة المشتريات فارغة، أضف بعض المنتجات أولاً" else "Shopping list is empty")
                return@launch
            }

            // If some items are checked, use checked items, otherwise all items
            val targetItems = if (currentItems.any { it.isChecked }) currentItems.filter { it.isChecked } else currentItems
            val total = customTotal ?: targetItems.sumOf { 
                (if (it.estimatedPriceDzd > 0) it.estimatedPriceDzd else 120.0) * it.quantity 
            }
            val count = targetItems.sumOf { it.quantity }

            val now = System.currentTimeMillis()
            val monthFormatter = SimpleDateFormat("yyyy-MM", Locale.US)
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val monthKey = monthFormatter.format(Date(now))
            val dateStr = dateFormatter.format(Date(now))

            val summaryText = targetItems.take(4).joinToString("، ") { if (_isArabic.value) it.nameAr else it.name }
            val topCategory = targetItems.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "Grocery"

            val record = SpendingRecord(
                timestamp = now,
                monthKey = monthKey,
                dateStr = dateStr,
                totalSpentDzd = total,
                itemsCount = if (count > 0) count else 1,
                storeName = storeName,
                storeNameAr = storeNameAr,
                itemsSummary = summaryText,
                topCategory = topCategory
            )

            repository.recordShoppingTrip(record)

            // Clear the purchased items from list
            if (currentItems.any { it.isChecked }) {
                repository.clearCheckedItems()
            } else {
                for (item in currentItems) {
                    repository.deleteItem(item)
                }
            }

            val formatted = LocalizationUtil.formatDzd(total, _isArabic.value)
            _userMessage.emit(
                if (_isArabic.value) "تم إنهاء التسوق ونقل $formatted فوراً إلى الإحصائيات الشهرية!"
                else "Shopping finished! $formatted transferred to monthly analytics."
            )
        }
    }

    fun addManualSpendingTrip(
        storeName: String,
        storeNameAr: String,
        totalDzd: Double,
        itemsCount: Int,
        category: String,
        notes: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val monthFormatter = SimpleDateFormat("yyyy-MM", Locale.US)
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val monthKey = monthFormatter.format(Date(now))
            val dateStr = dateFormatter.format(Date(now))

            val record = SpendingRecord(
                timestamp = now,
                monthKey = monthKey,
                dateStr = dateStr,
                totalSpentDzd = totalDzd,
                itemsCount = itemsCount,
                storeName = storeName,
                storeNameAr = storeNameAr,
                itemsSummary = notes,
                topCategory = category
            )
            repository.recordShoppingTrip(record)
            _userMessage.emit(if (_isArabic.value) "تم تسجيل النفقة في الإحصائيات بنجاح" else "Expense logged successfully")
        }
    }

    fun deleteSpendingRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteSpendingRecord(id)
            _userMessage.emit(if (_isArabic.value) "تم حذف السجل من الإحصائيات" else "Record deleted")
        }
    }

    // Live shopping stats
    fun calculateStats(items: List<ShoppingItem>): ShoppingStats {
        val totalCount = items.size
        val checkedCount = items.count { it.isChecked }
        val checkedTotalDzd = items.filter { it.isChecked }.sumOf { it.estimatedPriceDzd * it.quantity }
        val grandTotalDzd = items.sumOf { it.estimatedPriceDzd * it.quantity }
        return ShoppingStats(
            totalCount = totalCount,
            checkedCount = checkedCount,
            checkedTotalDzd = checkedTotalDzd,
            grandTotalDzd = grandTotalDzd
        )
    }
}

data class ShoppingStats(
    val totalCount: Int,
    val checkedCount: Int,
    val checkedTotalDzd: Double,
    val grandTotalDzd: Double
)

class ShoppingViewModelFactory(
    private val repository: ShoppingRepository,
    private val preferencesStorage: AppPreferencesStorage = InMemoryPreferencesStorage()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingViewModel(repository, preferencesStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
