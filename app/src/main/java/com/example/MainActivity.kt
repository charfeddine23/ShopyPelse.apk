package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.example.ui.BaseActivity
import com.example.util.LocaleHelper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.ShoppingRepository
import com.example.ui.components.AppUpdateOverlay
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.QrShareDialog
import com.example.util.AppUpdateStatus
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PriceComparisonScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SupermarketModeScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.ShopyPulseTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.ui.viewmodel.ShoppingViewModelFactory

enum class Screen {
    HOME,
    STATISTICS,
    PRICES,
    SUPERMARKET,
    SETTINGS
}

class MainActivity : BaseActivity() {

    private lateinit var viewModel: ShoppingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = ShoppingRepository(database.shoppingDao(), database.marketPriceDao(), database.spendingDao())
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val factory = ShoppingViewModelFactory(repository, preferencesRepository)
        viewModel = ViewModelProvider(this, factory)[ShoppingViewModel::class.java]

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()

            // Dynamic Layout Direction: RTL (من اليمين لليسار) for Arabic, LTR for English
            val currentLayoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            // Synchronize native window decorView direction dynamically
            LaunchedEffect(isArabic) {
                window.decorView.layoutDirection = if (isArabic) {
                    android.view.View.LAYOUT_DIRECTION_RTL
                } else {
                    android.view.View.LAYOUT_DIRECTION_LTR
                }
            }

            ShopyPulseTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
                    ShopyPulseApp(viewModel = viewModel, isDarkMode = isDarkMode)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::viewModel.isInitialized) {
            viewModel.checkForUpdatesOnAppOpen()
        }
    }
}

