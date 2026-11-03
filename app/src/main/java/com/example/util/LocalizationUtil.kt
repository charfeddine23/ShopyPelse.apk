package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object LocalizationUtil {

    private val numberFormatter = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))

    fun formatDzd(amount: Double, isArabic: Boolean = true): String {
        val formatted = numberFormatter.format(amount)
        return if (isArabic) {
            "$formatted د.ج"
        } else {
            "$formatted DA"
        }
    }

    fun getCategoryLabel(category: String, isArabic: Boolean = true): String {
        return if (isArabic) {
            when (category) {
                "Produce" -> "خضر وفواكه"
                "Dairy" -> "ألبان وأجبان"
                "Bakery" -> "مخبوزات"
                "Pantry" -> "مواد غذائية"
                "Butchery" -> "لحوم ودواجن"
                "Beverages" -> "مشروبات"
                "Household" -> "مواد تنظيف"
                else -> "أخرى"
            }
        } else {
            when (category) {
                "Produce" -> "Produce"
                "Dairy" -> "Dairy"
                "Bakery" -> "Bakery"
                "Pantry" -> "Pantry"
                "Butchery" -> "Butchery"
                "Beverages" -> "Beverages"
                "Household" -> "Household"
                else -> "Other"
            }
        }
    }

    val allCategories = listOf(
        "All",
        "Produce",
        "Dairy",
        "Bakery",
        "Pantry",
        "Butchery",
        "Beverages",
        "Household"
    )
}
