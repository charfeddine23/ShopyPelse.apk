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
     * Synchronizes decorView layout direction with the persisted language (RTL for Arabic, LTR for English).
     */
    fun syncDecorViewLayoutDirection() {
        window.decorView.layoutDirection = if (LocaleHelper.isArabic(this)) {
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
