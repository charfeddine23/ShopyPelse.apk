package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.util.LocaleHelper

/**
 * Persistent preferences manager for Shopy Pulse.
 * Safely persists and restores user configuration across app sessions:
 * - Selected language (Arabic vs English)
 * - Theme mode (Dark vs Light)
 * - Dynamic App Title Glow effect toggle
 * - Liquid Glass Dock Swipe gesture navigation toggle
 * - Monthly/Weekly budget limit (DZD)
 * - Price sync interval and last sync timestamp
 * - Cloud backup timestamp
 * - Active selected category filter
 */
class AppPreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "shopypulse_user_preferences"
        private const val KEY_IS_ARABIC = "is_arabic"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_IS_DYNAMIC_GLOW_ENABLED = "is_dynamic_glow_enabled"
        private const val KEY_IS_LIQUID_DOCK_SWIPE_ENABLED = "is_liquid_dock_swipe_enabled"
        private const val KEY_BUDGET_LIMIT_DZD = "budget_limit_dzd"
        private const val KEY_SYNC_INTERVAL_HOURS = "sync_interval_hours"
        private const val KEY_LAST_BACKUP_DATE = "last_backup_date"
        private const val KEY_SELECTED_CATEGORY = "selected_category"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
    }

    var isArabic: Boolean
        get() {
            return if (prefs.contains(KEY_IS_ARABIC)) {
                prefs.getBoolean(KEY_IS_ARABIC, true)
            } else {
                LocaleHelper.isArabic(context)
            }
        }
        set(value) {
            prefs.edit().putBoolean(KEY_IS_ARABIC, value).apply()
            LocaleHelper.persistLanguage(
                context,
                if (value) LocaleHelper.LANG_ARABIC else LocaleHelper.LANG_ENGLISH
            )
        }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_MODE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_DARK_MODE, value).apply()
        }

    var isDynamicGlowEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_DYNAMIC_GLOW_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_DYNAMIC_GLOW_ENABLED, value).apply()
        }

    var isLiquidDockSwipeEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_LIQUID_DOCK_SWIPE_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_LIQUID_DOCK_SWIPE_ENABLED, value).apply()
        }

    var budgetLimitDzd: Double
        get() = prefs.getFloat(KEY_BUDGET_LIMIT_DZD, 3500.0f).toDouble()
        set(value) {
            prefs.edit().putFloat(KEY_BUDGET_LIMIT_DZD, value.toFloat()).apply()
        }

    var syncIntervalHours: Int
        get() = prefs.getInt(KEY_SYNC_INTERVAL_HOURS, 12)
        set(value) {
            prefs.edit().putInt(KEY_SYNC_INTERVAL_HOURS, value).apply()
        }

    var lastBackupDate: String
        get() = prefs.getString(KEY_LAST_BACKUP_DATE, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_LAST_BACKUP_DATE, value).apply()
        }

    var selectedCategory: String
        get() = prefs.getString(KEY_SELECTED_CATEGORY, "All") ?: "All"
        set(value) {
            prefs.edit().putString(KEY_SELECTED_CATEGORY, value).apply()
        }

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, System.currentTimeMillis())
        set(value) {
            prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, value).apply()
        }
}
