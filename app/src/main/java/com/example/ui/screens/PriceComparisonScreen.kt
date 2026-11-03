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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.util.LocalizationUtil

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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .size(40.dp)
                            .testTag("price_comparison_back")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
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
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ElectricCyan)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_price_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x33141F32),
                        unfocusedContainerColor = Color(0x22141F32),
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Total Basket Price Comparison Section
            item {
                TotalBasketSection(
                    summaries = basketSummaries,
                    isArabic = isArabic,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            // Section Title
            item {
                Text(
                    text = if (isArabic) "مقارنة أسعار المنتجات حسب المتجر" else "Item-by-Item Store Comparison",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Grouped Product Cards
            items(groupedProducts.entries.toList(), key = { it.key }) { entry ->
                val prices = entry.value
                val first = prices.first()
                ProductPriceMatrixCard(
                    barcode = entry.key,
                    itemName = first.itemName,
                    itemNameAr = first.itemNameAr,
                    prices = prices,
                    isArabic = isArabic,
                    onReportPrice = { reportItemDialog = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
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
                            imageVector = Icons.Default.TrendingDown,
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
                summaries.forEachIndexed { index, summary ->
                    val isCheapest = summary.isBestValue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCheapest) Color(0x2E00E676) else Color(0x12FFFFFF))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isCheapest) EmeraldSavings.copy(alpha = 0.5f) else Color(0x10FFFFFF)
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${index + 1}",
                                color = if (isCheapest) EmeraldSavings else TextSecondary,
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
                                        color = EmeraldSavings,
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
                                color = if (isCheapest) EmeraldSavings else TextPrimary,
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prices.forEach { price ->
                    val isCheapest = price.priceDzd == minPrice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCheapest) Color(0x2200E676) else Color(0x10FFFFFF))
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
                                tint = if (isCheapest) EmeraldSavings else TextSecondary,
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
                                color = if (isCheapest) EmeraldSavings else TextPrimary,
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
                                    tint = ElectricCyan.copy(alpha = 0.7f),
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131B2A),
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
