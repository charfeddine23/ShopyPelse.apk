package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
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
import com.example.ui.components.SwipeToDeleteContainer
import com.example.ui.components.WindGlowAppNameBanner
import com.example.ui.components.disableParentPagerSwipe
import com.example.ui.components.horizontalFadingEdge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CheckedGreen
import com.example.ui.theme.CyanTintBg
import com.example.ui.theme.CyanTintBorder
import com.example.ui.theme.CyanTintText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceDark
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
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.util.LocalizationUtil
import com.example.util.ProductGroup
import com.example.util.ProductVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ShoppingViewModel,
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToPriceComparison: () -> Unit,
    onNavigateToSupermarketMode: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onOpenQrShare: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val items by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val basketSummaries by viewModel.basketSummaries.collectAsStateWithLifecycle()
    val isDynamicGlowEnabled by viewModel.isDynamicGlowEnabled.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showFinishTripDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val filteredItems by remember(items, selectedCategory) {
        derivedStateOf {
            if (selectedCategory == "All") items
            else items.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    // Auto-reset category filter if all items in selected category were removed
    LaunchedEffect(items) {
        if (selectedCategory != "All" && items.none { it.category.equals(selectedCategory, ignoreCase = true) }) {
            viewModel.setSelectedCategory("All")
        }
    }

    val stats = remember(items) { viewModel.calculateStats(items) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp)
        ) {
            // Header Bar
            item {
                HomeHeader(
                    isArabic = isArabic,
                    isDynamicGlowEnabled = isDynamicGlowEnabled,
                    onStartSupermarketMode = onNavigateToSupermarketMode
                )
            }

            // Summary Liquid Glass HUD (estimated total without best price banner)
            item {
                HomeSummaryHud(
                    stats = stats,
                    isArabic = isArabic,
                    onFinishShopping = { showFinishTripDialog = true },
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

            // Category Filter Chips (Filters ONLY the items present in the shopping list in-place)
            item {
                CategoryChipsSection(
                    items = items,
                    selectedCategory = selectedCategory,
                    isArabic = isArabic,
                    onSelectCategory = { category ->
                        viewModel.setSelectedCategory(category)
                    }
                )
            }

            // Action Row: Clean completed / Item count / Direct Add
            item {
                val isLight = MaterialTheme.colorScheme.background == BackgroundLight
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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

                        // Compact glowing Add button in place of the removed green FAB
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLight) CyanTintBg else ElectricCyan.copy(alpha = 0.16f))
                                .border(
                                    BorderStroke(1.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.5f)),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { showAddDialog = true }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("add_item_action_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (isLight) CyanTintText else ElectricCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "إضافة عنصر" else "Add Item",
                                    color = if (isLight) CyanTintText else ElectricCyan,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                    SwipeToDeleteContainer(
                        onDelete = { viewModel.deleteItem(item) },
                        isArabic = isArabic,
                        deleteLabel = if (isArabic) "حذف" else "Delete",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        ShoppingItemRow(
                            item = item,
                            isArabic = isArabic,
                            onToggleChecked = { viewModel.toggleChecked(item.id, !item.isChecked) },
                            onIncrement = { viewModel.updateQuantity(item.id, 1) },
                            onDecrement = { viewModel.updateQuantity(item.id, -1) },
                            onDelete = { viewModel.deleteItem(item) },
                            onItemClick = onNavigateToPriceComparison
                        )
                    }
                }
            }
        }
    }

    // Add Item Modal Bottom Sheet
    if (showAddDialog) {
        val isLight = MaterialTheme.colorScheme.background == BackgroundLight
        val productGroups by viewModel.productGroups.collectAsStateWithLifecycle()
        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false },
            sheetState = sheetState,
            containerColor = if (isLight) Color.White else Color(0xFF101726),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isLight) Color(0xFFCBD5E1) else Color(0x40FFFFFF))
                )
            }
        ) {
            AddItemSheetContent(
                isArabic = isArabic,
                productGroups = productGroups,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAddDialog = false }
                },
                onAutoCategorize = { n, na -> viewModel.autoCategorize(n, na) },
                onAddItem = { name, nameAr, category, qty, unit, barcode, price ->
                    viewModel.addItem(name, nameAr, category, qty, unit, barcode, price)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAddDialog = false }
                }
            )
        }
    }

    // Finish Shopping Trip & Transfer to Analytics Dialog
    if (showFinishTripDialog) {
            val stores = listOf(
                Pair("Uno Hypermarket", "هايبرماركت أونو"),
                Pair("Ardis Alger", "أرديس الجزائر"),
                Pair("Carrefour Alger", "كارفور الجزائر"),
                Pair("Superette El Baraka", "سوبرماركت البركة"),
                Pair("Superette En-Nour", "سوبرماركت النور")
            )
            var selectedStoreIndex by remember { mutableStateOf(0) }
            val isLight = MaterialTheme.colorScheme.background == BackgroundLight

            AlertDialog(
                onDismissRequest = { showFinishTripDialog = false },
                containerColor = if (isLight) Color.White else Color(0xFF131D2E),
                title = {
                    Text(
                        text = if (isArabic) "إنهاء التسوق ونقل المجموع للإنفاق" else "Finish Shopping Trip",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = if (isArabic)
                                "سيتم إنهاء التسوق ونقل مجموع السلة المقدر بـ ${LocalizationUtil.formatDzd(stats.grandTotalDzd, isArabic)} فورياً إلى صفحة الإحصائيات والإنفاق وتفريغ السلة."
                            else
                                "This will transfer ${LocalizationUtil.formatDzd(stats.grandTotalDzd, isArabic)} to your monthly spending analytics and clear the cart.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isArabic) "اختر متجر الشراء:" else "Select store:",
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalFadingEdge(fadeWidth = 24.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stores.forEachIndexed { index, (en, ar) ->
                                val isSelected = selectedStoreIndex == index
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) (if (isLight) CyanTintBg else ElectricCyan.copy(alpha = 0.25f)) else (if (isLight) Color(0xFFF1F5F9) else Color(0x18FFFFFF)))
                                        .border(BorderStroke(1.dp, if (isSelected) ElectricCyan else Color.Transparent), RoundedCornerShape(8.dp))
                                        .clickable { selectedStoreIndex = index }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) ar else en,
                                        color = if (isSelected) (if (isLight) CyanTintText else ElectricCyan) else TextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    GlowingActionButton(
                        text = if (isArabic) "تأكيد ونقل للإحصائيات" else "Confirm & Transfer",
                        color = EmeraldSavings,
                        onClick = {
                            val store = stores[selectedStoreIndex]
                            viewModel.finishShoppingTrip(storeName = store.first, storeNameAr = store.second)
                            showFinishTripDialog = false
                        }
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showFinishTripDialog = false }) {
                        Text(if (isArabic) "إلغاء" else "Cancel", color = TextSecondary)
                    }
                }
            )
    }
}