@Composable
fun ShopyPulseApp(viewModel: ShoppingViewModel, isDarkMode: Boolean = true) {
    val context = LocalContext.current
    val currentScreenName by viewModel.currentScreenName.collectAsStateWithLifecycle()
    val currentScreen = remember(currentScreenName) {
        Screen.entries.firstOrNull { it.name == currentScreenName } ?: Screen.HOME
    }
    var showQrShareDialog by remember { mutableStateOf(false) }
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val isLiquidDockSwipeEnabled by viewModel.isLiquidDockSwipeEnabled.collectAsStateWithLifecycle()
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    val showUpdateOverlay by viewModel.showUpdateOverlay.collectAsStateWithLifecycle()

    // Handle back button when on sub-screens
    BackHandler(enabled = currentScreen != Screen.HOME) {
        viewModel.setCurrentScreen(Screen.HOME.name)
    }

    LiquidGlassBackground(isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentScreen != Screen.SUPERMARKET) {
                    LiquidGlassBottomNavBar(
                        currentScreen = currentScreen,
                        isArabic = isArabic,
                        isLiquidSwipeEnabled = isLiquidDockSwipeEnabled,
                        onSelectScreen = { viewModel.setCurrentScreen(it.name) }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToPriceComparison = { viewModel.setCurrentScreen(Screen.PRICES.name) },
                            onNavigateToSupermarketMode = { viewModel.setCurrentScreen(Screen.SUPERMARKET.name) },
                            onNavigateToSettings = { viewModel.setCurrentScreen(Screen.SETTINGS.name) },
                            onNavigateToStatistics = { viewModel.setCurrentScreen(Screen.STATISTICS.name) },
                            onOpenQrShare = { showQrShareDialog = true }
                        )

                        Screen.PRICES -> PriceComparisonScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setCurrentScreen(Screen.HOME.name) }
                        )

                        Screen.SUPERMARKET -> SupermarketModeScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setCurrentScreen(Screen.HOME.name) }
                        )

                        Screen.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setCurrentScreen(Screen.HOME.name) },
                            onOpenQrShare = { showQrShareDialog = true }
                        )

                        Screen.STATISTICS -> StatisticsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setCurrentScreen(Screen.HOME.name) }
                        )
                    }
                }
            }
        }

        // Offline QR Share Dialog
        if (showQrShareDialog) {
            QrShareDialog(
                payload = viewModel.getQrSharePayload(),
                isArabic = isArabic,
                onDismiss = { showQrShareDialog = false },
                onImportPayload = { payload ->
                    viewModel.importListFromQr(payload)
                }
            )
        }

        // Full-Screen In-App Update Overlay (shown automatically when a newer update is detected)
        val availableUpdate = updateStatus as? AppUpdateStatus.UpdateAvailable
        AnimatedVisibility(
            visible = showUpdateOverlay && availableUpdate != null,
            enter = fadeIn(tween(240)) + scaleIn(initialScale = 0.96f, animationSpec = tween(240)),
            exit = fadeOut(tween(180))
        ) {
            if (availableUpdate != null) {
                AppUpdateOverlay(
                    updateInfo = availableUpdate,
                    isArabic = isArabic,
                    onDismiss = { viewModel.dismissUpdateOverlay() },
                    onUpdateClick = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LiquidGlassBottomNavBar(
    currentScreen: Screen,
    isArabic: Boolean,
    onSelectScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    isLiquidSwipeEnabled: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()
    val screens = remember { listOf(Screen.HOME, Screen.PRICES, Screen.SETTINGS) }

    // Dynamic swipe gesture & liquid droplet animation state
    var touchX by remember { mutableFloatStateOf(0f) }
    var dragAccumulated by remember { mutableFloatStateOf(0f) }
    val dropletAlpha = remember { Animatable(0f) }
    val dropletScale = remember { Animatable(0f) }

    // Dynamic Layout Direction: Mirrors automatically according to language (RTL for Arabic, LTR for English)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("liquid_glass_bottom_nav"),
        contentAlignment = Alignment.Center
    ) {
        // Floating Dark Capsule Pill (Neumorphic & Soft Glass)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF0E1626))
                .border(
                    BorderStroke(1.dp, Color(0x33475569)),
                    RoundedCornerShape(32.dp)
                )
                .then(
                    if (isLiquidSwipeEnabled) {
                        Modifier.pointerInput(currentScreen, isArabic) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    touchX = offset.x
                                    dragAccumulated = 0f
                                    coroutineScope.launch {
                                        dropletAlpha.snapTo(1f)
                                        dropletScale.snapTo(0.6f)
                                        dropletScale.animateTo(1f, tween(150))
                                    }
                                },
                                onDragEnd = {
                                    coroutineScope.launch {
                                        dropletAlpha.animateTo(0f, tween(240))
                                        dropletScale.snapTo(0f)
                                    }
                                    dragAccumulated = 0f
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        dropletAlpha.animateTo(0f, tween(200))
                                        dropletScale.snapTo(0f)
                                    }
                                    dragAccumulated = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    touchX = change.position.x
                                    dragAccumulated += dragAmount

                                    // Switch threshold: ~36dp
                                    val thresholdPx = 36.dp.toPx()
                                    if (abs(dragAccumulated) >= thresholdPx) {
                                        val currentIndex = screens.indexOf(currentScreen).coerceAtLeast(0)
                                        // Calculate step according to RTL/LTR layout
                                        val step = if (dragAccumulated > 0) {
                                            if (isArabic) -1 else 1
                                        } else {
                                            if (isArabic) 1 else -1
                                        }
                                        val targetIndex = (currentIndex + step).coerceIn(0, screens.lastIndex)
                                        if (targetIndex != currentIndex) {
                                            onSelectScreen(screens[targetIndex])
                                        }
                                        dragAccumulated = 0f
                                    }
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            // Liquid Glass Canvas Layer (Renders bubble and morphing teardrop droplet with tail)
            if (isLiquidSwipeEnabled && dropletAlpha.value > 0.01f) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(30.dp))
                ) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f
                    val clampedTouchX = touchX.coerceIn(24.dp.toPx(), w - 24.dp.toPx())
                    val stretchFactor = (abs(dragAccumulated) / 45.dp.toPx()).coerceIn(0f, 1f)
                    val dir = if (dragAccumulated >= 0f) 1f else -1f
                    val alpha = dropletAlpha.value
                    val scale = dropletScale.value

                    val baseRadius = 18.dp.toPx() * scale
                    val headR = baseRadius * (1f - stretchFactor * 0.15f)
                    val tailLength = stretchFactor * 26.dp.toPx() * scale

                    // Head of the bubble / droplet
                    val headX = clampedTouchX - dir * (stretchFactor * 6.dp.toPx())
                    // Tail extending along the swipe direction
                    val tailX = headX + dir * tailLength
                    val tailR = (5.dp.toPx() * (1f - stretchFactor * 0.4f) * scale).coerceAtLeast(2.dp.toPx())

                    val dropletPath = Path()
                    if (stretchFactor < 0.1f) {
                        // Stationary Liquid Glass Bubble
                        dropletPath.addOval(
                            Rect(
                                center = Offset(clampedTouchX, centerY),
                                radius = headR
                            )
                        )
                    } else {
                        // Morphing Teardrop Droplet with tail pointing in drag direction
                        val topControlX = (headX * 0.45f + tailX * 0.55f)
                        val bottomControlX = topControlX

                        dropletPath.moveTo(tailX, centerY - tailR)
                        dropletPath.quadraticTo(
                            topControlX, centerY - headR * 1.05f,
                            headX, centerY - headR
                        )
                        dropletPath.arcTo(
                            rect = Rect(
                                left = headX - headR,
                                top = centerY - headR,
                                right = headX + headR,
                                bottom = centerY + headR
                            ),
                            startAngleDegrees = -90f,
                            sweepAngleDegrees = -180f * dir,
                            forceMoveTo = false
                        )
                        dropletPath.quadraticTo(
                            bottomControlX, centerY + headR * 1.05f,
                            tailX, centerY + tailR
                        )
                        dropletPath.close()
                    }

                    // 1. Ambient Glow behind droplet
                    drawPath(
                        path = dropletPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricCyan.copy(alpha = 0.40f * alpha),
                                EmeraldSavings.copy(alpha = 0.20f * alpha),
                                Color.Transparent
                            ),
                            center = Offset(headX, centerY),
                            radius = headR * 2.5f
                        )
                    )

                    // 2. Liquid Glass Fill
                    drawPath(
                        path = dropletPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ElectricCyan.copy(alpha = 0.35f * alpha),
                                EmeraldSavings.copy(alpha = 0.25f * alpha),
                                Color(0x3300E5FF).copy(alpha = 0.20f * alpha)
                            ),
                            start = Offset(headX - headR, centerY - headR),
                            end = Offset(tailX, centerY + headR)
                        )
                    )

                    // 3. Crisp Glass Edge Refraction Stroke
                    drawPath(
                        path = dropletPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.75f * alpha),
                                ElectricCyan.copy(alpha = 0.85f * alpha),
                                Color.Transparent
                            )
                        ),
                        style = Stroke(width = 1.6.dp.toPx())
                    )

                    // 4. Liquid Glass Specular Highlight Bubble Reflection
                    drawCircle(
                        color = Color.White.copy(alpha = 0.55f * alpha),
                        radius = (headR * 0.30f).coerceAtLeast(1.dp.toPx()),
                        center = Offset(headX - dir * 3.dp.toPx(), centerY - headR * 0.35f)
                    )
                }
            }

            // Foreground Tab Icons & Stationary Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Shopping List Tab
                NavItem(
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    label = if (isArabic) "القائمة" else "List",
                    isSelected = currentScreen == Screen.HOME,
                    accentColor = ElectricCyan,
                    onClick = { onSelectScreen(Screen.HOME) },
                    testTag = "nav_home"
                )

                // 2. Products & Price Comparison Tab
                NavItem(
                    icon = Icons.AutoMirrored.Filled.CompareArrows,
                    label = if (isArabic) "المنتجات" else "Products",
                    isSelected = currentScreen == Screen.PRICES,
                    accentColor = EmeraldSavings,
                    onClick = { onSelectScreen(Screen.PRICES) },
                    testTag = "nav_products"
                )

                // 3. Settings Tab ("الإعدادات" on one single line in a stationary fixed position)
                NavItem(
                    icon = Icons.Default.Settings,
                    label = if (isArabic) "الإعدادات" else "Settings",
                    isSelected = currentScreen == Screen.SETTINGS,
                    accentColor = NeonAmber,
                    onClick = { onSelectScreen(Screen.SETTINGS) },
                    testTag = "nav_settings"
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accentColor),
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Pill Container (Glows when selected, stays fixed in place)
            Box(
                modifier = Modifier
                    .size(width = 46.dp, height = 28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(accentColor.copy(alpha = 0.18f))
                                .border(BorderStroke(1.2.dp, accentColor), RoundedCornerShape(14.dp))
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) accentColor else Color(0xFF64748B),
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Fixed Position Label: Always stationary, in one line, fully visible without truncation
            Text(
                text = label,
                color = if (isSelected) accentColor else Color(0xFF64748B),
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = (-0.2).sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}

// Backward compatibility alias
@Composable
fun SoukSmartApp(viewModel: ShoppingViewModel, isDarkMode: Boolean = true) {
    ShopyPulseApp(viewModel = viewModel, isDarkMode = isDarkMode)
}
