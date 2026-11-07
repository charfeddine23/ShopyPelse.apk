package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MarketPrice
import com.example.data.model.StoreBasketSummary
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CyanTintBg
import com.example.ui.theme.CyanTintBorder
import com.example.ui.theme.CyanTintText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.LightCardSurface
import com.example.ui.theme.MintTintBg
import com.example.ui.theme.MintTintBorder
import com.example.ui.theme.MintTintText
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.util.LocalizationUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PriceComparisonScreen(
    viewModel: ShoppingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val allPrices by viewModel.marketPrices.collectAsStateWithLifecycle()
    val basketSummaries by viewModel.basketSummaries.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val syncIntervalHours by viewModel.syncIntervalHours.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastPriceSyncTimestamp.collectAsStateWithLifecycle()
    val isSyncingPrices by viewModel.isSyncingPrices.collectAsStateWithLifecycle()
    val updatedCount by viewModel.updatedItemsCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    var reportItemDialog by remember { mutableStateOf<MarketPrice?>(null) }

    val filteredPrices = remember(allPrices, searchQuery, selectedCategory) {
        allPrices.filter { price ->
            val matchesCategory = if (selectedCategory == "All") true else viewModel.autoCategorize(price.itemName, price.itemNameAr) == selectedCategory
            val matchesSearch = if (searchQuery.isBlank()) true else {
                price.itemName.contains(searchQuery, ignoreCase = true) ||
                price.itemNameAr.contains(searchQuery, ignoreCase = true) ||
                price.barcode.contains(searchQuery)
            }
            matchesCategory && matchesSearch
        }
    }
    val groupedProducts = remember(filteredPrices) {
        filteredPrices.groupBy { it.barcode }
    }

    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val pullThreshold = with(density) { 72.dp.toPx() }
    val maxPullPx = with(density) { 150.dp.toPx() }

    var pullOffset by remember { mutableFloatStateOf(0f) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isPastThreshold by remember { mutableStateOf(false) }
    var showUpdateSettingsDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 && pullOffset > 0f) {
                    val consumed = available.y.coerceAtLeast(-pullOffset)
                    pullOffset = (pullOffset + consumed).coerceAtLeast(0f)
                    if (pullOffset < pullThreshold) {
                        isPastThreshold = false
                        timerJob?.cancel()
                        timerJob = null
                        holdProgress = 0f
                    }
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0 && source == NestedScrollSource.UserInput) {
                    // Realistic rubber-band drag dampening: higher pull gives organic resistance
                    val currentPull = pullOffset
                    val elasticResistance = (1f - (currentPull / (maxPullPx * 1.35f)).coerceIn(0f, 0.85f)) * 0.60f
                    val newOffset = (currentPull + available.y * elasticResistance).coerceAtMost(maxPullPx)
                    pullOffset = newOffset

                    if (pullOffset >= pullThreshold && !isPastThreshold && !showUpdateSettingsDialog) {
                        isPastThreshold = true
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}

                        timerJob?.cancel()
                        timerJob = coroutineScope.launch {
                            val start = System.currentTimeMillis()
                            var lastHapticMilestone = 0L
                            while (isActive) {
                                val elapsed = System.currentTimeMillis() - start
                                holdProgress = (elapsed / 1000f).coerceIn(0f, 1f)

                                // Gentle rhythmic haptic pulses during countdown
                                if (elapsed - lastHapticMilestone >= 250L && elapsed < 900L) {
                                    lastHapticMilestone = elapsed
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } catch (_: Exception) {}
                                }

                                if (elapsed >= 1000L) {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } catch (_: Exception) {}
                                    showUpdateSettingsDialog = true
                                    holdProgress = 0f
                                    isPastThreshold = false
                                    animate(
                                        initialValue = pullOffset,
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ) { v, _ -> pullOffset = v }
                                    timerJob = null
                                    break
                                }
                                delay(16L)
                            }
                        }
                    }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val wasPast = isPastThreshold
                val openedSettings = showUpdateSettingsDialog
                timerJob?.cancel()
                timerJob = null
                val progress = holdProgress
                holdProgress = 0f
                isPastThreshold = false

                if (wasPast && !openedSettings && progress < 1f) {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {}
                    viewModel.refreshEssentialPrices()
                }

                // Ultra-smooth physical spring-back
                animate(
                    initialValue = pullOffset,
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) { v, _ -> pullOffset = v }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Dynamic Glowing Animated Background Bloom
        DynamicPullGlowingBackground(
            pullOffset = pullOffset,
            threshold = pullThreshold,
            holdProgress = holdProgress,
            isSyncing = isSyncingPrices,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Floating Pull-To-Refresh / Hold-for-Settings Indicator
        if (pullOffset > 10f || isSyncingPrices) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                PullIndicatorPill(
                    pullOffset = pullOffset,
                    threshold = pullThreshold,
                    holdProgress = holdProgress,
                    isSyncing = isSyncingPrices,
                    isArabic = isArabic
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, pullOffset.toInt().coerceAtMost(200)) },
            contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
                    val backBtnBg = if (isLight) Color.White else Color(0x33FFFFFF)
                    val backBtnBorder = if (isLight) BorderStroke(1.dp, LightCardBorder) else BorderStroke(1.dp, Color(0x20FFFFFF))

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(backBtnBg)
                            .border(backBtnBorder, CircleShape)
                            .size(40.dp)
                            .testTag("price_comparison_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isLight) CyanTintText else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isArabic) "المنتجات" else "Products",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "قائمة المنتجات والأسعار المباشرة في الجزائر" else "Algerian Products & Direct Supermarket Prices",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Search Bar
            item {
                val isLight = MaterialTheme.colorScheme.background == BackgroundLight
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            if (isArabic) "ابحث عن منتج (مثال: حليب، إيليو، قهوة...)" else "Search products (e.g., Milk, Oil, Coffee...)",
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isLight) CyanTintText else ElectricCyan
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_price_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isLight) Color.White else Color(0x33141F32),
                        unfocusedContainerColor = if (isLight) Color.White else Color(0x22141F32),
                        focusedBorderColor = if (isLight) CyanTintBorder else ElectricCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Category Chips Selector in PriceComparisonScreen
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(LocalizationUtil.allCategories.size) { idx ->
                            val cat = LocalizationUtil.allCategories[idx]
                            val isSelected = selectedCategory == cat
                            val label = if (cat == "All") {
                                if (isArabic) "الكل" else "All"
                            } else {
                                LocalizationUtil.getCategoryLabel(cat, isArabic)
                            }

                            val isLightCat = MaterialTheme.colorScheme.background == BackgroundLight
                            val chipBg = if (isLightCat) {
                                if (isSelected) CyanTintBg else Color.White
                            } else {
                                if (isSelected) ElectricCyan.copy(alpha = 0.25f) else Color(0x1F1E283A)
                            }
                            val chipBorder = if (isLightCat) {
                                if (isSelected) BorderStroke(1.5.dp, CyanTintBorder) else BorderStroke(1.dp, LightCardBorder)
                            } else {
                                if (isSelected) BorderStroke(1.dp, ElectricCyan) else BorderStroke(1.dp, Color(0x20FFFFFF))
                            }
                            val chipTextColor = if (isLightCat) {
                                if (isSelected) CyanTintText else TextPrimary
                            } else {
                                if (isSelected) ElectricCyan else TextSecondary
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(chipBg)
                                    .border(chipBorder, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setSelectedCategory(cat) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = chipTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Section Title: Products Catalog & Price list
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "قائمة المنتجات والأسعار المباشرة" else "Direct Products & Prices Catalog",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    NeonBadge(
                        text = if (isArabic) "${groupedProducts.size} منتج" else "${groupedProducts.size} Products",
                        accentColor = ElectricCyan
                    )
                }
            }

            // Products Listed Under Each Other (Name + Direct Price)
            items(groupedProducts.entries.toList(), key = { it.key }) { entry ->
                val prices = entry.value
                val first = prices.first()
                val minPrice = prices.minOfOrNull { it.priceDzd } ?: first.priceDzd

                DirectProductPriceItem(
                    barcode = entry.key,
                    itemName = first.itemName,
                    itemNameAr = first.itemNameAr,
                    storeName = first.storeName,
                    storeNameAr = first.storeNameAr,
                    priceDzd = minPrice,
                    isArabic = isArabic,
                    onEditPrice = { reportItemDialog = first },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Crowdsourced Price Update Dialog
    if (reportItemDialog != null) {
        val itemToReport = reportItemDialog!!
        ReportPriceDialog(
            priceItem = itemToReport,
            isArabic = isArabic,
            onDismiss = { reportItemDialog = null },
            onSubmit = { storeName, storeNameAr, newPrice ->
                viewModel.reportPrice(
                    barcode = itemToReport.barcode,
                    storeName = storeName,
                    storeNameAr = storeNameAr,
                    itemName = itemToReport.itemName,
                    priceDzd = newPrice
                )
                reportItemDialog = null
            }
        )
    }

    if (showUpdateSettingsDialog) {
        UpdateSettingsSheet(
            lastSyncTime = lastSyncTime,
            syncIntervalHours = syncIntervalHours,
            updatedCount = updatedCount,
            totalProductsCount = allPrices.map { it.barcode }.distinct().size,
            isSyncing = isSyncingPrices,
            isArabic = isArabic,
            onDismiss = { showUpdateSettingsDialog = false },
            onSelectInterval = { hours -> viewModel.setSyncIntervalHours(hours) },
            onForceRefresh = {
                viewModel.refreshEssentialPrices()
                showUpdateSettingsDialog = false
            }
        )
    }
}

// Dynamic Glowing Animated Background Bloom
@Composable
fun DynamicPullGlowingBackground(
    pullOffset: Float,
    threshold: Float,
    holdProgress: Float,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    if (pullOffset <= 2f && !isSyncing) return

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val pullRatio = (pullOffset / (threshold * 1.4f)).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "aura_shimmer")
    val pulseShimmer by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Morph glow colors based on pull phase and 1-second hold
    val primaryGlow = when {
        holdProgress > 0.6f -> NeonAmber
        holdProgress > 0.1f -> Color(0xFFFFCC00)
        pullOffset >= threshold -> ElectricCyan
        else -> Color(0xFF00E5FF)
    }

    val secondaryGlow = when {
        holdProgress > 0.4f -> Color(0xFFFF5500)
        pullOffset >= threshold -> Color(0xFF00FFA3)
        else -> Color(0xFF8B5CF6)
    }

    val baseAlpha = if (isSyncing) 0.45f else ((pullRatio * 0.6f + holdProgress * 0.4f) * pulseShimmer).coerceIn(0f, 0.95f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .graphicsLayer { alpha = baseAlpha }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = (pullOffset * 0.5f).coerceAtMost(size.height * 0.6f) - 20.dp.toPx()
            val centerOffset = Offset(centerX, centerY)

            val maxRadius = size.width * (0.65f + pullRatio * 0.35f)

            // 1. Ambient outer luminous halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = if (isLight) 0.35f else 0.48f),
                        secondaryGlow.copy(alpha = if (isLight) 0.18f else 0.26f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = maxRadius
                )
            )

            // 2. Focused vibrant radiant plasma core
            val coreRadius = 75.dp.toPx() + pullOffset * 0.9f + holdProgress * 60.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (holdProgress > 0.1f) NeonAmber.copy(alpha = 0.75f) else ElectricCyan.copy(alpha = 0.70f),
                        primaryGlow.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = coreRadius
                )
            )

            // 3. Fluid neon wave light rays cascading down the top
            val waveHeight = (pullOffset * 0.5f).coerceAtMost(80.dp.toPx())
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, waveHeight * 0.6f)
                cubicTo(
                    size.width * 0.3f, waveHeight * 1.35f,
                    size.width * 0.7f, waveHeight * 0.45f,
                    size.width, waveHeight * 0.95f
                )
                lineTo(size.width, 0f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        secondaryGlow.copy(alpha = 0.25f),
                        primaryGlow.copy(alpha = 0.55f),
                        secondaryGlow.copy(alpha = 0.25f)
                    )
                )
            )
        }
    }
}

