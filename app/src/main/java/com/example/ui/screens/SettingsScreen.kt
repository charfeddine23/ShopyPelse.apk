package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CircularRevealThemeOverlay
import com.example.ui.components.QrShareDialog
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CyanTintBg
import com.example.ui.theme.CyanTintBorder
import com.example.ui.theme.CyanTintText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.PeachTintBg
import com.example.ui.theme.PeachTintBorder
import com.example.ui.theme.PeachTintText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.BuildConfig
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.util.ApkDownloadState
import com.example.util.AppUpdateStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Production-ready Settings Screen for 'Shopy Pulse' matching the Dark Slate
 * Neumorphic & Soft Glass Engine design system with custom glowing switch toggles,
 * section headers, and modular architecture.
 */
@Composable
fun SettingsScreen(
    viewModel: ShoppingViewModel,
    onBack: () -> Unit = {},
    onOpenQrShare: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val isDarkModePref by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isAutoThemeEnabled by viewModel.isAutoThemeEnabled.collectAsStateWithLifecycle()
    val systemInDark = isSystemInDarkTheme()
    val isEffectiveDarkMode = if (isAutoThemeEnabled) systemInDark else isDarkModePref
    val lastBackupDate by viewModel.lastBackupDate.collectAsStateWithLifecycle()
    val isDynamicGlowEnabled by viewModel.isDynamicGlowEnabled.collectAsStateWithLifecycle()
    val isLiquidDockSwipeEnabled by viewModel.isLiquidDockSwipeEnabled.collectAsStateWithLifecycle()
    val isFullScreenEnabled by viewModel.isFullScreenEnabled.collectAsStateWithLifecycle()
    val isTwoFingerFullScreenSwipeEnabled by viewModel.isTwoFingerFullScreenSwipeEnabled.collectAsStateWithLifecycle()
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    val apkDownloadState by viewModel.apkDownloadState.collectAsStateWithLifecycle()

    var showQrDialog by remember { mutableStateOf(false) }
    var qrDialogInitialMode by remember { mutableStateOf(false) }
    var showDriveDialog by remember { mutableStateOf(false) }
    val ioScope = rememberCoroutineScope()

    // Circular Reveal animation states for Light/Dark/Auto mode transition
    var revealTargetDark by remember { mutableStateOf<Boolean?>(null) }
    var revealTargetAuto by remember { mutableStateOf<Boolean?>(null) }
    var switchCenterOffset by remember { mutableStateOf(Offset.Zero) }

    // Storage Access Framework Launcher to save backup file (Google Drive / local)
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            ioScope.launch {
                val payload = viewModel.generateGoogleDriveBackupPayload()
                val saved = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(payload.toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                        } ?: error("Output stream unavailable")
                    }.isSuccess
                }
                if (saved) {
                    viewModel.markGoogleDriveBackupDone()
                } else {
                    viewModel.emitUserMessage(
                        if (isArabic) "تعذر حفظ ملف النسخ الاحتياطي" else "Unable to save backup file"
                    )
                }
            }
        }
    }

    // Storage Access Framework Launcher to restore backup file (Google Drive / local)
    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            ioScope.launch {
                val readResult = withContext(Dispatchers.IO) {
                    runCatching {
                        val maxBytes = 2 * 1024 * 1024
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val buffer = ByteArray(8192)
                            val out = ByteArrayOutputStream()
                            var totalRead = 0
                            while (true) {
                                val read = inputStream.read(buffer)
                                if (read == -1) break
                                totalRead += read
                                if (totalRead > maxBytes) {
                                    throw IllegalArgumentException("Backup file too large")
                                }
                                out.write(buffer, 0, read)
                            }
                            out.toString(Charsets.UTF_8.name())
                        }
                    }.getOrNull()
                }
                if (readResult != null) {
                    viewModel.restoreFromBackupPayload(readResult)
                } else {
                    viewModel.emitUserMessage(
                        if (isArabic) "ملف النسخة الاحتياطية غير صالح أو كبير جداً" else "Backup file is invalid or too large"
                    )
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header (without back arrow, title strictly on one line)
            item {
                SettingsScreenHeader(
                    isArabic = isArabic
                )
            }

            // 1. Language Selection Section (clean, compact, no large container)
            item {
                LanguageSelectionSection(
                    isArabic = isArabic,
                    onLanguageSelect = { arabicSelected ->
                        viewModel.setLanguage(arabicSelected)
                    }
                )
            }

            // 2. Theme Mode Section (with Custom Glowing Switch Toggle + Auto System Sync Button)
            item {
                ThemeModeSection(
                    isDarkMode = isEffectiveDarkMode,
                    isAutoTheme = isAutoThemeEnabled,
                    isArabic = isArabic,
                    onDarkModeChange = { darkSelected ->
                        if (revealTargetDark == null) {
                            if (darkSelected != isEffectiveDarkMode) {
                                revealTargetAuto = false
                                revealTargetDark = darkSelected
                            } else {
                                viewModel.setAutoThemeEnabled(false)
                                viewModel.setDarkMode(darkSelected)
                            }
                        }
                    },
                    onAutoToggle = {
                        if (revealTargetDark == null) {
                            val nextAuto = !isAutoThemeEnabled
                            if (nextAuto) {
                                if (systemInDark != isEffectiveDarkMode) {
                                    revealTargetAuto = true
                                    revealTargetDark = systemInDark
                                } else {
                                    viewModel.setAutoThemeEnabled(true)
                                }
                            } else {
                                viewModel.setDarkMode(isEffectiveDarkMode)
                                viewModel.setAutoThemeEnabled(false)
                            }
                        }
                    },
                    onSwitchPositionChanged = { center ->
                        switchCenterOffset = center
                    }
                )
            }

            // 2b. Dynamic App Title Glow Effect Toggle
            item {
                DynamicGlowToggleSection(
                    isEnabled = isDynamicGlowEnabled,
                    isArabic = isArabic,
                    onToggleChange = { enabled ->
                        viewModel.setDynamicGlowEnabled(enabled)
                    }
                )
            }

            // 2c. Liquid Glass Dock Swipe Effect Toggle
            item {
                LiquidDockSwipeToggleSection(
                    isEnabled = isLiquidDockSwipeEnabled,
                    isArabic = isArabic,
                    onToggleChange = { enabled ->
                        viewModel.setLiquidDockSwipeEnabled(enabled)
                    }
                )
            }

            // 2d. Full Screen Mode Toggle (Hide Top Phone Status Bar)
            item {
                FullScreenToggleSection(
                    isEnabled = isFullScreenEnabled,
                    isArabic = isArabic,
                    onToggleChange = { enabled ->
                        viewModel.setFullScreenEnabled(enabled)
                    }
                )
            }

            // 2e. Two-Finger Swipe Full Screen Gesture Toggle
            item {
                TwoFingerFullScreenSwipeToggleSection(
                    isEnabled = isTwoFingerFullScreenSwipeEnabled,
                    isArabic = isArabic,
                    onToggleChange = { enabled ->
                        viewModel.setTwoFingerFullScreenSwipeEnabled(enabled)
                    }
                )
            }

            // 3. QR Code Offline Sync Section (single clean action button to transfer)
            item {
                QrCodeOfflineSyncSection(
                    isArabic = isArabic,
                    onOpenTransfer = {
                        qrDialogInitialMode = false
                        showQrDialog = true
                    }
                )
            }

            // 4. Google Drive Cloud Backup Section (single clean action button to drive)
            item {
                GoogleDriveBackupSection(
                    isArabic = isArabic,
                    onOpenDrive = {
                        showDriveDialog = true
                    }
                )
            }

            // 4b. In-App Update Section
            item {
                AppUpdateSection(
                    isArabic = isArabic,
                    updateStatus = updateStatus,
                    apkDownloadState = apkDownloadState,
                    onOpenUpdate = {
                        when {
                            apkDownloadState is ApkDownloadState.Downloaded ||
                                apkDownloadState is ApkDownloadState.RequiresInstallPermission -> {
                                viewModel.installDownloadedApk(context)
                            }
                            updateStatus is AppUpdateStatus.UpdateAvailable -> {
                                viewModel.openUpdateOverlayIfAvailable()
                            }
                            else -> {
                                viewModel.checkForUpdates(autoOpenOverlay = true)
                            }
                        }
                    }
                )
            }

            // 4c. Interactive App Tutorial Guide
            item {
                SingleActionNavButton(
                    icon = Icons.Default.AutoAwesome,
                    title = if (isArabic) "الدليل التفاعلي لشرح التطبيق" else "Interactive App Tutorial",
                    accentColor = ElectricCyan,
                    onClick = { viewModel.openTutorial() },
                    testTag = "open_tutorial_button"
                )
            }

            // 5. App & Developer Info Section ("Developed by Kahlessenane Charfeddine")
            item {
                DeveloperInfoSection(
                    isArabic = isArabic
                )
            }

            // Extra bottom spacing for Floating Bottom Navigation Bar
            item {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }

        // Dedicated Offline QR Share & Import Dialog
        if (showQrDialog) {
            QrShareDialog(
                payload = viewModel.getQrSharePayload(),
                isArabic = isArabic,
                initialImportMode = qrDialogInitialMode,
                onDismiss = { showQrDialog = false },
                onImportPayload = { payload ->
                    viewModel.importListFromQr(payload)
                }
            )
        }

        // Dedicated Google Drive Backup & Restore Dialog
        if (showDriveDialog) {
            GoogleDriveDialog(
                isArabic = isArabic,
                lastBackupDate = lastBackupDate,
                onDismiss = { showDriveDialog = false },
                onBackup = {
                    createDocLauncher.launch("ShopyPulse_Backup_${System.currentTimeMillis()}.json")
                },
                onRestore = {
                    openDocLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                }
            )
        }

        // Circular Reveal Transition Animation Overlay for Light/Dark/Auto Mode Switch
        val pendingTargetDark = revealTargetDark
        if (pendingTargetDark != null) {
            val pendingTargetAuto = revealTargetAuto
            CircularRevealThemeOverlay(
                isTargetDark = pendingTargetDark,
                center = switchCenterOffset,
                onMiddlePoint = {
                    if (pendingTargetAuto != null) {
                        viewModel.setAutoThemeEnabled(pendingTargetAuto)
                    }
                    viewModel.setDarkMode(pendingTargetDark)
                },
                onAnimationEnd = {
                    revealTargetDark = null
                    revealTargetAuto = null
                }
            )
        }
    }
}

