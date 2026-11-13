package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.util.LocaleHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private val Context.shopyPulseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "shopy_pulse_preferences"
)

data class AppPreferencesState(
    val isArabic: Boolean = true,
    val isDarkMode: Boolean = true,
    val isDynamicGlowEnabled: Boolean = true,
    val isLiquidDockSwipeEnabled: Boolean = true,
    val budgetLimitDzd: Double = 3500.0,
    val lastBackupDate: String = "",
    val syncIntervalHours: Int = 12,
    val lastPriceSyncTimestamp: Long = 0L,
    val updatedItemsCount: Int = 0,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val currentScreen: String = "HOME",
    val selectedStatsMonth: String = "",
    val hasSeededSpending: Boolean = false,
    val hasSeededCatalog: Boolean = false,
    val lastUpdateCheckTimestamp: Long = 0L,
    val latestKnownVersion: String = ""
)

interface AppPreferencesStorage {
    val preferencesFlow: Flow<AppPreferencesState>
    fun getInitialState(): AppPreferencesState
    suspend fun setArabic(isArabic: Boolean)
    suspend fun setDarkMode(isDarkMode: Boolean)
    suspend fun setDynamicGlowEnabled(enabled: Boolean)
    suspend fun setLiquidDockSwipeEnabled(enabled: Boolean)
    suspend fun setBudgetLimitDzd(limit: Double)
    suspend fun setLastBackupDate(date: String)
    suspend fun setSyncIntervalHours(hours: Int)
    suspend fun setLastPriceSyncTimestamp(timestamp: Long)
    suspend fun setUpdatedItemsCount(count: Int)
    suspend fun setSelectedCategory(category: String)
    suspend fun setSearchQuery(query: String)
    suspend fun setCurrentScreen(screenName: String)
    suspend fun setSelectedStatsMonth(month: String)
    suspend fun setHasSeededSpending(seeded: Boolean)
    suspend fun setHasSeededCatalog(seeded: Boolean)
    suspend fun setLastUpdateCheckTimestamp(timestamp: Long)
    suspend fun setLatestKnownVersion(version: String)
}

/**
 * Persistent preferences repository backed by Jetpack DataStore Preferences
 * with synchronous SharedPreferences bootstrap cache & LocaleHelper sync so
 * every setting (language, theme, toggles, budget, filters, active tab, etc.)
 * loads immediately on frame 0 with zero flicker and survives app kills/restarts.
 */
class UserPreferencesRepository(context: Context) : AppPreferencesStorage {

