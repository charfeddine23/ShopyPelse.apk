package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.draw.rotate
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
    val lastSyncTime by viewModel.lastPriceSyncTimestamp.collectAsStateWithLifecycle()
    val isSyncingPrices by viewModel.isSyncingPrices.collectAsStateWithLifecycle()
    val updatedCount by viewModel.updatedItemsCount.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var reportItemDialog by remember { mutableStateOf<MarketPrice?>(null) }

    // Group prices by product barcode
    val groupedProducts = remember(allPrices, searchQuery) {
        val filtered = if (searchQuery.isBlank()) {
            allPrices
        } else {
            allPrices.filter {
                it.itemName.contains(searchQuery, ignoreCase = true) ||
                it.itemNameAr.contains(searchQuery, ignoreCase = true) ||
                it.barcode.contains(searchQuery)
            }
        }
        filtered.groupBy { it.barcode }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                            text = if (isArabic) "مقارنة الأسعار وسلة التسوق" else "Market Price Comparison",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "مقارنة أسعار المتاجر الكبرى في الجزائر" else "Compare nearby supermarkets in Algeria",
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
                    onValueChange = { searchQuery = it },
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

            // 12-Hour Automated Essential Product Price Sync Card
            item {
                AutoPriceSyncCard(
                    lastSyncTime = lastSyncTime,
                    isSyncing = isSyncingPrices,
                    isArabic = isArabic,
                    onManualRefresh = { viewModel.refreshEssentialPrices() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Section Title: 1000 Products Catalog & Price list
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
}

@Composable
private fun AutoPriceSyncCard(
    lastSyncTime: Long,
    isSyncing: Boolean,
    isArabic: Boolean,
    onManualRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val dateFormat = remember(isArabic) {
        val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
        SimpleDateFormat("HH:mm - dd/MM", locale)
    }
    val timeFormatted = remember(lastSyncTime, dateFormat) {
        dateFormat.format(Date(lastSyncTime))
    }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("auto_price_sync_card"),
        glowColor = ElectricCyan
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isLight) CyanTintBg else ElectricCyan.copy(alpha = 0.16f)
                        )
                        .border(
                            BorderStroke(1.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.4f)),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = if (isLight) CyanTintText else ElectricCyan,
                        modifier = Modifier
                            .size(20.dp)
                            .then(if (isSyncing) Modifier.rotate(rotation) else Modifier)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isArabic) "تحديث تلقائي كل 12 ساعة" else "Auto-Updates Every 12 Hours",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        NeonBadge(
                            text = "12H",
                            accentColor = ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isSyncing) {
                            if (isArabic) "جاري مسح وتحديث أسعار المنتجات الأساسية..." else "Scanning & updating essential products..."
                        } else {
                            if (isArabic) "آخر مسح لأسعار السلع الأساسية: $timeFormatted" else "Last essential scan: $timeFormatted"
                        },
                        color = if (isSyncing) ElectricCyan else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Manual Refresh Pill Button
            val refreshBg = if (isLight) CyanTintBg else Color(0x2200E5FF)
            val refreshBorder = if (isLight) BorderStroke(1.dp, CyanTintBorder) else BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(refreshBg)
                    .border(refreshBorder, RoundedCornerShape(10.dp))
                    .clickable(enabled = !isSyncing, onClick = onManualRefresh)
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .testTag("manual_sync_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "تحديث الآن" else "Update Now",
                    color = if (isLight) CyanTintText else ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
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