/**
 * Screen Top Header without Back Arrow, title strictly on one line.
 */
@Composable
private fun SettingsScreenHeader(
    isArabic: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Text(
            text = if (isArabic) "الإعدادات" else "Settings",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.3).sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}

/**
 * 1. Language Selection Section (compact, no large outer card)
 */
@Composable
private fun LanguageSelectionSection(
    isArabic: Boolean,
    onLanguageSelect: (Boolean) -> Unit
) {
    Column {
        SectionHeader(
            icon = Icons.Default.Translate,
            title = if (isArabic) "إعدادات اللغة" else "Language Selection",
            accentColor = ElectricCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactLanguagePill(
                title = "العربية",
                isSelected = isArabic,
                onClick = { onLanguageSelect(true) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("language_arabic")
            )

            CompactLanguagePill(
                title = "English",
                isSelected = !isArabic,
                onClick = { onLanguageSelect(false) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("language_english")
            )
        }
    }
}

@Composable
private fun CompactLanguagePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val pillBg = if (isLight) {
        if (isSelected) CyanTintBg else Color.White
    } else {
        if (isSelected) Color(0x2800E5FF) else Color(0x181E293B)
    }
    val pillBorder = if (isLight) {
        if (isSelected) BorderStroke(1.5.dp, CyanTintBorder) else BorderStroke(1.dp, LightCardBorder)
    } else {
        if (isSelected) BorderStroke(1.5.dp, ElectricCyan) else BorderStroke(1.dp, Color(0x33475569))
    }
    val titleColor = if (isLight) {
        if (isSelected) CyanTintText else TextPrimary
    } else {
        if (isSelected) ElectricCyan else TextPrimary
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(pillBg)
            .border(pillBorder, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = ElectricCyan),
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 14.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = titleColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 2. Theme Mode Section
 * Includes custom glowing switch toggle for instant Night/Day mode switching
 * plus an Auto button alongside it for automatic system theme following.
 */
@Composable
private fun ThemeModeSection(
    isDarkMode: Boolean,
    isAutoTheme: Boolean,
    isArabic: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onAutoToggle: () -> Unit,
    onSwitchPositionChanged: (Offset) -> Unit = {}
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val accentColor = when {
        isAutoTheme -> EmeraldSavings
        isDarkMode -> ElectricCyan
        else -> NeonAmber
    }

    Column {
        SectionHeader(
            icon = if (isAutoTheme) Icons.Default.BrightnessAuto else if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
            title = if (isArabic) "المظهر والسمات" else "Theme & Mode",
            accentColor = NeonAmber
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = accentColor.copy(alpha = 0.30f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isAutoTheme -> Icons.Default.BrightnessAuto
                                isDarkMode -> Icons.Default.DarkMode
                                else -> Icons.Default.LightMode
                            },
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = when {
                                isAutoTheme && isArabic -> "تلقائي"
                                isAutoTheme -> "Auto"
                                isDarkMode && isArabic -> "الوضع الليلي"
                                isDarkMode -> "Night Mode"
                                isArabic -> "الوضع النهاري"
                                else -> "Day Mode"
                            },
                            color = TextPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isAutoTheme) {
                            Text(
                                text = if (isArabic) "حسب وضع الهاتف" else "Follows system",
                                color = EmeraldSavings,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Auto Button (48dp min accessible target with compact 32dp pill)
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = EmeraldSavings),
                                onClick = onAutoToggle
                            )
                            .testTag("theme_auto_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isAutoTheme) {
                                        EmeraldSavings.copy(alpha = 0.22f)
                                    } else if (isLight) {
                                        Color(0xFFE2E8F0)
                                    } else {
                                        Color(0x330B111E)
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        width = if (isAutoTheme) 1.5.dp else 1.dp,
                                        color = if (isAutoTheme) {
                                            EmeraldSavings
                                        } else if (isLight) {
                                            Color(0xFFCBD5E1)
                                        } else {
                                            Color(0x44FFFFFF)
                                        }
                                    ),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Auto",
                                color = if (isAutoTheme) {
                                    if (isLight) Color(0xFF047857) else EmeraldSavings
                                } else {
                                    TextSecondary
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isAutoTheme) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    // Custom Switch Toggle with Glowing Active State
                    NeumorphicGlowingSwitch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeChange,
                        activeColor = ElectricCyan,
                        inactiveColor = NeonAmber,
                        checkedIcon = Icons.Default.DarkMode,
                        uncheckedIcon = Icons.Default.LightMode,
                        onPositionChanged = onSwitchPositionChanged,
                        modifier = Modifier.testTag("theme_switch_toggle")
                    )
                }
            }
        }
    }
}