    private val appContext = context.applicationContext
    private val dataStore = appContext.shopyPulseDataStore
    private val cachePrefs: SharedPreferences =
        appContext.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE)

    private object Keys {
        val IS_ARABIC = booleanPreferencesKey("is_arabic")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_DYNAMIC_GLOW = booleanPreferencesKey("is_dynamic_glow_enabled")
        val IS_LIQUID_DOCK_SWIPE = booleanPreferencesKey("is_liquid_dock_swipe_enabled")
        val BUDGET_LIMIT_DZD = doublePreferencesKey("budget_limit_dzd")
        val LAST_BACKUP_DATE = stringPreferencesKey("last_backup_date")
        val SYNC_INTERVAL_HOURS = intPreferencesKey("sync_interval_hours")
        val LAST_PRICE_SYNC_TS = longPreferencesKey("last_price_sync_timestamp")
        val UPDATED_ITEMS_COUNT = intPreferencesKey("updated_items_count")
        val SELECTED_CATEGORY = stringPreferencesKey("selected_category")
        val SEARCH_QUERY = stringPreferencesKey("search_query")
        val CURRENT_SCREEN = stringPreferencesKey("current_screen")
        val SELECTED_STATS_MONTH = stringPreferencesKey("selected_stats_month")
        val HAS_SEEDED_SPENDING = booleanPreferencesKey("has_seeded_spending")
        val HAS_SEEDED_CATALOG = booleanPreferencesKey("has_seeded_catalog")
        val LAST_UPDATE_CHECK_TS = longPreferencesKey("last_update_check_timestamp")
        val LATEST_KNOWN_VERSION = stringPreferencesKey("latest_known_version")
    }

    override val preferencesFlow: Flow<AppPreferencesState> = dataStore.data.map { prefs ->
        val fallback = getInitialState()
        AppPreferencesState(
            isArabic = prefs[Keys.IS_ARABIC] ?: fallback.isArabic,
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: fallback.isDarkMode,
            isDynamicGlowEnabled = prefs[Keys.IS_DYNAMIC_GLOW] ?: fallback.isDynamicGlowEnabled,
            isLiquidDockSwipeEnabled = prefs[Keys.IS_LIQUID_DOCK_SWIPE] ?: fallback.isLiquidDockSwipeEnabled,
            budgetLimitDzd = prefs[Keys.BUDGET_LIMIT_DZD] ?: fallback.budgetLimitDzd,
            lastBackupDate = prefs[Keys.LAST_BACKUP_DATE] ?: fallback.lastBackupDate,
            syncIntervalHours = prefs[Keys.SYNC_INTERVAL_HOURS] ?: fallback.syncIntervalHours,
            lastPriceSyncTimestamp = prefs[Keys.LAST_PRICE_SYNC_TS] ?: fallback.lastPriceSyncTimestamp,
            updatedItemsCount = prefs[Keys.UPDATED_ITEMS_COUNT] ?: fallback.updatedItemsCount,
            selectedCategory = prefs[Keys.SELECTED_CATEGORY] ?: fallback.selectedCategory,
            searchQuery = prefs[Keys.SEARCH_QUERY] ?: fallback.searchQuery,
            currentScreen = prefs[Keys.CURRENT_SCREEN] ?: fallback.currentScreen,
            selectedStatsMonth = prefs[Keys.SELECTED_STATS_MONTH] ?: fallback.selectedStatsMonth,
            hasSeededSpending = prefs[Keys.HAS_SEEDED_SPENDING] ?: fallback.hasSeededSpending,
            hasSeededCatalog = prefs[Keys.HAS_SEEDED_CATALOG] ?: fallback.hasSeededCatalog,
            lastUpdateCheckTimestamp = prefs[Keys.LAST_UPDATE_CHECK_TS] ?: fallback.lastUpdateCheckTimestamp,
            latestKnownVersion = prefs[Keys.LATEST_KNOWN_VERSION] ?: fallback.latestKnownVersion
        )
    }

    override fun getInitialState(): AppPreferencesState {
        val defaultArabic = LocaleHelper.isArabic(appContext)
        val initialSyncTs = if (cachePrefs.contains("last_price_sync_timestamp")) {
            cachePrefs.getLong("last_price_sync_timestamp", 0L)
        } else {
            0L
        }
        return AppPreferencesState(
            isArabic = if (cachePrefs.contains("is_arabic")) {
                cachePrefs.getBoolean("is_arabic", defaultArabic)
            } else {
                defaultArabic
            },
            isDarkMode = cachePrefs.getBoolean("is_dark_mode", true),
            isDynamicGlowEnabled = cachePrefs.getBoolean("is_dynamic_glow_enabled", true),
            isLiquidDockSwipeEnabled = cachePrefs.getBoolean("is_liquid_dock_swipe_enabled", true),
            budgetLimitDzd = java.lang.Double.longBitsToDouble(
                cachePrefs.getLong(
                    "budget_limit_dzd_bits",
                    java.lang.Double.doubleToRawLongBits(3500.0)
                )
            ),
            lastBackupDate = cachePrefs.getString("last_backup_date", "") ?: "",
            syncIntervalHours = cachePrefs.getInt("sync_interval_hours", 12),
            lastPriceSyncTimestamp = initialSyncTs,
            updatedItemsCount = cachePrefs.getInt("updated_items_count", 0),
            selectedCategory = cachePrefs.getString("selected_category", "All") ?: "All",
            searchQuery = cachePrefs.getString("search_query", "") ?: "",
            currentScreen = cachePrefs.getString("current_screen", "HOME") ?: "HOME",
            selectedStatsMonth = cachePrefs.getString("selected_stats_month", "") ?: "",
            hasSeededSpending = cachePrefs.getBoolean("has_seeded_spending", false),
            hasSeededCatalog = cachePrefs.getBoolean("has_seeded_catalog", false),
            lastUpdateCheckTimestamp = cachePrefs.getLong("last_update_check_timestamp", 0L),
            latestKnownVersion = cachePrefs.getString("latest_known_version", "") ?: ""
        )
    }

    override suspend fun setArabic(isArabic: Boolean) {
        val langCode = if (isArabic) LocaleHelper.LANG_ARABIC else LocaleHelper.LANG_ENGLISH
        LocaleHelper.setLocale(appContext, langCode)
        cachePrefs.edit().putBoolean("is_arabic", isArabic).commit()
        dataStore.edit { it[Keys.IS_ARABIC] = isArabic }
    }

    override suspend fun setDarkMode(isDarkMode: Boolean) {
        cachePrefs.edit().putBoolean("is_dark_mode", isDarkMode).apply()
        dataStore.edit { it[Keys.IS_DARK_MODE] = isDarkMode }
    }

    override suspend fun setDynamicGlowEnabled(enabled: Boolean) {
        cachePrefs.edit().putBoolean("is_dynamic_glow_enabled", enabled).apply()
        dataStore.edit { it[Keys.IS_DYNAMIC_GLOW] = enabled }
    }

    override suspend fun setLiquidDockSwipeEnabled(enabled: Boolean) {
        cachePrefs.edit().putBoolean("is_liquid_dock_swipe_enabled", enabled).apply()
        dataStore.edit { it[Keys.IS_LIQUID_DOCK_SWIPE] = enabled }
    }

    override suspend fun setBudgetLimitDzd(limit: Double) {
        cachePrefs.edit()
            .putLong("budget_limit_dzd_bits", java.lang.Double.doubleToRawLongBits(limit))
            .apply()
        dataStore.edit { it[Keys.BUDGET_LIMIT_DZD] = limit }
    }

    override suspend fun setLastBackupDate(date: String) {
        cachePrefs.edit().putString("last_backup_date", date).apply()
        dataStore.edit { it[Keys.LAST_BACKUP_DATE] = date }
    }

    override suspend fun setSyncIntervalHours(hours: Int) {
        cachePrefs.edit().putInt("sync_interval_hours", hours).apply()
        dataStore.edit { it[Keys.SYNC_INTERVAL_HOURS] = hours }
    }

    override suspend fun setLastPriceSyncTimestamp(timestamp: Long) {
        cachePrefs.edit().putLong("last_price_sync_timestamp", timestamp).apply()
        dataStore.edit { it[Keys.LAST_PRICE_SYNC_TS] = timestamp }
    }

    override suspend fun setUpdatedItemsCount(count: Int) {
        cachePrefs.edit().putInt("updated_items_count", count).apply()
        dataStore.edit { it[Keys.UPDATED_ITEMS_COUNT] = count }
    }

    override suspend fun setSelectedCategory(category: String) {
        cachePrefs.edit().putString("selected_category", category).apply()
        dataStore.edit { it[Keys.SELECTED_CATEGORY] = category }
    }

    override suspend fun setSearchQuery(query: String) {
        cachePrefs.edit().putString("search_query", query).apply()
        dataStore.edit { it[Keys.SEARCH_QUERY] = query }
    }

    override suspend fun setCurrentScreen(screenName: String) {
        cachePrefs.edit().putString("current_screen", screenName).apply()
        dataStore.edit { it[Keys.CURRENT_SCREEN] = screenName }
    }

    override suspend fun setSelectedStatsMonth(month: String) {
        cachePrefs.edit().putString("selected_stats_month", month).apply()
        dataStore.edit { it[Keys.SELECTED_STATS_MONTH] = month }
    }

    override suspend fun setHasSeededSpending(seeded: Boolean) {
        cachePrefs.edit().putBoolean("has_seeded_spending", seeded).apply()
        dataStore.edit { it[Keys.HAS_SEEDED_SPENDING] = seeded }
    }

    override suspend fun setHasSeededCatalog(seeded: Boolean) {
        cachePrefs.edit().putBoolean("has_seeded_catalog", seeded).apply()
        dataStore.edit { it[Keys.HAS_SEEDED_CATALOG] = seeded }
    }

    override suspend fun setLastUpdateCheckTimestamp(timestamp: Long) {
        cachePrefs.edit().putLong("last_update_check_timestamp", timestamp).apply()
        dataStore.edit { it[Keys.LAST_UPDATE_CHECK_TS] = timestamp }
    }

    override suspend fun setLatestKnownVersion(version: String) {
        cachePrefs.edit().putString("latest_known_version", version).apply()
        dataStore.edit { it[Keys.LATEST_KNOWN_VERSION] = version }
    }

    companion object {
        private const val CACHE_PREFS_NAME = "shopy_pulse_prefs_cache"
    }
}

