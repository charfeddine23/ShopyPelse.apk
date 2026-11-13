package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.UserPreferencesRepository
import com.example.util.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Shopy Pulse", appName)
  }

  @Test
  fun `language and all app preferences persist across restarts`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs1 = UserPreferencesRepository(context)

    prefs1.setArabic(false)
    prefs1.setDarkMode(false)
    prefs1.setDynamicGlowEnabled(false)
    prefs1.setLiquidDockSwipeEnabled(false)
    prefs1.setBudgetLimitDzd(8200.0)
    prefs1.setLastBackupDate("13 November 2026 - 10:00")
    prefs1.setSyncIntervalHours(24)
    prefs1.setLastPriceSyncTimestamp(1700000000000L)
    prefs1.setUpdatedItemsCount(15)
    prefs1.setSelectedCategory("Dairy")
    prefs1.setSearchQuery("Candia")
    prefs1.setCurrentScreen("SETTINGS")
    prefs1.setSelectedStatsMonth("2026-10")
    prefs1.setHasSeededSpending(true)
    prefs1.setHasSeededCatalog(true)

    // Simulate fresh app launch with new repository instance & LocaleHelper check
    assertEquals(LocaleHelper.LANG_ENGLISH, LocaleHelper.getLanguage(context))
    assertFalse(LocaleHelper.isArabic(context))

    val prefs2 = UserPreferencesRepository(context)
    val syncState = prefs2.getInitialState()
    assertFalse(syncState.isArabic)
    assertFalse(syncState.isDarkMode)
    assertFalse(syncState.isDynamicGlowEnabled)
    assertFalse(syncState.isLiquidDockSwipeEnabled)
    assertEquals(8200.0, syncState.budgetLimitDzd, 0.01)
    assertEquals("13 November 2026 - 10:00", syncState.lastBackupDate)
    assertEquals(24, syncState.syncIntervalHours)
    assertEquals(1700000000000L, syncState.lastPriceSyncTimestamp)
    assertEquals(15, syncState.updatedItemsCount)
    assertEquals("Dairy", syncState.selectedCategory)
    assertEquals("Candia", syncState.searchQuery)
    assertEquals("SETTINGS", syncState.currentScreen)
    assertEquals("2026-10", syncState.selectedStatsMonth)
    assertTrue(syncState.hasSeededSpending)
    assertTrue(syncState.hasSeededCatalog)

    // Verify DataStore flow itself also matches
    val dsState = prefs2.preferencesFlow.first()
    assertFalse(dsState.isArabic)
    assertFalse(dsState.isDarkMode)
    assertEquals(8200.0, dsState.budgetLimitDzd, 0.01)
    assertEquals("SETTINGS", dsState.currentScreen)
  }

  @Test
  fun `fluid water physics propagates waves and settles cleanly under viscous friction`() {
    val waterState = com.example.ui.components.FluidWaterState(densityPx = 3f)
    assertFalse(waterState.isSimulating)
    assertFalse(waterState.hasActiveDisturbance)

    // 1. Touch down spawns initial capillary wave
    val pointerId = androidx.compose.ui.input.pointer.PointerId(0L)
    var nowNanos = 1_000_000_000L
    waterState.onPointerDown(
      pointerId = pointerId,
      position = androidx.compose.ui.geometry.Offset(200f, 400f),
      eventTimeNanos = nowNanos
    )
    assertTrue(waterState.isSimulating)
    assertTrue(waterState.hasActiveDisturbance)

    // 2. Drag horizontally and vertically to build fluid wake momentum
    nowNanos += 50_000_000L
    waterState.onPointerMove(
      pointerId = pointerId,
      position = androidx.compose.ui.geometry.Offset(380f, 520f),
      eventTimeNanos = nowNanos
    )
    assertTrue(waterState.wakeEnergy > 0.2f)

    // 3. Release finger and simulate 2.5 seconds at 60 FPS
    waterState.onPointerUp(
      pointerId = pointerId,
      position = androidx.compose.ui.geometry.Offset(420f, 540f)
    )
    repeat(150) {
      waterState.stepPhysics(1f / 60f)
    }

    // After waves propagate and attenuate via fluid friction, engine rests at 0% idle cost
    assertFalse(waterState.isSimulating)
    assertFalse(waterState.hasActiveDisturbance)
  }
}