/**
 * 2b. Dynamic App Title Wind Glow Effect Toggle Section
 */
@Composable
private fun DynamicGlowToggleSection(
    isEnabled: Boolean,
    isArabic: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = if (isEnabled) ElectricCyan.copy(alpha = 0.30f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isEnabled) ElectricCyan.copy(alpha = 0.15f) else Color(0x18777777)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isEnabled) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = if (isArabic) "التأثير الحركي لاسم التطبيق" else "App Title Dynamic Glow",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            NeumorphicGlowingSwitch(
                checked = isEnabled,
                onCheckedChange = onToggleChange,
                activeColor = ElectricCyan,
                inactiveColor = if (isLight) Color(0xFFCBD5E1) else Color(0xFF475569),
                checkedIcon = Icons.Default.AutoAwesome,
                uncheckedIcon = Icons.Default.AutoAwesome,
                modifier = Modifier.testTag("dynamic_glow_switch")
            )
        }
    }
}

/**
 * 2c. Interactive Fluid Water Physics Effect Toggle Section
 */
@Composable
private fun LiquidDockSwipeToggleSection(
    isEnabled: Boolean,
    isArabic: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = if (isEnabled) ElectricCyan.copy(alpha = 0.30f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isEnabled) ElectricCyan.copy(alpha = 0.15f) else Color(0x18777777)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = if (isEnabled) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = if (isArabic) "فيزياء المياه في الدوك السفلي" else "Bottom Dock Fluid Water",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isArabic) "تموجات المياه والتنقل بالسحب عبر الدوك" else "Water ripples & fluid swipe navigation",
                        color = if (isEnabled) ElectricCyan else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            NeumorphicGlowingSwitch(
                checked = isEnabled,
                onCheckedChange = onToggleChange,
                activeColor = ElectricCyan,
                inactiveColor = if (isLight) Color(0xFFCBD5E1) else Color(0xFF475569),
                checkedIcon = Icons.Default.WaterDrop,
                uncheckedIcon = Icons.Default.WaterDrop,
                modifier = Modifier.testTag("liquid_dock_swipe_switch")
            )
        }
    }
}

