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
     * Keeps decorView layout direction fixed to LTR so UI elements do not mirror on language changes.
     */
    fun syncDecorViewLayoutDirection() {
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }

    /**
     * Changes language dynamically and recreates Activity smoothly.
     */
    fun changeLanguage(languageCode: String) {
        LocaleHelper.setLocale(this, languageCode)
        recreate()
    }
}
