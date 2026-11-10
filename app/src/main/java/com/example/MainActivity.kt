package com.example

import android.os.Bundle
import com.example.ui.BaseActivity
import com.example.util.LocaleHelper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.ShoppingRepository
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.QrShareDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PriceComparisonScreen
import com.example.ui.screens.ScannerScreen
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
    SCANNER,
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
        val factory = ShoppingViewModelFactory(repository)
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
}

@Composable
fun ShopyPulseApp(viewModel: ShoppingViewModel, isDarkMode: Boolean = true) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var showQrShareDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()

    // Handle back button when on sub-screens
    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    // Collect user notification toasts / snackbars
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LiquidGlassBackground(isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 80.dp)
                )
            },
            bottomBar = {
                if (currentScreen != Screen.SUPERMARKET) {
                    LiquidGlassBottomNavBar(
                        currentScreen = currentScreen,
                        isArabic = isArabic,
                        onSelectScreen = { currentScreen = it }
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
                            onNavigateToScanner = { currentScreen = Screen.SCANNER },
                            onNavigateToPriceComparison = { currentScreen = Screen.PRICES },
                            onNavigateToSupermarketMode = { currentScreen = Screen.SUPERMARKET },
                            onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                            onNavigateToStatistics = { currentScreen = Screen.STATISTICS },
                            onOpenQrShare = { showQrShareDialog = true }
                        )

                        Screen.SCANNER -> ScannerScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME }
                        )

                        Screen.PRICES -> PriceComparisonScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME }
                        )

                        Screen.SUPERMARKET -> SupermarketModeScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME }
                        )

                        Screen.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME },
                            onOpenQrShare = { showQrShareDialog = true }
                        )

                        Screen.STATISTICS -> StatisticsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.HOME }
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
    }
}

@Composable
fun LiquidGlassBottomNavBar(
    currentScreen: Screen,
    isArabic: Boolean,
    onSelectScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    // Forced LTR layout ensuring the Settings tab is permanently fixed on the FAR LEFT
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("liquid_glass_bottom_nav"),
            contentAlignment = Alignment.Center
        ) {
            // Floating Dark Capsule Pill (Neumorphic & Soft Glass)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color(0xFF0E1626))
                    .border(
                        BorderStroke(1.dp, Color(0x33475569)),
                        RoundedCornerShape(36.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Permanently Fixed on FAR LEFT: Settings Tab
                    NavItem(
                        icon = Icons.Default.Settings,
                        label = if (isArabic) "الإعدادات" else "Settings",
                        isSelected = currentScreen == Screen.SETTINGS,
                        accentColor = NeonAmber,
                        onClick = { onSelectScreen(Screen.SETTINGS) },
                        testTag = "nav_settings"
                    )

                    // 2. Shopping List Tab
                    NavItem(
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        label = if (isArabic) "القائمة" else "List",
                        isSelected = currentScreen == Screen.HOME,
                        accentColor = ElectricCyan,
                        onClick = { onSelectScreen(Screen.HOME) },
                        testTag = "nav_home"
                    )

                    // 3. Barcode Scanner Tab
                    NavItem(
                        icon = Icons.Default.QrCodeScanner,
                        label = if (isArabic) "الماسح" else "Scanner",
                        isSelected = currentScreen == Screen.SCANNER,
                        accentColor = ElectricCyan,
                        onClick = { onSelectScreen(Screen.SCANNER) },
                        testTag = "nav_scanner"
                    )

                    // 4. Products & Price Comparison Tab
                    NavItem(
                        icon = Icons.AutoMirrored.Filled.CompareArrows,
                        label = if (isArabic) "المنتجات" else "Products",
                        isSelected = currentScreen == Screen.PRICES,
                        accentColor = EmeraldSavings,
                        onClick = { onSelectScreen(Screen.PRICES) },
                        testTag = "nav_products"
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    if (isSelected) {
        // Active Pill (Bordered glow with icon and text, matching screenshots)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(accentColor.copy(alpha = 0.16f))
                .border(
                    BorderStroke(1.5.dp, accentColor),
                    RoundedCornerShape(24.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = accentColor),
                    onClick = onClick
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        // Inactive Icon (Sleek and minimalist, matching screenshots)
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onClick
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Backward compatibility alias
@Composable
fun SoukSmartApp(viewModel: ShoppingViewModel, isDarkMode: Boolean = true) {
    ShopyPulseApp(viewModel = viewModel, isDarkMode = isDarkMode)
}