/**
 * 2d. Full Screen Mode Toggle Section (hides the phone's top status bar completely)
 */
@Composable
private fun FullScreenToggleSection(
    isEnabled: Boolean,
    isArabic: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = if (isEnabled) ElectricCyan.copy(alpha = 0.30f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isEnabled) ElectricCyan.copy(alpha = 0.15f) else Color(0x18777777)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.Fullscreen else Icons.Default.FullscreenExit,
                        contentDescription = null,
                        tint = if (isEnabled) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = if (isArabic) "وضع ملء الشاشة" else "Full Screen Mode",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isArabic) "إخفاء شريط الهاتف العلوي بالكامل" else "Hide top status bar completely",
                        color = if (isEnabled) ElectricCyan else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            NeumorphicGlowingSwitch(
                checked = isEnabled,
                onCheckedChange = onToggleChange,
                activeColor = ElectricCyan,
                inactiveColor = if (isLight) Color(0xFFCBD5E1) else Color(0xFF475569),
                checkedIcon = Icons.Default.Fullscreen,
                uncheckedIcon = Icons.Default.FullscreenExit,
                modifier = Modifier.testTag("full_screen_switch")
            )
        }
    }
}

/**
 * 2e. Two-Finger Swipe Gesture Toggle Section for Full Screen Mode
 */