@Composable
fun PullIndicatorPill(
    pullOffset: Float,
    threshold: Float,
    holdProgress: Float,
    isSyncing: Boolean,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val isHoldActive = holdProgress > 0f
    val isThresholdReached = pullOffset >= threshold

    val borderBrush = when {
        isHoldActive -> Brush.horizontalGradient(listOf(NeonAmber, Color(0xFFFFD54F), NeonAmber))
        isThresholdReached -> Brush.horizontalGradient(listOf(ElectricCyan, Color(0xFF00FFA3), ElectricCyan))
        else -> Brush.horizontalGradient(
            listOf(
                if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.5f),
                if (isLight) Color(0xFF80D8FF) else Color(0xFF7000FF).copy(alpha = 0.45f),
                if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.5f)
            )
        )
    }

    val pillBg = if (isLight) Color.White.copy(alpha = 0.96f) else Color(0xEE0B1426)

    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing)
        ),
        label = "spin"
    )

    val arrowBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_bounce"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(pillBg)
            .border(
                BorderStroke(
                    width = if (isThresholdReached || isHoldActive) 1.5.dp else 1.dp,
                    brush = borderBrush
                ),
                RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag("pull_indicator_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSyncing) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Syncing",
                    tint = ElectricCyan,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(spinRotation)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = if (isArabic) "جاري تحديث أسعار المنتجات..." else "Updating product prices...",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            } else {
                if (isHoldActive) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(18.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { holdProgress },
                            strokeWidth = 2.2.dp,
                            color = NeonAmber,
                            trackColor = Color(0x33FFAA00),
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Hold",
                            tint = NeonAmber,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                } else if (isThresholdReached) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Release to refresh",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Pull",
                        tint = if (isLight) CyanTintText else ElectricCyan,
                        modifier = Modifier
                            .size(15.dp)
                            .offset(y = arrowBounce.dp)
                    )
                }

                Spacer(modifier = Modifier.width(7.dp))

                val textColor = when {
                    isHoldActive -> NeonAmber
                    isThresholdReached -> ElectricCyan
                    isLight -> CyanTintText
                    else -> TextPrimary
                }

                Text(
                    text = if (isArabic) "إسحب للتحديث أو إستمر في السحب للدخول إلى إعدادات المزامنة"
                           else "Pull to refresh or keep pulling for sync settings",
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = if (isThresholdReached || isHoldActive) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateSettingsSheet(
    lastSyncTime: Long,
    syncIntervalHours: Int,
    updatedCount: Int,
    totalProductsCount: Int,
    isSyncing: Boolean,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSelectInterval: (Int) -> Unit,
    onForceRefresh: () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetBg = if (isLight) Color.White else Color(0xFF0D1627)

    val dateFormat = remember(isArabic) {
        val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
        SimpleDateFormat("HH:mm - yyyy/MM/dd", locale)
    }
    val timeFormatted = remember(lastSyncTime, dateFormat) {
        dateFormat.format(Date(lastSyncTime))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isLight) Color(0xFFD0D7DE) else Color(0x40FFFFFF))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("update_settings_sheet")
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) CyanTintBg else ElectricCyan.copy(alpha = 0.16f))
                        .border(
                            BorderStroke(1.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.4f)),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (isLight) CyanTintText else ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isArabic) "إعدادات تحديث الأسعار" else "Price Update Settings",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "تخصيص فترة التحديث وطرق السحب" else "Customize refresh intervals & gestures",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gesture Tutorial Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isLight) Color(0xFFF0F7FF) else Color(0xFF132038))
                    .border(
                        BorderStroke(1.dp, if (isLight) Color(0xFFCCE4FF) else ElectricCyan.copy(alpha = 0.25f)),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = if (isArabic) "كيفية التحكم بحركات السحب:" else "How to use pull gestures:",
                        color = if (isLight) Color(0xFF005580) else ElectricCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Text("⬇️ ", fontSize = 13.sp)
                        Text(
                            text = if (isArabic) "سحب سريع من الأعلى لأسفل: تحديث مباشر وفوري لأسعار المنتجات في الصفحة."
                            else "Quick pull down: Instant update of product prices in the page.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Text("⏳ ", fontSize = 13.sp)
                        Text(
                            text = if (isArabic) "سحب مع الانتظار لمدة ثانية واحدة (1s): يفتح نافذة إعدادات التحديث هذه تلقائيًا."
                            else "Pull and hold for 1 second: Automatically opens this Update Settings modal.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Refresh Interval Title
            Text(
                text = if (isArabic) "التكرار التلقائي للتحديث:" else "Auto-Refresh Frequency:",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Interval options: 6 hours, 12 hours, 24 hours
            val intervals = listOf(
                Triple(6, if (isArabic) "كل 6 ساعات" else "Every 6 Hours", if (isArabic) "تحديث سريع ومتكرر" else "Fast & frequent"),
                Triple(12, if (isArabic) "كل 12 ساعة (افتراضي)" else "Every 12 Hours (Default)", if (isArabic) "توازن مثالي بين الدقة والبطارية" else "Balanced accuracy & battery"),
                Triple(24, if (isArabic) "كل 24 ساعة" else "Every 24 Hours", if (isArabic) "تحديث يومي اقتصادي" else "Economical daily refresh")
            )

            intervals.forEach { (hours, title, desc) ->
                val isSelected = syncIntervalHours == hours
                val optionBg = if (isSelected) {
                    if (isLight) CyanTintBg else ElectricCyan.copy(alpha = 0.15f)
                } else {
                    if (isLight) Color(0xFFFAFAFA) else Color(0x0EFFFFFF)
                }
                val optionBorder = if (isSelected) {
                    BorderStroke(1.5.dp, if (isLight) CyanTintBorder else ElectricCyan)
                } else {
                    BorderStroke(1.dp, if (isLight) LightCardBorder else Color(0x1AFFFFFF))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(optionBg)
                        .border(optionBorder, RoundedCornerShape(12.dp))
                        .clickable { onSelectInterval(hours) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("interval_option_$hours"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectInterval(hours) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (isLight) CyanTintText else ElectricCyan,
                            unselectedColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = desc,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (isLight) CyanTintText else ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Last Update Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "آخر فحص لأسعار السلع:" else "Last Price Check:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = timeFormatted,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "السلع المحدثة حالياً:" else "Currently Updated Items:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = if (isArabic) "$updatedCount من أصل $totalProductsCount" else "$updatedCount of $totalProductsCount",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Action: Force Refresh & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlowingActionButton(
                    text = if (isArabic) "تحديث الآن يدويًا" else "Force Refresh Now",
                    onClick = onForceRefresh,
                    modifier = Modifier.weight(1.2f),
                    icon = Icons.Default.Refresh,
                    color = ElectricCyan
                )

                GlowingActionButton(
                    text = if (isArabic) "إغلاق" else "Close",
                    onClick = onDismiss,
                    modifier = Modifier.weight(0.8f),
                    icon = Icons.Default.Check,
                    color = NeonAmber
                )
            }
        }
    }
}

@Composable
fun DirectProductPriceItem(
    barcode: String,
    itemName: String,
    itemNameAr: String,
    storeName: String,
    storeNameAr: String,
    priceDzd: Double,
    isArabic: Boolean,
    onEditPrice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val cardBg = if (isLight) Color.White else Color(0xFF131D31)
    val cardBorder = if (isLight) BorderStroke(1.dp, LightCardBorder) else BorderStroke(1.dp, Color(0x24FFFFFF))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("product_item_$barcode")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) itemNameAr else itemName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) storeNameAr else storeName,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• EAN: $barcode",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Direct Price Display
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isLight) MintTintBg else Color(0x2600E676)
                        )
                        .border(
                            BorderStroke(1.dp, if (isLight) MintTintBorder else EmeraldSavings.copy(alpha = 0.5f)),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LocalizationUtil.formatDzd(priceDzd, isArabic),
                        color = if (isLight) MintTintText else EmeraldSavings,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onEditPrice,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Price",
                        tint = if (isLight) CyanTintText else ElectricCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalBasketSection(
    summaries: List<StoreBasketSummary>,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("total_basket_card"),
        glowColor = EmeraldSavings
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = EmeraldSavings,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "مقارنة تكلفة السلة الإجمالية" else "Total Basket Comparison",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isArabic) "حساب السعر الكلي لكل قائمة مشترياتك في كل متجر" else "Calculated total for your active shopping list",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                NeonBadge(
                    text = if (isArabic) "محدث" else "LIVE",
                    accentColor = ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (summaries.isEmpty()) {
                Text(
                    text = if (isArabic) "أضف عناصر لقائمتك لمقارنة تكلفة السلة الكلية" else "Add items to your list to compare full basket totals",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                val isLight = MaterialTheme.colorScheme.background == BackgroundLight
                summaries.forEachIndexed { index, summary ->
                    val isCheapest = summary.isBestValue
                    val rowBg = if (isLight) {
                        if (isCheapest) MintTintBg else Color(0xFFF8FAFC)
                    } else {
                        if (isCheapest) Color(0x2E00E676) else Color(0x12FFFFFF)
                    }
                    val rowBorder = if (isLight) {
                        if (isCheapest) BorderStroke(1.5.dp, MintTintBorder) else BorderStroke(1.dp, LightCardBorder)
                    } else {
                        if (isCheapest) BorderStroke(1.dp, EmeraldSavings.copy(alpha = 0.5f)) else BorderStroke(1.dp, Color(0x10FFFFFF))
                    }
                    val saveTextColor = if (isLight) MintTintText else EmeraldSavings

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(rowBg)
                            .border(rowBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${index + 1}",
                                color = if (isCheapest) saveTextColor else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(26.dp)
                            )
                            Column {
                                Text(
                                    text = if (isArabic) summary.storeNameAr else summary.storeName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCheapest) FontWeight.Bold else FontWeight.SemiBold
                                )
                                if (isCheapest && summary.savingsComparedToMaxDzd > 0) {
                                    Text(
                                        text = if (isArabic) {
                                            "وفرت ${LocalizationUtil.formatDzd(summary.savingsComparedToMaxDzd, true)}"
                                        } else {
                                            "Save ${LocalizationUtil.formatDzd(summary.savingsComparedToMaxDzd, false)}"
                                        },
                                        color = saveTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCheapest) {
                                NeonBadge(
                                    text = if (isArabic) "أفضل سعر" else "BEST VALUE",
                                    accentColor = EmeraldSavings,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text(
                                text = LocalizationUtil.formatDzd(summary.totalDzd, isArabic),
                                color = if (isCheapest) saveTextColor else TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductPriceMatrixCard(
    barcode: String,
    itemName: String,
    itemNameAr: String,
    prices: List<MarketPrice>,
    isArabic: Boolean,
    onReportPrice: (MarketPrice) -> Unit,
    modifier: Modifier = Modifier
) {
    val minPrice = remember(prices) { prices.minOfOrNull { it.priceDzd } ?: 0.0 }
    val maxPrice = remember(prices) { prices.maxOfOrNull { it.priceDzd } ?: 0.0 }
    val diff = maxPrice - minPrice

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("price_matrix_$barcode")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) itemNameAr else itemName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "EAN: $barcode",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (diff > 0) {
                    NeonBadge(
                        text = if (isArabic) "فارق ${LocalizationUtil.formatDzd(diff, true)}" else "Diff: ${LocalizationUtil.formatDzd(diff, false)}",
                        accentColor = NeonAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Supermarket Price Breakdown
            val isLight = MaterialTheme.colorScheme.background == BackgroundLight
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prices.forEach { price ->
                    val isCheapest = price.priceDzd == minPrice
                    val rowBg = if (isLight) {
                        if (isCheapest) MintTintBg else Color(0xFFF8FAFC)
                    } else {
                        if (isCheapest) Color(0x2200E676) else Color(0x10FFFFFF)
                    }
                    val rowBorder = if (isLight) {
                        if (isCheapest) BorderStroke(1.dp, MintTintBorder) else BorderStroke(1.dp, LightCardBorder)
                    } else {
                        BorderStroke(1.dp, Color.Transparent)
                    }
                    val priceTextColor = if (isCheapest) (if (isLight) MintTintText else EmeraldSavings) else TextPrimary

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(rowBg)
                            .border(rowBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = if (isCheapest) (if (isLight) MintTintText else EmeraldSavings) else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) price.storeNameAr else price.storeName,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isCheapest) FontWeight.Bold else FontWeight.Normal
                            )
                            if (price.isPromo) {
                                Spacer(modifier = Modifier.width(6.dp))
                                NeonBadge(
                                    text = price.promoNote ?: if (isArabic) "عرض" else "PROMO",
                                    accentColor = NeonAmber
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LocalizationUtil.formatDzd(price.priceDzd, isArabic),
                                color = priceTextColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Crowdsource update icon
                            IconButton(
                                onClick = { onReportPrice(price) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Report Price",
                                    tint = if (isLight) CyanTintText else ElectricCyan.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportPriceDialog(
    priceItem: MarketPrice,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (storeName: String, storeNameAr: String, newPrice: Double) -> Unit
) {
    var priceText by remember { mutableStateOf(priceItem.priceDzd.toInt().toString()) }
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isLight) Color.White else Color(0xFF131C2E),
        title = {
            Text(
                text = if (isArabic) "تحديث سعر المنتج" else "Update Store Price",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = if (isArabic) priceItem.itemNameAr else priceItem.itemName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isArabic) priceItem.storeNameAr else priceItem.storeName,
                    color = EmeraldSavings,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(if (isArabic) "السعر الجديد (د.ج)" else "New Price (DZD)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) {
                        "سيتم تحديث السعر في قاعدة البيانات المحلية لإفادة قائمتك ومقارنة الأسعار."
                    } else {
                        "This updates the local database price for your shopping calculations."
                    },
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            GlowingActionButton(
                text = if (isArabic) "تأكيد التحديث" else "Submit",
                icon = Icons.Default.Check,
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: priceItem.priceDzd
                    onSubmit(priceItem.storeName, priceItem.storeNameAr, p)
                },
                color = EmeraldSavings
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel", color = TextSecondary)
            }
        }
    )
}