/**
 * In-memory fallback storage used only when no Context-backed storage is passed (e.g. pure unit tests).
 */
class InMemoryPreferencesStorage(
    initial: AppPreferencesState = AppPreferencesState()
) : AppPreferencesStorage {
    private val state = MutableStateFlow(initial)
    override val preferencesFlow: Flow<AppPreferencesState> = state.asStateFlow()
    override fun getInitialState(): AppPreferencesState = state.value
    override suspend fun setArabic(isArabic: Boolean) { state.value = state.value.copy(isArabic = isArabic) }
    override suspend fun setDarkMode(isDarkMode: Boolean) { state.value = state.value.copy(isDarkMode = isDarkMode) }
    override suspend fun setDynamicGlowEnabled(enabled: Boolean) { state.value = state.value.copy(isDynamicGlowEnabled = enabled) }
    override suspend fun setLiquidDockSwipeEnabled(enabled: Boolean) { state.value = state.value.copy(isLiquidDockSwipeEnabled = enabled) }
    override suspend fun setBudgetLimitDzd(limit: Double) { state.value = state.value.copy(budgetLimitDzd = limit) }
    override suspend fun setLastBackupDate(date: String) { state.value = state.value.copy(lastBackupDate = date) }
    override suspend fun setSyncIntervalHours(hours: Int) { state.value = state.value.copy(syncIntervalHours = hours) }
    override suspend fun setLastPriceSyncTimestamp(timestamp: Long) { state.value = state.value.copy(lastPriceSyncTimestamp = timestamp) }
    override suspend fun setUpdatedItemsCount(count: Int) { state.value = state.value.copy(updatedItemsCount = count) }
    override suspend fun setSelectedCategory(category: String) { state.value = state.value.copy(selectedCategory = category) }
    override suspend fun setSearchQuery(query: String) { state.value = state.value.copy(searchQuery = query) }
    override suspend fun setCurrentScreen(screenName: String) { state.value = state.value.copy(currentScreen = screenName) }
    override suspend fun setSelectedStatsMonth(month: String) { state.value = state.value.copy(selectedStatsMonth = month) }
    override suspend fun setHasSeededSpending(seeded: Boolean) { state.value = state.value.copy(hasSeededSpending = seeded) }
    override suspend fun setHasSeededCatalog(seeded: Boolean) { state.value = state.value.copy(hasSeededCatalog = seeded) }
    override suspend fun setLastUpdateCheckTimestamp(timestamp: Long) { state.value = state.value.copy(lastUpdateCheckTimestamp = timestamp) }
    override suspend fun setLatestKnownVersion(version: String) { state.value = state.value.copy(latestKnownVersion = version) }
}