@Composable
private fun HomeHeader(
    isArabic: Boolean,
    isDynamicGlowEnabled: Boolean,
    onStartSupermarketMode: () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val shopPillBg = if (isLight) MintTintBg else EmeraldSavings.copy(alpha = 0.2f)
    val shopPillBorder = if (isLight) MintTintBorder else EmeraldSavings
    val shopPillContent = if (isLight) MintTintText else EmeraldSavings

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        WindGlowAppNameBanner(
            isArabic = isArabic,
            isGlowEnabled = isDynamicGlowEnabled,
            trailingAction = {
                // Cart / Supermarket Mode Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(shopPillBg)
                        .border(BorderStroke(1.dp, shopPillBorder), RoundedCornerShape(18.dp))
                        .clickable(onClick = onStartSupermarketMode)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("supermarket_mode_header_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = shopPillContent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isArabic) "السلة" else "Cart",
                            color = shopPillContent,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun HomeSummaryHud(
    stats: com.example.ui.viewmodel.ShoppingStats,
    isArabic: Boolean,
    onFinishShopping: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_summary_hud"),
        glowColor = ElectricCyan
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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

                val ratioBg = if (isLight) CyanTintBg else GlassSurfaceLight
                val ratioBorder = if (isLight) BorderStroke(1.dp, CyanTintBorder) else BorderStroke(1.dp, Color(0x20FFFFFF))
                val ratioText = if (isLight) CyanTintText else ElectricCyan

                Column(horizontalAlignment = Alignment.End) {
                    // Progress ratio pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ratioBg)
                            .border(ratioBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${stats.checkedCount}/${stats.totalCount} " + if (isArabic) "جاهز" else "done",
                            color = ratioText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (stats.totalCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLight) MintTintBg else EmeraldSavings.copy(alpha = 0.22f))
                                .border(
                                    BorderStroke(1.dp, if (isLight) MintTintBorder else EmeraldSavings.copy(alpha = 0.65f)),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable(onClick = onFinishShopping)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("finish_shopping_hud_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isLight) MintTintText else EmeraldSavings,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isArabic) "إنهاء التسوق" else "Finish Trip",
                                    color = if (isLight) MintTintText else EmeraldSavings,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
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
private fun FrequentItemsSection(
    presets: List<ShoppingItem>,
    isArabic: Boolean,
    onAddPreset: (ShoppingItem) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val presetCardBg = if (isLight) Color.White else Color(0x281F2A3E)
    val presetCardBorder = if (isLight) BorderStroke(1.dp, LightCardBorder) else BorderStroke(1.dp, Color(0x33FFFFFF))
    val presetPriceColor = if (isLight) MintTintText else EmeraldSavings

    Column(
        modifier = Modifier
            .disableParentPagerSwipe()
            .padding(top = 10.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = null,
                tint = if (isLight) CyanTintText else ElectricCyan,
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
            modifier = Modifier
                .fillMaxWidth()
                .horizontalFadingEdge(fadeWidth = 26.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(presetCardBg)
                        .border(presetCardBorder, RoundedCornerShape(14.dp))
                        .clickable { onAddPreset(preset) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = if (isLight) CyanTintText else ElectricCyan,
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
                            color = presetPriceColor,
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
    items: List<ShoppingItem>,
    selectedCategory: String,
    isArabic: Boolean,
    onSelectCategory: (String) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    // Filter categories to only those that exist in the shopping list
    val presentCategories = remember(items) {
        items.map { it.category }.filter { it.isNotBlank() }.distinct()
    }

    // Always show "All", and only categories that have items in the current list
    val categoriesToShow = remember(presentCategories) {
        if (presentCategories.isEmpty()) {
            listOf("All")
        } else {
            listOf("All") + presentCategories
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .disableParentPagerSwipe()
            .horizontalFadingEdge(fadeWidth = 26.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categoriesToShow.forEach { category ->
            val isSelected = selectedCategory == category
            val count = if (category == "All") {
                items.size
            } else {
                items.count { it.category.equals(category, ignoreCase = true) }
            }

            val baseLabel = if (category == "All") {
                if (isArabic) "الكل" else "All"
            } else {
                LocalizationUtil.getCategoryLabel(category, isArabic)
            }
            val label = "$baseLabel ($count)"

            val chipBg = if (isLight) {
                if (isSelected) CyanTintBg else Color.White
            } else {
                if (isSelected) ElectricCyan.copy(alpha = 0.25f) else Color(0x1F1E283A)
            }
            val chipBorder = if (isLight) {
                if (isSelected) BorderStroke(1.5.dp, CyanTintBorder) else BorderStroke(1.dp, LightCardBorder)
            } else {
                if (isSelected) BorderStroke(1.dp, ElectricCyan) else BorderStroke(1.dp, Color(0x20FFFFFF))
            }
            val chipTextColor = if (isLight) {
                if (isSelected) CyanTintText else TextSecondaryLight
            } else {
                if (isSelected) ElectricCyan else TextSecondary
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(chipBg)
                    .border(chipBorder, RoundedCornerShape(12.dp))
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("category_chip_$category")
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

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    // Solid 100% opaque backgrounds: Zero transparency so the red delete strip never shows through
    val cardBackground = if (item.isChecked) {
        if (isLight) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    } else {
        if (isLight) Color(0xFFFFFFFF) else Color(0xFF131D31)
    }
    val uncheckBorder = if (isLight) Color(0xFF94A3B8) else Color(0x60FFFFFF)
    val itemPriceColor = if (isLight) MintTintText else EmeraldSavings
    val stepperBg = if (isLight) Color(0xFFEDF2F7) else Color(0x30182236)
    val stepperBorder = if (isLight) BorderStroke(1.dp, LightCardBorder) else BorderStroke(1.dp, Color(0x15FFFFFF))

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shopping_item_${item.id}"),
        backgroundColor = cardBackground,
        glowColor = if (item.isChecked) null else if (isLight) null else ElectricCyan.copy(alpha = 0.2f),
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
                            if (item.isChecked) CheckedGreen else uncheckBorder
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
                val rawTitle = if (isArabic) item.nameAr else item.name
                val cleanedTitle = rawTitle
                    .replace("(مجموعة)", "")
                    .replace("- مجموعة", "")
                    .replace(" - مجموعة", "")
                    .trim()
                val isGroupItem = item.barcode?.startsWith("GRP-") == true ||
                        item.unit.equals("مجموعة", ignoreCase = true) ||
                        item.unit.equals("group", ignoreCase = true)
                val displayTitle = if (isGroupItem) {
                    cleanedTitle
                } else if (cleanedTitle.contains("(") && cleanedTitle.contains(")")) {
                    val main = cleanedTitle.substringBefore("(").trim()
                    val details = cleanedTitle.substringAfter("(").substringBefore(")").trim()
                    if (details.equals("مجموعة", ignoreCase = true) || details.equals("group", ignoreCase = true)) {
                        main
                    } else {
                        "$main - $details"
                    }
                } else {
                    cleanedTitle
                }

                Text(
                    text = displayTitle,
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

                    if (item.estimatedPriceDzd > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LocalizationUtil.formatDzd(item.estimatedPriceDzd * item.quantity, isArabic),
                            color = itemPriceColor,
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
                    .background(stepperBg)
                    .border(stepperBorder, RoundedCornerShape(10.dp))
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
                        color = if (isLight) CyanTintText else ElectricCyan,
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
                        color = if (isLight) CyanTintText else ElectricCyan,
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
    productGroups: List<ProductGroup> = emptyList(),
    onDismiss: () -> Unit,
    onAutoCategorize: (name: String, nameAr: String) -> String,
    onAddItem: (name: String, nameAr: String, category: String, qty: Int, unit: String, barcode: String?, price: Double) -> Unit
) {
    var productSearchQuery by remember { mutableStateOf("") }
    var isManualEntryExpanded by remember { mutableStateOf(false) }

    // Manual item entry states
    var manualName by remember { mutableStateOf("") }
    var manualNameAr by remember { mutableStateOf("") }
    var manualCategory by remember { mutableStateOf("") }
    var manualQuantity by remember { mutableIntStateOf(1) }
    var manualUnit by remember { mutableStateOf("pcs") }
    var manualPrice by remember { mutableDoubleStateOf(0.0) }

    // Filter pre-grouped catalog products off the heavy grouping step
    val filteredGroups = remember(productGroups, productSearchQuery) {
        if (productSearchQuery.isBlank()) {
            productGroups
        } else {
            val q = productSearchQuery.trim()
            productGroups.filter { group ->
                group.nameAr.contains(q, ignoreCase = true) ||
                group.nameEn.contains(q, ignoreCase = true) ||
                group.category.contains(q, ignoreCase = true) ||
                group.variants.any { v ->
                    v.itemName.contains(q, ignoreCase = true) ||
                    v.itemNameAr.contains(q, ignoreCase = true) ||
                    v.barcode.contains(q)
                }
            }
        }
    }

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 680.dp)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Row: Title & Close Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "إضافة عنصر إلى القائمة" else "Add Item to Shopping List",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) {
                            "تصفح المجموعات أو ابحث لإضافة سريعة للسلة"
                        } else {
                            "Browse groups or search for quick cart additions"
                        },
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Compact Elegant Glowing Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isLight) Color(0xFFF8FAFC) else Color(0x350F1829)
                    )
                    .border(
                        BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    ElectricCyan.copy(alpha = 0.9f),
                                    EmeraldSavings.copy(alpha = 0.7f),
                                    ElectricCyan.copy(alpha = 0.9f)
                                )
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(ElectricCyan),
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp)
                            .testTag("catalog_group_search_input"),
                        decorationBox = { innerTextField ->
                            if (productSearchQuery.isEmpty()) {
                                Text(
                                    text = if (isArabic) "البحث عن منتج أو صنف أو مجموعة" else "Search product, variant or group",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (productSearchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { productSearchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Search Status / Count Pill
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (productSearchQuery.isNotBlank()) {
                        if (isArabic) "المجموعات المطابقة (${filteredGroups.size})" else "Matching Groups (${filteredGroups.size})"
                    } else {
                        if (isArabic) "المجموعات الرئيسية (${filteredGroups.size})" else "All Product Groups (${filteredGroups.size})"
                    },
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (productSearchQuery.isNotBlank()) {
                    Text(
                        text = if (isArabic) "تظهر المجموعات فقط (توسيع للاطلاع على الصنف)" else "Groups only (Expand to view variants)",
                        color = if (isLight) CyanTintText else ElectricCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Empty Search Results State
        if (filteredGroups.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) Color(0xFFF8FAFC) else Color(0x18FFFFFF))
                        .border(BorderStroke(1.dp, if (isLight) LightCardBorder else Color(0x20FFFFFF)), RoundedCornerShape(12.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isArabic) "لم يتم العثور على مجموعة مطابقة لـ \"$productSearchQuery\"" else "No groups matching \"$productSearchQuery\"",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLight) CyanTintBg else ElectricCyan.copy(alpha = 0.18f))
                                .border(BorderStroke(1.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                                .clickable {
                                    manualName = productSearchQuery
                                    manualNameAr = productSearchQuery
                                    manualCategory = onAutoCategorize(manualName, manualNameAr)
                                    isManualEntryExpanded = true
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (isLight) CyanTintText else ElectricCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "إضافة \"$productSearchQuery\" كمنتج يدوي" else "Add \"$productSearchQuery\" manually",
                                    color = if (isLight) CyanTintText else ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Groups Listing (Show groups only, each expandable to show its variants)
        items(
            items = filteredGroups,
            key = { it.id }
        ) { group ->
            AddItemGroupCard(
                group = group,
                isArabic = isArabic,
                onAddGroup = { grp ->
                    val finalCat = grp.category.ifBlank { onAutoCategorize(grp.nameEn, grp.nameAr) }
                    onAddItem(
                        grp.nameEn,
                        grp.nameAr,
                        finalCat,
                        1,
                        if (isArabic) "مجموعة" else "group",
                        "GRP-${grp.id}",
                        grp.avgPrice
                    )
                },
                onAddVariant = { variant ->
                    val finalCat = group.category.ifBlank { onAutoCategorize(variant.itemName, variant.itemNameAr) }
                    onAddItem(
                        variant.itemName,
                        variant.itemNameAr,
                        finalCat,
                        1,
                        "pcs",
                        variant.barcode,
                        variant.priceDzd
                    )
                }
            )
        }

        // Manual Custom Product Entry (Collapsible Accordion at the bottom)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isLight) Color(0xFFF8FAFC) else Color(0x161D2B44))
                    .border(
                        BorderStroke(1.dp, if (isLight) LightCardBorder else Color(0x24FFFFFF)),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { isManualEntryExpanded = !isManualEntryExpanded }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("toggle_manual_entry_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (isLight) CyanTintText else ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "أو إدخال منتج يدوي مخصص (سعر، كمية)" else "Or enter custom product (price, quantity)",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = if (isManualEntryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Expanded Manual Entry Form
        if (isManualEntryExpanded) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isLight) Color.White else Color(0xFF131D31))
                        .border(
                            BorderStroke(1.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.4f)),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Manual Name Input
                    OutlinedTextField(
                        value = if (isArabic && manualNameAr.isNotBlank()) manualNameAr else manualName,
                        onValueChange = {
                            manualName = it
                            if (isArabic) manualNameAr = it
                            manualCategory = onAutoCategorize(manualName, manualNameAr)
                        },
                        label = { Text(if (isArabic) "اسم المنتج المخصص" else "Custom Product Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_item_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Price & Quantity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = if (manualPrice > 0) manualPrice.toInt().toString() else "",
                            onValueChange = { manualPrice = it.toDoubleOrNull() ?: 0.0 },
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

                        // Quantity stepper
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLight) Color(0xFFF1F5F9) else GlassSurfaceLight)
                                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = { if (manualQuantity > 1) manualQuantity-- }) {
                                Text("–", color = ElectricCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "$manualQuantity $manualUnit",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { manualQuantity++ }) {
                                Text("+", color = ElectricCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Submit Manual Item Button
                    GlowingActionButton(
                        text = if (isArabic) "إضافة إلى القائمة" else "Add to Shopping List",
                        icon = Icons.Default.Add,
                        enabled = manualName.isNotBlank(),
                        onClick = {
                            if (manualName.isNotBlank()) {
                                val finalCat = manualCategory.ifBlank { onAutoCategorize(manualName, manualNameAr) }
                                onAddItem(
                                    manualName,
                                    manualNameAr,
                                    finalCat,
                                    manualQuantity,
                                    manualUnit,
                                    null,
                                    manualPrice
                                )
                                manualName = ""
                                manualNameAr = ""
                                manualPrice = 0.0
                                isManualEntryExpanded = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_add_item_button"),
                        color = EmeraldSavings
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AddItemGroupCard(
    group: ProductGroup,
    isArabic: Boolean,
    onAddGroup: (ProductGroup) -> Unit,
    onAddVariant: (ProductVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    var isExpanded by remember { mutableStateOf(false) }
    var groupAdded by remember { mutableStateOf(false) }
    var justAddedId by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current

    val cardBg = if (isLight) Color.White else Color(0xFF131D31)
    val cardBorder = if (isExpanded) {
        BorderStroke(1.2.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.5f))
    } else {
        BorderStroke(1.dp, if (isLight) LightCardBorder else Color(0x22FFFFFF))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(cardBorder, RoundedCornerShape(14.dp))
            .testTag("sheet_group_${group.id}")
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            // Top: Category tag + Variants Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val catLabel = LocalizationUtil.getCategoryLabel(group.category, isArabic)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isLight) Color(0xFFEBF5FF) else ElectricCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = catLabel,
                        color = if (isLight) CyanTintText else ElectricCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isLight) Color(0xFFF1F5F9) else Color(0x18FFFFFF))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = if (isArabic) "${group.variants.size} أصناف" else "${group.variants.size} variants",
                        color = TextSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Info: Group Name & Average Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) group.nameAr else group.nameEn,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isArabic) "متوسط المجموعة: ${LocalizationUtil.formatDzd(group.avgPrice, true)}" else "Group Average: ${LocalizationUtil.formatDzd(group.avgPrice, false)}",
                        color = if (isLight) MintTintText else EmeraldSavings,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Price display (Average price + range)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = LocalizationUtil.formatDzd(group.avgPrice, isArabic),
                        color = EmeraldSavings,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (group.minPrice != group.maxPrice) {
                        Text(
                            text = "${LocalizationUtil.formatDzd(group.minPrice, isArabic)} - ${LocalizationUtil.formatDzd(group.maxPrice, isArabic)}",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Quick Add Group Button + Expand Accordion Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Add Group Button (Adds group itself with average price)
                val addBg = if (groupAdded) EmeraldSavings else if (isLight) Color(0xFF0284C7) else ElectricCyan
                val addContentColor = if (groupAdded) Color.White else if (isLight) Color.White else Color(0xFF051323)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(addBg)
                        .clickable {
                            try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                            onAddGroup(group)
                            groupAdded = true
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("quick_add_sheet_group_${group.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (groupAdded) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            tint = addContentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (groupAdded) {
                                if (isArabic) "أُضيفت المجموعة!" else "Group Added!"
                            } else {
                                if (isArabic) "إضافة المجموعة (${group.avgPrice.toInt()} د.ج)" else "Add Group (${group.avgPrice.toInt()} DZD)"
                            },
                            color = addContentColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Expand / Collapse Accordion Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLight) Color(0xFFF1F5F9) else Color(0x18FFFFFF))
                        .border(
                            BorderStroke(1.dp, if (isLight) LightCardBorder else Color(0x1FFFFFFF)),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("expand_sheet_group_${group.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isExpanded) {
                                if (isArabic) "طي المجموعة" else "Collapse"
                            } else {
                                if (isArabic) "توسيع للمجموعة (${group.variants.size})" else "Expand group (${group.variants.size})"
                            },
                            color = if (isLight) CyanTintText else ElectricCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = if (isLight) CyanTintText else ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Expanded Variants List
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = if (isLight) Color(0xFFE2E8F0) else Color(0x18FFFFFF))
                    Text(
                        text = if (isArabic) "اختر صنفاً أو نكهة محددة:" else "Choose a specific variant:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    var visibleLimit by remember(group.id, isExpanded) { mutableIntStateOf(20) }
                    val visibleVariants = remember(group.variants, visibleLimit) {
                        group.variants.take(visibleLimit)
                    }

                    visibleVariants.forEach { variant ->
                        val isVarAdded = (justAddedId == variant.barcode)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLight) Color(0xFFF8FAFC) else Color(0x14FFFFFF))
                                .border(
                                    BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0x10FFFFFF)),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) variant.itemNameAr else variant.itemName,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = LocalizationUtil.formatDzd(variant.priceDzd, isArabic),
                                    color = EmeraldSavings,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isVarAdded) EmeraldSavings else if (isLight) Color(0xFFE0F2FE) else ElectricCyan.copy(alpha = 0.2f))
                                    .border(
                                        BorderStroke(1.dp, if (isVarAdded) EmeraldSavings else if (isLight) Color(0xFFBAE6FD) else ElectricCyan.copy(alpha = 0.4f)),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                                        onAddVariant(variant)
                                        justAddedId = variant.barcode
                                    }
                                    .padding(horizontal = 9.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isVarAdded) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = null,
                                        tint = if (isVarAdded) Color.White else if (isLight) Color(0xFF0369A1) else ElectricCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isVarAdded) (if (isArabic) "أُضيف" else "Added") else (if (isArabic) "أضف" else "Add"),
                                        color = if (isVarAdded) Color.White else if (isLight) Color(0xFF0369A1) else ElectricCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (group.variants.size > visibleLimit) {
                        val remaining = group.variants.size - visibleLimit
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLight) CyanTintBg else ElectricCyan.copy(alpha = 0.14f))
                                .border(
                                    BorderStroke(1.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.35f)),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { visibleLimit += 25 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "عرض المزيد ($remaining متبقي)" else "Show more ($remaining remaining)",
                                color = if (isLight) CyanTintText else ElectricCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
