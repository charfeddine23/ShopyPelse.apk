package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.view.View
import java.util.Locale

/**
 * Production-ready Locale & LayoutDirection Helper for Shopy Pulse.
 * Handles instant locale switching, resource configuration updates,
 * and layout mirroring (RTL for Arabic vs LTR for English).
 */
object LocaleHelper {

    private const val PREFS_NAME = "shopy_pulse_locale_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    const val LANG_ARABIC = "ar"
    const val LANG_ENGLISH = "en"

    /**
     * Call this in Activity.attachBaseContext() to wrap with the stored locale.
     */
    fun onAttach(context: Context): ContextWrapper {
        val lang = getPersistedLanguage(context, defaultLanguage = LANG_ARABIC)
        return setLocale(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedLanguage(context, defaultLanguage = LANG_ARABIC)
    }

    fun isArabic(context: Context): Boolean {
        return getLanguage(context) == LANG_ARABIC
    }

    /**
     * Updates Configuration, Locales, and LayoutDirection, persisting choice.
     */
    fun setLocale(context: Context, languageCode: String): ContextWrapper {
        persistLanguage(context, languageCode)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            config.setLayoutDirection(locale)
            ContextWrapper(context.createConfigurationContext(config))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            config.setLayoutDirection(locale)
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)
            ContextWrapper(context)
        }
    }

    /**
     * Dynamically forces or resets layout direction on any specific View or decorView.
     */
    fun applyLayoutDirection(view: View, languageCode: String) {
        if (languageCode == LANG_ARABIC) {
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    /**
     * Forces LTR layout direction regardless of current system/app locale.
     */
    fun forceLtr(view: View) {
        view.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }

    /**
     * Forces RTL layout direction regardless of current system/app locale.
     */
    fun forceRtl(view: View) {
        view.layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    private fun getPersistedLanguage(context: Context, defaultLanguage: String): String {
        val preferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getString(KEY_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    fun persistLanguage(context: Context, language: String) {
        val preferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit().putString(KEY_LANGUAGE, language).commit()
    }
}
