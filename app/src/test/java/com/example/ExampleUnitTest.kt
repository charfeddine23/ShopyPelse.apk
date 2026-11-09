package com.example

import com.example.data.model.ShoppingItem
import com.example.util.LocalizationUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormatting() {
        val formattedAr = LocalizationUtil.formatDzd(1250.0, isArabic = true)
        val formattedEn = LocalizationUtil.formatDzd(1250.0, isArabic = false)

        assertTrue(formattedAr.contains("1,250") && formattedAr.contains("د.ج"))
        assertTrue(formattedEn.contains("1,250") && formattedEn.contains("DA"))
    }

    @Test
    fun testCategoryLocalization() {
        assertEquals("خضر وفواكه", LocalizationUtil.getCategoryLabel("Produce", isArabic = true))
        assertEquals("ألبان وأجبان", LocalizationUtil.getCategoryLabel("Dairy", isArabic = true))
        assertEquals("Produce", LocalizationUtil.getCategoryLabel("Produce", isArabic = false))
    }

    @Test
    fun testQrSerializationAndDeserialization() {
        val originalItem = ShoppingItem(
            name = "Elio Cooking Oil 5L",
            nameAr = "زيت المائدة إيليو 5 لتر",
            category = "Pantry",
            quantity = 2,
            unit = "pcs",
            barcode = "6130002000456",
            estimatedPriceDzd = 620.0
        )
        val payload = "SOUK|${originalItem.name};${originalItem.nameAr};${originalItem.category};${originalItem.quantity};${originalItem.unit};${originalItem.barcode};${originalItem.estimatedPriceDzd}"

        assertTrue(payload.startsWith("SOUK|"))
        val itemsData = payload.removePrefix("SOUK|").split(";")
        assertEquals("Elio Cooking Oil 5L", itemsData[0])
        assertEquals("زيت المائدة إيليو 5 لتر", itemsData[1])
        assertEquals("Pantry", itemsData[2])
        assertEquals(2, itemsData[3].toInt())
    }

    @Test
    fun testDeveloperAndAppInfo() {
        assertEquals("Developed by Kahlessenane Charfeddine", com.example.ui.viewmodel.ShoppingViewModel.DEVELOPER_NAME)
        assertEquals("v1.0 Build 1", com.example.ui.viewmodel.ShoppingViewModel.APP_VERSION)
    }
}
