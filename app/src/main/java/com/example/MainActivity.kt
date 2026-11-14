package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import com.example.ui.BaseActivity
import com.example.util.ApkDownloadState
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
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
import com.example.ui.components.AppTutorialOverlay
import com.example.ui.components.AppUpdateOverlay
import com.example.ui.components.FluidWaterState
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LocalPagerSwipeBlockController
import com.example.ui.components.QrShareDialog
import com.example.ui.components.fluidWaterDistortion
import com.example.ui.components.fluidWaterTouchPhysics
import com.example.ui.components.rememberFluidWaterState
import com.example.ui.components.rememberPagerSwipeBlockController
import com.example.ui.components.twoFingerFullScreenSwipe
import com.example.util.AppUpdateStatus
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PriceComparisonScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SupermarketModeScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.LightCardSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.PeachTintText
import com.example.ui.theme.ShopyPulseTheme
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.ui.viewmodel.ShoppingViewModelFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = ShoppingRepository(database.shoppingDao(), database.marketPriceDao(), database.spendingDao())
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val factory = ShoppingViewModelFactory(repository, preferencesRepository)
        viewModel = ViewModelProvider(this, factory)[ShoppingViewModel::class.java]

        setContent {
            val isDarkModePref by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val isAutoTheme by viewModel.isAutoThemeEnabled.collectAsStateWithLifecycle()
            val systemInDark = isSystemInDarkTheme()
            val effectiveDarkMode = if (isAutoTheme) systemInDark else isDarkModePref
            val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
            val isFullScreen by viewModel.isFullScreenEnabled.collectAsStateWithLifecycle()

            // Dynamic Layout Direction: RTL (من اليمين لليسار) for Arabic, LTR for English
            val currentLayoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            LaunchedEffect(effectiveDarkMode) {
                enableEdgeToEdge(
                    statusBarStyle = if (effectiveDarkMode) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (effectiveDarkMode) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isStatusBarContrastEnforced = false
                    window.isNavigationBarContrastEnforced = false
                }
            }

            // Synchronize native window decorView direction dynamically
            LaunchedEffect(isArabic) {
                window.decorView.layoutDirection = if (isArabic) {
                    android.view.View.LAYOUT_DIRECTION_RTL
                } else {
                    android.view.View.LAYOUT_DIRECTION_LTR
                }
            }

            // Hide or show the phone's top status bar dynamically when Full Screen mode toggles
            LaunchedEffect(isFullScreen) {
                applyFullScreenStatusBar(isFullScreen)
            }

            ShopyPulseTheme(darkTheme = effectiveDarkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
                    ShopyPulseApp(
                        viewModel = viewModel,
                        isDarkMode = effectiveDarkMode,
                        isFullScreen = isFullScreen
                    )
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && ::viewModel.isInitialized) {
            applyFullScreenStatusBar(viewModel.isFullScreenEnabled.value)
        }
    }

    private fun applyFullScreenStatusBar(enabled: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val lp = window.attributes
            if (lp.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS) {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                window.attributes = lp
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            if (lp.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES) {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = lp
            }
        }
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (enabled) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    override fun onStart() {
        super.onStart()
        if (::viewModel.isInitialized) {
            viewModel.checkForUpdatesOnAppOpen()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            applyFullScreenStatusBar(viewModel.isFullScreenEnabled.value)
            if (viewModel.apkDownloadState.value is ApkDownloadState.RequiresInstallPermission &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls())
            ) {
                viewModel.installDownloadedApk(this)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShopyPulseApp(
    viewModel: ShoppingViewModel,
    isDarkMode: Boolean = true,
    isFullScreen: Boolean = false
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.userMessage.collect { msg ->
            if (msg.isNotBlank()) {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Lock the top status-bar space permanently for this orientation so toggling Full Screen ON or OFF
    // only shows/hides the system bar over the background without moving any screen content.
    val visibleStatusTopPx = WindowInsets.statusBars.getTop(density)
    val stableStatusTopPx = WindowInsets.statusBarsIgnoringVisibility.getTop(density)
    val cutoutTopPx = WindowInsets.displayCutout.getTop(density)
    val fallbackStatusBarTopPx = remember(context, configuration.orientation) {
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) context.resources.getDimensionPixelSize(resId) else 0
    }
    var cachedStatusBarTopPx by remember(configuration.orientation) { mutableIntStateOf(0) }
    val resolvedMeasuredTopPx = maxOf(visibleStatusTopPx, stableStatusTopPx, cutoutTopPx)
    if (resolvedMeasuredTopPx > cachedStatusBarTopPx) {
        cachedStatusBarTopPx = resolvedMeasuredTopPx
    }
    val effectiveTopPx = if (cachedStatusBarTopPx > 0) {
        cachedStatusBarTopPx
    } else {
        fallbackStatusBarTopPx
    }
    val preservedTopPaddingDp = with(density) { effectiveTopPx.toDp() }
    val currentScreenName by viewModel.currentScreenName.collectAsStateWithLifecycle()
    val currentScreen = remember(currentScreenName) {
        Screen.entries.firstOrNull { it.name == currentScreenName } ?: Screen.HOME
    }
    var showQrShareDialog by remember { mutableStateOf(false) }
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val isFluidWaterEnabled by viewModel.isLiquidDockSwipeEnabled.collectAsStateWithLifecycle()
    val isTwoFingerFullScreenSwipeEnabled by viewModel.isTwoFingerFullScreenSwipeEnabled.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    val showUpdateOverlay by viewModel.showUpdateOverlay.collectAsStateWithLifecycle()
    val apkDownloadState by viewModel.apkDownloadState.collectAsStateWithLifecycle()
    val showTutorialOverlay by viewModel.showTutorialOverlay.collectAsStateWithLifecycle()
    var lastAutoInstallPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(apkDownloadState) {
        val downloaded = apkDownloadState as? ApkDownloadState.Downloaded ?: return@LaunchedEffect
        val path = downloaded.apkFile.absolutePath
        if (lastAutoInstallPath != path) {
            lastAutoInstallPath = path
            viewModel.installDownloadedApk(context, downloaded.apkFile)
        }
    }

    // Primary ViewPager screens (Home <-> Products <-> Settings)
    val pagerScreens = remember { listOf(Screen.HOME, Screen.PRICES, Screen.SETTINGS) }
    val initialPageIndex = remember { pagerScreens.indexOf(currentScreen).coerceAtLeast(0) }
    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { pagerScreens.size }
    )
    val springSnapSpec = remember {
        spring<Float>(
            dampingRatio = 0.86f,
            stiffness = Spring.StiffnessMediumLow
        )
    }
    val pagerFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = springSnapSpec
    )

    // Interactive 60-90 FPS Fluid Water Physics engine (scoped strictly to the bottom dock)
    val dockWaterState = rememberFluidWaterState(enabled = isFluidWaterEnabled)
    val pagerSwipeBlockController = rememberPagerSwipeBlockController()

    // Keep ViewModel screen persisted when user finishes swiping via HorizontalPager
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                val targetScreen = pagerScreens.getOrNull(settledPage) ?: return@collect
                val activeScreen = Screen.entries.firstOrNull { it.name == viewModel.currentScreenName.value }
                if (activeScreen in pagerScreens && activeScreen != targetScreen) {
                    viewModel.setCurrentScreen(targetScreen.name)
                }
            }
    }

    // Feed continuous HorizontalPager swipe velocity into the dock water surface current shader
    LaunchedEffect(pagerState, isFluidWaterEnabled) {
        if (!isFluidWaterEnabled) return@LaunchedEffect
        snapshotFlow { pagerState.currentPageOffsetFraction }
            .collect { offsetFraction ->
                dockWaterState.updatePageScrollVelocity(offsetFraction)
            }
    }

    // Animate pager smoothly with spring physics whenever a primary screen is selected from dock or back button
    LaunchedEffect(currentScreen) {
        val targetIndex = pagerScreens.indexOf(currentScreen)
        if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
            pagerState.animateScrollToPage(
                page = targetIndex,
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    // Handle back button when on sub-screens or non-Home tabs
    BackHandler(enabled = currentScreen != Screen.HOME) {
        viewModel.setCurrentScreen(Screen.HOME.name)
    }

    LiquidGlassBackground(isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.navigationBars,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (isDarkMode) Color(0xFF162235) else Color(0xFF0F172A),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            },
            bottomBar = {
                if (currentScreen != Screen.SUPERMARKET) {
                    val activeDockScreen = pagerScreens.getOrNull(pagerState.currentPage) ?: currentScreen
                    LiquidGlassBottomNavBar(
                        currentScreen = if (currentScreen in pagerScreens) activeDockScreen else currentScreen,
                        isArabic = isArabic,
                        isDarkMode = isDarkMode,
                        waterState = dockWaterState,
                        isFluidWaterEnabled = isFluidWaterEnabled,
                        onSelectScreen = { targetScreen ->
                            viewModel.setCurrentScreen(targetScreen.name)
                            val idx = pagerScreens.indexOf(targetScreen)
                            if (idx >= 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = idx,
                                        animationSpec = spring(
                                            dampingRatio = 0.86f,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .twoFingerFullScreenSwipe(
                    enabled = isTwoFingerFullScreenSwipeEnabled,
                    onSwipeUp = {
                        if (!isFullScreen) {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (_: Exception) {}
                            viewModel.setFullScreenEnabled(true)
                            viewModel.emitUserMessage(
                                if (isArabic) "تم تفعيل وضع ملء الشاشة" else "Full Screen Enabled"
                            )
                        }
                    },
                    onSwipeDown = {
                        if (isFullScreen) {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (_: Exception) {}
                            viewModel.setFullScreenEnabled(false)
                            viewModel.emitUserMessage(
                                if (isArabic) "تم إيقاف وضع ملء الشاشة" else "Full Screen Disabled"
                            )
                        }
                    }
                )
                .padding(top = preservedTopPaddingDp)
        ) { innerPadding ->
            CompositionLocalProvider(
                LocalPagerSwipeBlockController provides pagerSwipeBlockController
            ) {
                val showMainPager = currentScreen != Screen.SUPERMARKET && currentScreen != Screen.STATISTICS
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Fluid HorizontalPager (hidden immediately when Cart/Supermarket or Statistics opens so screens never overlap)
                    AnimatedVisibility(
                        visible = showMainPager,
                        enter = fadeIn(tween(180)),
                        exit = ExitTransition.None
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 2,
                            flingBehavior = pagerFlingBehavior,
                            userScrollEnabled = !pagerSwipeBlockController.isBlocked,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("fluid_water_view_pager")
                        ) { page ->
                            when (pagerScreens[page]) {
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

                                Screen.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { viewModel.setCurrentScreen(Screen.HOME.name) },
                                    onOpenQrShare = { showQrShareDialog = true }
                                )

                                else -> Unit
                            }
                        }
                    }

                    // Dedicated secondary screens (Supermarket Cart Mode & Statistics) with zero overlap over Home
                    AnimatedVisibility(
                        visible = currentScreen == Screen.SUPERMARKET,
                        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.98f, animationSpec = tween(200)),
                        exit = ExitTransition.None
                    ) {
                        SupermarketModeScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setCurrentScreen(Screen.HOME.name) }
                        )
                    }

                    AnimatedVisibility(
                        visible = currentScreen == Screen.STATISTICS,
                        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.98f, animationSpec = tween(200)),
                        exit = ExitTransition.None
                    ) {
                        StatisticsScreen(
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

        // Compact Luxury In-App Update Card (shown automatically when a newer update is detected)
        val availableUpdate = updateStatus as? AppUpdateStatus.UpdateAvailable
        AnimatedVisibility(
            visible = showUpdateOverlay && availableUpdate != null,
            enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220)),
            exit = fadeOut(tween(170))
        ) {
            if (availableUpdate != null) {
                AppUpdateOverlay(
                    updateInfo = availableUpdate,
                    apkDownloadState = apkDownloadState,
                    isArabic = isArabic,
                    onDismiss = { viewModel.dismissUpdateOverlay() },
                    onUpdateClick = { url ->
                        viewModel.startInAppUpdateDownload(
                            context = context,
                            url = url,
                            versionLabel = availableUpdate.latestVersion
                        )
                    },
                    onInstallClick = { viewModel.installDownloadedApk(context) },
                    onCancelDownload = { viewModel.cancelUpdateDownload() }
                )
            }
        }

        // Luxury Interactive App Tutorial Overlay
        AnimatedVisibility(
            visible = showTutorialOverlay && !(showUpdateOverlay && availableUpdate != null),
            enter = fadeIn(tween(240)) + scaleIn(initialScale = 0.95f, animationSpec = tween(240)),
            exit = fadeOut(tween(180))
        ) {
            AppTutorialOverlay(
                isArabic = isArabic,
                isDarkMode = isDarkMode,
                onClose = { viewModel.completeTutorial() }
            )
        }
    }
}

@Composable
fun LiquidGlassBottomNavBar(
    currentScreen: Screen,
    isArabic: Boolean,
    isDarkMode: Boolean = true,
    waterState: FluidWaterState,
    isFluidWaterEnabled: Boolean,
    onSelectScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val dockScreens = remember { listOf(Screen.HOME, Screen.PRICES, Screen.SETTINGS) }
    val latestCurrentScreen by rememberUpdatedState(currentScreen)
    val latestOnSelectScreen by rememberUpdatedState(onSelectScreen)

    val dockBackground by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFF0E1626) else LightCardSurface,
        animationSpec = tween(durationMillis = 280),
        label = "dock_bg_color"
    )
    val dockBorderColor by animateColorAsState(
        targetValue = if (isDarkMode) Color(0x33475569) else LightCardBorder,
        animationSpec = tween(durationMillis = 280),
        label = "dock_border_color"
    )
    val unselectedTint = if (isDarkMode) Color(0xFF64748B) else TextSecondaryLight
    val homeAccent = if (isDarkMode) ElectricCyan else ElectricCyanDark
    val pricesAccent = if (isDarkMode) EmeraldSavings else EmeraldDark
    val settingsAccent = if (isDarkMode) NeonAmber else PeachTintText

    // Dynamic Layout Direction: Mirrors automatically according to language (RTL for Arabic, LTR for English)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("liquid_glass_bottom_nav"),
        contentAlignment = Alignment.Center
    ) {
        // Floating Capsule Dock adapting cleanly between Dark & Light themes with Fluid Water Physics
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!isDarkMode) {
                        Modifier.shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color(0x260F172A),
                            spotColor = Color(0x260F172A)
                        )
                    } else {
                        Modifier
                    }
                )
                .clip(RoundedCornerShape(32.dp))
                .background(dockBackground)
                .border(
                    BorderStroke(1.dp, dockBorderColor),
                    RoundedCornerShape(32.dp)
                )
                .fluidWaterTouchPhysics(
                    state = waterState,
                    enabled = isFluidWaterEnabled
                )
                .fluidWaterDistortion(
                    state = waterState,
                    enabled = isFluidWaterEnabled,
                    isDarkMode = isDarkMode
                )
                .pointerInput(isArabic) {
                    var dragStartIndex = 0
                    var lastEmittedIndex = 0
                    var cumulativeDragX = 0f

                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragStartIndex = dockScreens.indexOf(latestCurrentScreen).coerceAtLeast(0)
                            lastEmittedIndex = dragStartIndex
                            cumulativeDragX = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            cumulativeDragX += dragAmount
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            val xFraction = (change.position.x / width).coerceIn(0f, 0.999f)
                            val visualZone = (xFraction * dockScreens.size).toInt().coerceIn(0, dockScreens.lastIndex)
                            val targetIndex = if (isArabic) dockScreens.lastIndex - visualZone else visualZone
                            if (targetIndex != lastEmittedIndex) {
                                lastEmittedIndex = targetIndex
                                latestOnSelectScreen(dockScreens[targetIndex])
                            }
                        },
                        onDragEnd = {
                            val quickSwipePx = 30.dp.toPx()
                            if (lastEmittedIndex == dragStartIndex && abs(cumulativeDragX) >= quickSwipePx) {
                                val step = if (isArabic) {
                                    if (cumulativeDragX > 0f) -1 else 1
                                } else {
                                    if (cumulativeDragX > 0f) 1 else -1
                                }
                                val targetIndex = (dragStartIndex + step).coerceIn(0, dockScreens.lastIndex)
                                if (targetIndex != dragStartIndex) {
                                    latestOnSelectScreen(dockScreens[targetIndex])
                                }
                            }
                        }
                    )
                }
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
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
                    accentColor = homeAccent,
                    unselectedColor = unselectedTint,
                    onClick = { onSelectScreen(Screen.HOME) },
                    testTag = "nav_home"
                )

                // 2. Products & Price Comparison Tab
                NavItem(
                    icon = Icons.AutoMirrored.Filled.CompareArrows,
                    label = if (isArabic) "المنتجات" else "Products",
                    isSelected = currentScreen == Screen.PRICES,
                    accentColor = pricesAccent,
                    unselectedColor = unselectedTint,
                    onClick = { onSelectScreen(Screen.PRICES) },
                    testTag = "nav_products"
                )

                // 3. Settings Tab ("الإعدادات" on one single line in a stationary fixed position)
                NavItem(
                    icon = Icons.Default.Settings,
                    label = if (isArabic) "الإعدادات" else "Settings",
                    isSelected = currentScreen == Screen.SETTINGS,
                    accentColor = settingsAccent,
                    unselectedColor = unselectedTint,
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
    unselectedColor: Color,
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
                                .background(accentColor.copy(alpha = 0.16f))
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
                    tint = if (isSelected) accentColor else unselectedColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Fixed Position Label: Always stationary, in one line, fully visible without truncation
            Text(
                text = label,
                color = if (isSelected) accentColor else unselectedColor,
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
