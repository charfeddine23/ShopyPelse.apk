package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.theme.BackgroundLight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.ui.theme.CheckedGreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.util.LocalizationUtil

@Composable
fun SupermarketModeScreen(
    viewModel: ShoppingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val budgetLimit by viewModel.budgetLimitDzd.collectAsStateWithLifecycle()

    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var hideChecked by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val stats = remember(items) { viewModel.calculateStats(items) }

    val displayItems by remember(items, hideChecked) {
        derivedStateOf {
            if (hideChecked) items.filter { !it.isChecked } else items
        }
    }

    val budgetProgress by animateFloatAsState(
        targetValue = if (budgetLimit > 0) {
            (stats.checkedTotalDzd / budgetLimit).toFloat().coerceIn(0f, 1f)
        } else 0f,
        label = "budget_progress"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            stats.checkedTotalDzd > budgetLimit -> NeonRed
            stats.checkedTotalDzd >= budgetLimit * 0.85 -> NeonAmber
            else -> EmeraldSavings
        },
        label = "progress_color"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Sticky-like Top Bar & HUD
            item {
                SupermarketModeHeader(
                    isArabic = isArabic,
                    onBack = onBack,
                    hideChecked = hideChecked,
                    onToggleHideChecked = { hideChecked = !hideChecked },
                    onFinishShopping = { showFinishDialog = true }
                )
            }

            // Running Budget Live Calculator Card
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("supermarket_budget_hud"),
                    glowColor = progressColor
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "إجمالي المشتريات الحالية (في السلة)" else "Current Cart Total",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = LocalizationUtil.formatDzd(stats.checkedTotalDzd, isArabic),
                                        color = progressColor,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "/ " + LocalizationUtil.formatDzd(budgetLimit, isArabic),
                                        color = TextSecondary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }

                            // Edit budget button
                            IconButton(
                                onClick = { showEditBudgetDialog = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0x20FFFFFF))
                                    .size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Budget",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Linear Budget Progress Bar
                        LinearProgressIndicator(
                            progress = { budgetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = progressColor,
                            trackColor = Color(0x33FFFFFF)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stats.checkedCount} / ${stats.totalCount} " +
                                    if (isArabic) "عناصر تم وضعها في العربة" else "items in cart",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            val remaining = budgetLimit - stats.checkedTotalDzd
                            if (remaining >= 0) {
                                Text(
                                    text = if (isArabic) {
                                        "متبقي للميزانية: ${LocalizationUtil.formatDzd(remaining, true)}"
                                    } else {
                                        "Remaining: ${LocalizationUtil.formatDzd(remaining, false)}"
                                    },
                                    color = EmeraldSavings,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = if (isArabic) {
                                        "تجاوزت الميزانية بـ ${LocalizationUtil.formatDzd(-remaining, true)}"
                                    } else {
                                        "Over by ${LocalizationUtil.formatDzd(-remaining, false)}"
                                    },
                                    color = NeonRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Swipe hint
            if (displayItems.isNotEmpty()) {
                item {
                    Text(
                        text = if (isArabic) "👈 اسحب أي عنصر لحذفه من السلة" else "👉 Swipe any item to remove from cart",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                    )
                }
            }

            // High Contrast Aisle Items List with Swipe-to-Delete
            items(displayItems, key = { it.id }) { item ->
                SwipeToDeleteContainer(
                    onDelete = { viewModel.deleteItem(item) },
                    isArabic = isArabic,
                    deleteLabel = if (isArabic) "حذف من السلة" else "Remove from cart",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    SupermarketItemCard(
                        item = item,
                        isArabic = isArabic,
                        onToggleChecked = { viewModel.toggleChecked(item.id, !item.isChecked) },
                        onIncrement = { viewModel.updateQuantity(item.id, 1) },
                        onDecrement = { viewModel.updateQuantity(item.id, -1) }
                    )
                }
            }
        }

        // Bottom Done bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xF00B0E14), Color(0xFF0B0E14))
                    )
                )
                .padding(16.dp)
        ) {
            GlowingActionButton(
                text = if (isArabic) "إنهاء التسوق وتفريغ المكتمل" else "Finish Shopping (${stats.checkedCount}/${stats.totalCount})",
                icon = Icons.Default.DoneAll,
                color = EmeraldSavings,
                onClick = { showFinishDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("finish_shopping_bottom_button")
            )
        }
    }

    // Edit Budget Dialog
    if (showEditBudgetDialog) {
        var newBudgetText by remember { mutableStateOf(budgetLimit.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showEditBudgetDialog = false },
            containerColor = Color(0xFF131B2A),
            title = {
                Text(
                    text = if (isArabic) "تحديد ميزانية التسوق" else "Set Shopping Budget",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newBudgetText,
                    onValueChange = { newBudgetText = it },
                    label = { Text(if (isArabic) "الميزانية بالدينار الجزائري (د.ج)" else "Budget (DZD)") },
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldSavings,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                GlowingActionButton(
                    text = if (isArabic) "حفظ" else "Save",
                    color = EmeraldSavings,
                    onClick = {
                        val parsed = newBudgetText.toDoubleOrNull() ?: budgetLimit
                        viewModel.setBudgetLimit(parsed)
                        showEditBudgetDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditBudgetDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Finish Shopping Summary Celebration Dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = Color(0xFF131B2A),
            title = {
                Text(
                    text = if (isArabic) "اكتملت جولة التسوق بنجاح!" else "Shopping Completed!",
                    color = EmeraldSavings,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isArabic) {
                            "قمت بشراء ${stats.checkedCount} عنصر بتكلفة إجمالية قدرها:"
                        } else {
                            "You purchased ${stats.checkedCount} items for a total of:"
                        },
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = LocalizationUtil.formatDzd(stats.checkedTotalDzd, isArabic),
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                GlowingActionButton(
                    text = if (isArabic) "حذف العناصر المكتملة" else "Clear Purchased Items",
                    color = EmeraldSavings,
                    onClick = {
                        viewModel.clearChecked()
                        showFinishDialog = false
                        onBack()
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text(if (isArabic) "إغلاق" else "Close", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SupermarketModeHeader(
    isArabic: Boolean,
    onBack: () -> Unit,
    hideChecked: Boolean,
    onToggleHideChecked: () -> Unit,
    onFinishShopping: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .size(42.dp)
                    .testTag("supermarket_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isArabic) "وضع السوبرماركت" else "Supermarket Mode",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    NeonBadge(
                        text = "LIVE HUD",
                        accentColor = EmeraldSavings
                    )
                }
                Text(
                    text = if (isArabic) "أزرار كبيرة للتحكم بيد واحدة أثناء التسوق" else "One-thumb reachability inside the aisles",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Filter Toggle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (hideChecked) ElectricCyan.copy(alpha = 0.25f) else Color(0x20FFFFFF))
                .border(
                    BorderStroke(1.dp, if (hideChecked) ElectricCyan else GlassBorder),
                    RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onToggleHideChecked)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (hideChecked) {
                    if (isArabic) "عرض الكل" else "Show All"
                } else {
                    if (isArabic) "إخفاء المكتمل" else "Hide Done"
                },
                color = if (hideChecked) ElectricCyan else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SupermarketItemCard(
    item: ShoppingItem,
    isArabic: Boolean,
    onToggleChecked: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkColor by animateColorAsState(
        targetValue = if (item.isChecked) CheckedGreen else Color.Transparent,
        label = "big_check_color"
    )

    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val cardBackground = if (item.isChecked) {
        if (isLight) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    } else {
        if (isLight) Color(0xFFFFFFFF) else Color(0xFF131D31)
    }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("supermarket_item_${item.id}"),
        backgroundColor = cardBackground,
        glowColor = if (item.isChecked) null else EmeraldSavings.copy(alpha = 0.4f),
        onClick = onToggleChecked
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Extra-Large 54dp High-Contrast Check Button (For effortless tapping in aisles)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(checkColor)
                    .border(
                        BorderStroke(
                            2.5.dp,
                            if (item.isChecked) CheckedGreen else Color(0x80FFFFFF)
                        ),
                        CircleShape
                    )
                    .clickable(onClick = onToggleChecked)
                    .testTag("supermarket_toggle_btn_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Oversized Title and Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) item.nameAr else item.name,
                    color = if (item.isChecked) TextMuted else TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        color = ElectricCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (item.estimatedPriceDzd > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• " + LocalizationUtil.formatDzd(item.estimatedPriceDzd * item.quantity, isArabic),
                            color = EmeraldSavings,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick quantity adjustments
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x331C283E))
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) {
                    Text("–", color = ElectricCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onIncrement),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = ElectricCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
