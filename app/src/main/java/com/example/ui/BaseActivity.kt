package com.example.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.example.util.LocaleHelper

/**
 * BaseActivity that attaches the localized ContextWrapper,
 * synchronizes decorView layoutDirection (RTL for Arabic, LTR for English),
 * and provides smooth language switching without app crashes.
 */
abstract class BaseActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        syncDecorViewLayoutDirection()
    }

    override fun onResume() {
        super.onResume()
        syncDecorViewLayoutDirection()
    }

    /**
     * Programmatically forces the decorView layout direction to RTL for Arabic
     * or LTR for English/other languages.
     */
    fun syncDecorViewLayoutDirection() {
        val currentLang = LocaleHelper.getLanguage(this)
        window.decorView.layoutDirection = if (currentLang == LocaleHelper.LANG_ARABIC) {
            View.LAYOUT_DIRECTION_RTL
        } else {
            View.LAYOUT_DIRECTION_LTR
        }
    }

    /**
     * Changes language dynamically and recreates Activity smoothly.
     */
    fun changeLanguage(languageCode: String) {
        LocaleHelper.setLocale(this, languageCode)
        recreate()
    }
}