@Composable
private fun TwoFingerFullScreenSwipeToggleSection(
    isEnabled: Boolean,
    isArabic: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = if (isEnabled) ElectricCyan.copy(alpha = 0.30f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isEnabled) ElectricCyan.copy(alpha = 0.15f) else Color(0x18777777)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwipeVertical,
                        contentDescription = null,
                        tint = if (isEnabled) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = if (isArabic) "السحب بإصبعين لملء الشاشة" else "Two-Finger Full Screen Swipe",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isArabic) {
                            "اسحب بإصبعين للأعلى للتفعيل وللأسفل للإلغاء"
                        } else {
                            "Swipe 2 fingers up to enable, down to exit"
                        },
                        color = if (isEnabled) ElectricCyan else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            NeumorphicGlowingSwitch(
                checked = isEnabled,
                onCheckedChange = onToggleChange,
                activeColor = ElectricCyan,
                inactiveColor = if (isLight) Color(0xFFCBD5E1) else Color(0xFF475569),
                checkedIcon = Icons.Default.SwipeVertical,
                uncheckedIcon = Icons.Default.SwipeVertical,
                modifier = Modifier.testTag("two_finger_fullscreen_swipe_switch")
            )
        }
    }
}

/**
 * Custom Neumorphic Switch Toggle with Animated Glowing Active States
 */
