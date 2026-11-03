package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ShoppingItem
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.theme.CheckedGreen
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ShoppingViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToPriceComparison: () -> Unit,
    onNavigateToSupermarketMode: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenQrShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val basketSummaries by viewModel.basketSummaries.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val filteredItems by remember(items, selectedCategory) {
        derivedStateOf {
            if (selectedCategory == "All") items
            else items.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val stats = remember(items) { viewModel.calculateStats(items) }
    val bestStore = remember(basketSummaries) { basketSummaries.firstOrNull { it.isBestValue } }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp)
        ) {
            // Header Bar
            item {
                HomeHeader(
                    isArabic = isArabic,
                    onNavigateToSettings = onNavigateToSettings,
                    onOpenQrShare = onOpenQrShare,
                    onStartSupermarketMode = onNavigateToSupermarketMode
                )
            }

            // Summary Liquid Glass HUD
            item {
                HomeSummaryHud(
                    stats = stats,
                    bestStore = bestStore,
                    isArabic = isArabic,
                    onViewPrices = onNavigateToPriceComparison,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Frequent Items (One-tap add)
            item {
                FrequentItemsSection(
                    presets = viewModel.frequentPresets,
                    isArabic = isArabic,
                    onAddPreset = { viewModel.addFrequentPreset(it) }
                )
            }

            // Category Filter Chips
            item {
                CategoryChipsSection(
                    selectedCategory = selectedCategory,
                    isArabic = isArabic,
                    onSelectCategory = { viewModel.setSelectedCategory(it) }
                )
            }

            // Action Row: Clean completed / Item count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) {
                            "قائمة المشتريات (${filteredItems.size})"
                        } else {
                            "Shopping Items (${filteredItems.size})"
                        },
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (stats.checkedCount > 0) {
                        Text(
                            text = if (isArabic) "حذف المكتمل (${stats.checkedCount})" else "Clear done (${stats.checkedCount})",
                            color = NeonAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.clearChecked() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("clear_checked_button")
                        )
                    }
                }
            }

            // Shopping Items List
            if (filteredItems.isEmpty()) {
                item {
                    EmptyListCard(isArabic = isArabic, onAddItem = { showAddDialog = true })
                }
            } else {
                items(
                    items = filteredItems,
                    key = { it.id }
                ) { item ->
                    ShoppingItemRow(
                        item = item,
                        isArabic = isArabic,
                        onToggleChecked = { viewModel.toggleChecked(item.id, !item.isChecked) },
                        onIncrement = { viewModel.updateQuantity(item.id, 1) },
                        onDecrement = { viewModel.updateQuantity(item.id, -1) },
                        onDelete = { viewModel.deleteItem(item) },
                        onItemClick = onNavigateToPriceComparison,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Floating Action Button for Adding New Item
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = EmeraldSavings,
            contentColor = Color(0xFF041822),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("add_item_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isArabic) "إضافة عنصر" else "Add Item",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Add Item Modal Bottom Sheet
    if (showAddDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false },
            sheetState = sheetState,
            containerColor = Color(0xFF101726),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x40FFFFFF))
                )
            }
        ) {
            AddItemSheetContent(
                isArabic = isArabic,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAddDialog = false }
                },
                onAddItem = { name, nameAr, category, qty, unit, barcode, price ->
                    viewModel.addItem(name, nameAr, category, qty, unit, barcode, price)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAddDialog = false }
                },
                onScanBarcode = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showAddDialog = false
                        onNavigateToScanner()
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeHeader(
    isArabic: Boolean,
    onNavigateToSettings: () -> Unit,
    onOpenQrShare: () -> Unit,
    onStartSupermarketMode: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isArabic) "شوبي بلس" else "ShopyPelse",
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                NeonBadge(
                    text = "M5 GLASS",
                    accentColor = ElectricCyan
                )
            }
            Text(
                text = if (isArabic) "مقارنة الأسعار الذكية وقائمة التسوق" else "Smart Price Comparison & Shopping",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Settings Button
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .size(40.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // QR Share List Button
            IconButton(
                onClick = onOpenQrShare,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .size(40.dp)
                    .testTag("share_qr_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "Share QR",
                    tint = EmeraldSavings,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Supermarket Mode Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(EmeraldSavings.copy(alpha = 0.2f))
                    .border(BorderStroke(1.dp, EmeraldSavings), RoundedCornerShape(20.dp))
                    .clickable(onClick = onStartSupermarketMode)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("supermarket_mode_header_pill"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = EmeraldSavings,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "المتجر" else "Shop Mode",
                        color = EmeraldSavings,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSummaryHud(
    stats: com.example.ui.viewmodel.ShoppingStats,
    bestStore: com.example.data.model.StoreBasketSummary?,
    isArabic: Boolean,
    onViewPrices: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_summary_hud"),
        glowColor = if (bestStore != null) EmeraldSavings else ElectricCyan
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = if (isArabic) "المجموع التقديري للسلة" else "Estimated Basket Total",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LocalizationUtil.formatDzd(stats.grandTotalDzd, isArabic),
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Progress ratio pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurfaceLight)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${stats.checkedCount}/${stats.totalCount} " + if (isArabic) "جاهز" else "done",
                        color = ElectricCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Best Value Store Banner
            if (bestStore != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x2E00E676))
                        .border(BorderStroke(1.dp, Color(0x6600E676)), RoundedCornerShape(14.dp))
                        .clickable(onClick = onViewPrices)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = EmeraldSavings,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isArabic) "الأرخص: ${bestStore.storeNameAr}" else "Cheapest: ${bestStore.storeName}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (bestStore.savingsComparedToMaxDzd > 0) {
                                Text(
                                    text = if (isArabic) {
                                        "توفر ${LocalizationUtil.formatDzd(bestStore.savingsComparedToMaxDzd, true)}"
                                    } else {
                                        "Save ${LocalizationUtil.formatDzd(bestStore.savingsComparedToMaxDzd, false)}"
                                    },
                                    color = EmeraldSavings,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    NeonBadge(
                        text = if (isArabic) "أفضل سعر" else "BEST VALUE",
                        accentColor = EmeraldSavings
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequentItemsSection(
    presets: List<ShoppingItem>,
    isArabic: Boolean,
    onAddPreset: (ShoppingItem) -> Unit
) {
    Column(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isArabic) "عناصر شائعة (إضافة بنقرة)" else "Frequent Items (Quick Add)",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x281F2A3E))
                        .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(14.dp))
                        .clickable { onAddPreset(preset) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) preset.nameAr else preset.name,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LocalizationUtil.formatDzd(preset.estimatedPriceDzd, isArabic),
                            color = EmeraldSavings,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsSection(
    selectedCategory: String,
    isArabic: Boolean,
    onSelectCategory: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LocalizationUtil.allCategories.forEach { category ->
            val isSelected = selectedCategory == category
            val label = if (category == "All") {
                if (isArabic) "الكل" else "All"
            } else {
                LocalizationUtil.getCategoryLabel(category, isArabic)
            }

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) ElectricCyan.copy(alpha = 0.25f) else Color(0x1F1E283A),
                label = "chip_bg"
            )
            val borderBrush = if (isSelected) {
                Brush.linearGradient(listOf(ElectricCyan, ElectricCyan.copy(alpha = 0.5f)))
            } else {
                Brush.linearGradient(listOf(Color(0x20FFFFFF), Color(0x0AFFFFFF)))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(BorderStroke(1.dp, borderBrush), RoundedCornerShape(12.dp))
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("category_chip_$category")
            ) {
                Text(
                    text = label,
                    color = if (isSelected) ElectricCyan else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    isArabic: Boolean,
    onToggleChecked: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkColor by animateColorAsState(
        targetValue = if (item.isChecked) CheckedGreen else Color.Transparent,
        animationSpec = tween(250),
        label = "check_color"
    )

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shopping_item_${item.id}"),
        backgroundColor = if (item.isChecked) Color(0x1A142030) else GlassSurface,
        glowColor = if (item.isChecked) null else if (item.barcode != null) ElectricCyan.copy(alpha = 0.3f) else null,
        onClick = onItemClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(checkColor)
                    .border(
                        BorderStroke(
                            2.dp,
                            if (item.isChecked) CheckedGreen else Color(0x60FFFFFF)
                        ),
                        CircleShape
                    )
                    .clickable(onClick = onToggleChecked)
                    .testTag("toggle_item_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Item Name & Category Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) item.nameAr else item.name,
                    color = if (item.isChecked) TextMuted else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = LocalizationUtil.getCategoryLabel(item.category, isArabic),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    if (item.barcode != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        NeonBadge(
                            text = if (isArabic) "باركود" else "BARCODE",
                            accentColor = ElectricCyan,
                            icon = Icons.Default.QrCodeScanner
                        )
                    }

                    if (item.estimatedPriceDzd > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LocalizationUtil.formatDzd(item.estimatedPriceDzd * item.quantity, isArabic),
                            color = EmeraldSavings,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quantity Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x30182236))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "–",
                        color = ElectricCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${item.quantity}",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onIncrement),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = ElectricCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Delete item
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyListCard(
    isArabic: Boolean,
    onAddItem: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = ElectricCyan.copy(alpha = 0.6f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isArabic) "قائمة التسوق فارغة" else "Shopping List is Empty",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isArabic) {
                    "أضف عناصر يدوياً أو اختر من العناصر الشائعة أعلاه"
                } else {
                    "Add items manually or tap a frequent item above"
                },
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            GlowingActionButton(
                text = if (isArabic) "إضافة عنصر جديد" else "Add New Item",
                icon = Icons.Default.Add,
                onClick = onAddItem,
                color = ElectricCyan
            )
        }
    }
}

@Composable
fun AddItemSheetContent(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onAddItem: (name: String, nameAr: String, category: String, qty: Int, unit: String, barcode: String?, price: Double) -> Unit,
    onScanBarcode: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameAr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Produce") }
    var quantity by remember { mutableIntStateOf(1) }
    var unit by remember { mutableStateOf("pcs") }
    var barcode by remember { mutableStateOf("") }
    var estimatedPrice by remember { mutableDoubleStateOf(0.0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "إضافة عنصر جديد" else "Add New Item",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Close",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(if (isArabic) "اسم المنتج (مثال: حليب كانديا)" else "Product Name (e.g. Candia Milk)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_item_name"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = GlassBorder,
                focusedLabelColor = ElectricCyan,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Price & Quantity row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = if (estimatedPrice > 0) estimatedPrice.toInt().toString() else "",
                onValueChange = { estimatedPrice = it.toDoubleOrNull() ?: 0.0 },
                label = { Text(if (isArabic) "السعر (د.ج)" else "Price (DZD)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_item_price"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldSavings,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Quantity selector
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GlassSurfaceLight)
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                    Text("–", color = ElectricCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "$quantity $unit",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { quantity++ }) {
                    Text("+", color = ElectricCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Barcode input with Quick Scan Trigger
        OutlinedTextField(
            value = barcode,
            onValueChange = { barcode = it },
            label = { Text(if (isArabic) "الرمز الشريطي / باركود (اختياري)" else "Barcode / EAN (Optional)") },
            trailingIcon = {
                IconButton(onClick = onScanBarcode) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan",
                        tint = ElectricCyan
                    )
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_item_barcode"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Submit Button
        GlowingActionButton(
            text = if (isArabic) "إضافة إلى القائمة" else "Add to Shopping List",
            icon = Icons.Default.Add,
            enabled = name.isNotBlank(),
            onClick = {
                if (name.isNotBlank()) {
                    onAddItem(name, nameAr, category, quantity, unit, barcode, estimatedPrice)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_add_item_button"),
            color = EmeraldSavings
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