@Composable
fun NeumorphicGlowingSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = ElectricCyan,
    inactiveColor: Color = NeonAmber,
    checkedIcon: ImageVector = Icons.Default.DarkMode,
    uncheckedIcon: ImageVector = Icons.Default.LightMode,
    onPositionChanged: (Offset) -> Unit = {}
) {
    val trackWidth = 58.dp
    val trackHeight = 32.dp
    val thumbSize = 24.dp
    val padding = 4.dp

    val currentColor by animateColorAsState(
        targetValue = if (checked) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 300),
        label = "switch_accent_color"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - padding else padding,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "switch_thumb_offset"
    )

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInWindow()
                val center = Offset(
                    pos.x + coordinates.size.width / 2f,
                    pos.y + coordinates.size.height / 2f
                )
                onPositionChanged(center)
            }
            .size(width = trackWidth, height = trackHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x330B111E))
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(currentColor.copy(alpha = 0.7f), currentColor.copy(alpha = 0.25f))
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = currentColor),
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Glowing halo behind thumb when active
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = currentColor,
                    spotColor = currentColor
                )
                .clip(CircleShape)
                .background(currentColor)
                .border(BorderStroke(1.5.dp, Color.White), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (checked) checkedIcon else uncheckedIcon,
                contentDescription = null,
                tint = if (checked) Color(0xFF071422) else Color(0xFF1E1405),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * 3. QR Code Offline Sync Section
 * Streamlined to a single clean action button leading directly to the transfer & sync page.
 */
@Composable
private fun QrCodeOfflineSyncSection(
    isArabic: Boolean,
    onOpenTransfer: () -> Unit
) {
    SingleActionNavButton(
        icon = Icons.Default.QrCode,
        title = if (isArabic) "نقل ومشاركة القوائم (QR Code)" else "Transfer & Share Lists via QR",
        accentColor = EmeraldSavings,
        onClick = onOpenTransfer,
        testTag = "open_qr_transfer_button"
    )
}

/**
 * 4. Google Drive Cloud Backup Section
 * Streamlined to a single clean action button opening the Google Drive Backup & Restore dialog.
 */
@Composable
private fun GoogleDriveBackupSection(
    isArabic: Boolean,
    onOpenDrive: () -> Unit
) {
    SingleActionNavButton(
        icon = Icons.Default.CloudDone,
        title = if (isArabic) "النسخ الاحتياطي  (Google Drive)" else "Google Drive Backup",
        accentColor = ElectricCyan,
        onClick = onOpenDrive,
        testTag = "open_google_drive_button"
    )
}

/**
 * Clean Single Action Nav Button without clutter
 */
@Composable
private fun SingleActionNavButton(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val btnBg = if (isLight) Color.White else Color(0x221E293B)
    val btnBorder = if (isLight) BorderStroke(1.dp, LightCardBorder) else BorderStroke(1.dp, Color(0x33475569))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(btnBg)
            .border(btnBorder, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = accentColor),
                onClick = onClick
            )
            .padding(horizontal = 16.dp)
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = accentColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Dedicated Google Drive Backup & Restore Dialog
 */
@Composable
private fun GoogleDriveDialog(
    isArabic: Boolean,
    lastBackupDate: String,
    onDismiss: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val dialogBg = if (isLight) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isArabic) "إغلاق" else "Close",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isArabic) "Google Drive" else "Google Drive Sync",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (lastBackupDate.isNotBlank()) {
                        if (isArabic) "آخر نسخ احتياطي: $lastBackupDate" else "Last Backup: $lastBackupDate"
                    } else {
                        if (isArabic) "لم يتم عمل نسخ احتياطي بعد" else "No backup recorded yet"
                    },
                    color = if (lastBackupDate.isNotBlank()) EmeraldSavings else TextMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Backup Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSavings.copy(alpha = 0.18f))
                        .border(BorderStroke(1.2.dp, EmeraldSavings), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = EmeraldSavings),
                            onClick = {
                                onBackup()
                                onDismiss()
                            }
                        )
                        .padding(horizontal = 14.dp)
                        .testTag("dialog_drive_backup_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = EmeraldSavings,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "نسخ احتياطي جديد" else "New Backup to Drive",
                            color = EmeraldSavings,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Restore Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricCyan.copy(alpha = 0.18f))
                        .border(BorderStroke(1.2.dp, ElectricCyan), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = ElectricCyan),
                            onClick = {
                                onRestore()
                                onDismiss()
                            }
                        )
                        .padding(horizontal = 14.dp)
                        .testTag("dialog_drive_restore_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "استعادة نسخة سابقة" else "Restore from Drive",
                            color = ElectricCyan,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = dialogBg,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 4b. In-App Update Section
 */
@Composable
private fun AppUpdateSection(
    isArabic: Boolean,
    updateStatus: AppUpdateStatus,
    apkDownloadState: ApkDownloadState,
    onOpenUpdate: () -> Unit
) {
    val isChecking = updateStatus is AppUpdateStatus.Checking
    val availableUpdate = updateStatus as? AppUpdateStatus.UpdateAvailable
    val hasUpdate = availableUpdate != null
    val cleanVersion = availableUpdate?.latestVersion?.trim()?.removePrefix("v")?.removePrefix("V").orEmpty()

    val title = when (apkDownloadState) {
        is ApkDownloadState.Downloading -> if (isArabic) {
            val pct = if (apkDownloadState.progressPercent >= 0) "${apkDownloadState.progressPercent}%" else "..."
            "جاري تحميل التحديث ($pct)"
        } else {
            val pct = if (apkDownloadState.progressPercent >= 0) "${apkDownloadState.progressPercent}%" else "..."
            "Downloading Update ($pct)"
        }
        is ApkDownloadState.Downloaded,
        is ApkDownloadState.RequiresInstallPermission -> if (isArabic) {
            "التحديث جاهز للتثبيت"
        } else {
            "Update Ready to Install"
        }
        else -> when {
            isChecking && isArabic -> "جاري التحقق..."
            isChecking -> "Checking..."
            hasUpdate && isArabic -> "تحديث جديد متاح ($cleanVersion)"
            hasUpdate -> "New Update Available ($cleanVersion)"
            isArabic -> "التطبيق محدث الى اخر نسخة"
            else -> "App is up to date"
        }
    }

    val accentColor = when {
        isChecking -> ElectricCyan
        hasUpdate -> NeonAmber
        else -> EmeraldSavings
    }

    SingleActionNavButton(
        icon = Icons.Default.SystemUpdate,
        title = title,
        accentColor = accentColor,
        onClick = onOpenUpdate,
        testTag = "open_app_update_button",
        isLoading = isChecking
    )
}

/**
 * 5. App & Developer Info Section
 * Redesigned with precision, modern glass aesthetics, and verified badge.
 */
@Composable
private fun DeveloperInfoSection(
    isArabic: Boolean
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val cardBg = if (isLight) Color(0xF7FFFFFF) else Color(0xD90E1726)
    val cardBorder = if (isLight) {
        BorderStroke(1.2.dp, LightCardBorder)
    } else {
        BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    ElectricCyan.copy(alpha = 0.45f),
                    EmeraldSavings.copy(alpha = 0.35f),
                    Color(0x33475569)
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(cardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("developer_info_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Profile Row: Glowing Monogram Avatar + Developer Name + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Stylized Liquid Glowing Avatar
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(ElectricCyan, EmeraldSavings, Color(0xFF8B5CF6), ElectricCyan)
                                )
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(if (isLight) Color.White else Color(0xFF09101D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CK",
                            color = if (isLight) CyanTintText else ElectricCyan,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Developed By Charfeddine Ka",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp,
                                maxLines = 2,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = EmeraldSavings,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Shopy Pulse Official",
                            color = if (isLight) CyanTintText else ElectricCyan.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Delicate Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) CyanTintBg else Color(0x2200E5FF))
                        .border(
                            BorderStroke(1.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.5f)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        color = if (isLight) CyanTintText else ElectricCyan,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Soft Horizontal Hairline Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                if (isLight) Color(0x22000000) else Color(0x33475569),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Precision Metadata Grid (2x2 Clean Layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item 1: App Name
                DeveloperInfoMicroCard(
                    title = if (isArabic) "التطبيق" else "App",
                    value = "Shopy Pulse",
                    modifier = Modifier.weight(1f)
                )

                // Item 2: Engine Stack
                DeveloperInfoMicroCard(
                    title = if (isArabic) "المحرك" else "Stack",
                    value = "Compose • Room",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item 3: Build Version
                DeveloperInfoMicroCard(
                    title = if (isArabic) "الإصدار" else "Version",
                    value = ShoppingViewModel.APP_VERSION,
                    modifier = Modifier.weight(1f)
                )

                // Item 4: Last Update Date
                DeveloperInfoMicroCard(
                    title = if (isArabic) "التحديث" else "Updated",
                    value = if (isArabic) ShoppingViewModel.LAST_UPDATE_DATE_AR else ShoppingViewModel.LAST_UPDATE_DATE_EN,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DeveloperInfoMicroCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val itemBg = if (isLight) Color(0xFFF8FAFC) else Color(0x181E293B)
    val itemBorder = if (isLight) BorderStroke(1.dp, Color(0xFFE2E8F0)) else BorderStroke(1.dp, Color(0x22475569))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(itemBg)
            .border(itemBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Standard Neumorphic & Soft Glass Card Container with glowing borders & elevation
 */
@Composable
private fun NeumorphicGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    glowColor: Color = ElectricCyan,
    content: @Composable BoxScope.() -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val cardBg = if (isLight) Color(0xF2FFFFFF) else Color(0xCC0E1726)
    val cardBorder = if (isLight) {
        BorderStroke(1.2.dp, LightCardBorder)
    } else {
        BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(glowColor.copy(alpha = 0.55f), glowColor.copy(alpha = 0.15f), Color(0x10FFFFFF))
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isLight) 2.dp else 4.dp,
                shape = shape,
                ambientColor = if (isLight) Color.Black.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.5f),
                spotColor = if (isLight) Color.Black.copy(alpha = 0.08f) else glowColor.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(cardBg)
            .border(cardBorder, shape)
    ) {
        if (!isLight) {
            // Specular glass highlight line along top edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0x40FFFFFF), Color(0x20FFFFFF), Color.Transparent)
                        )
                    )
            )
        }
        content()
    }
}

/**
 * Section Header Component
 */
@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Neon Badge Chip Component
 */
@Composable
private fun NeonBadgeChip(
    text: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.14f))
            .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = accentColor,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Key-Value Info Row Component
 */
@Composable
private fun InfoKeyValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
