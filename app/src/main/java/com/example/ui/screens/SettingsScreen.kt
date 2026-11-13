package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.util.AppUpdateChecker
import com.example.util.AppUpdateStatus
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val lastBackupDate by viewModel.lastBackupDate.collectAsStateWithLifecycle()
    val isDynamicGlowEnabled by viewModel.isDynamicGlowEnabled.collectAsStateWithLifecycle()
    val isLiquidDockSwipeEnabled by viewModel.isLiquidDockSwipeEnabled.collectAsStateWithLifecycle()
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    val lastUpdateCheckTimestamp by viewModel.lastUpdateCheckTimestamp.collectAsStateWithLifecycle()
    val apkDownloadState by viewModel.apkDownloadState.collectAsStateWithLifecycle()

    var showQrDialog by remember { mutableStateOf(false) }
    var qrDialogInitialMode by remember { mutableStateOf(false) }
    var showDriveDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Circular Reveal animation states for Light/Dark mode transition
    var revealTargetDark by remember { mutableStateOf<Boolean?>(null) }
    var switchCenterOffset by remember { mutableStateOf(Offset.Zero) }

    // Storage Access Framework Launcher to save backup file (Google Drive / local)
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(viewModel.generateGoogleDriveBackupPayload().toByteArray())
                }
                viewModel.markGoogleDriveBackupDone()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Storage Access Framework Launcher to restore backup file (Google Drive / local)
    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    viewModel.restoreFromBackupPayload(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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

            // 2. Theme Mode Section (with Custom Glowing Switch Toggle)
            item {
                ThemeModeSection(
                    isDarkMode = isDarkMode,
                    isArabic = isArabic,
                    onDarkModeChange = { darkSelected ->
                        if (revealTargetDark == null && darkSelected != isDarkMode) {
                            revealTargetDark = darkSelected
                        } else if (revealTargetDark == null) {
                            viewModel.setDarkMode(darkSelected)
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
                    onOpenUpdate = {
                        if (updateStatus is AppUpdateStatus.UpdateAvailable) {
                            viewModel.openUpdateOverlayIfAvailable()
                        } else {
                            showUpdateDialog = true
                            viewModel.checkForUpdates(autoOpenOverlay = true)
                        }
                    }
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

        // Dismiss status dialog automatically when an update is found so AppUpdateOverlay shows cleanly
        LaunchedEffect(updateStatus, showUpdateDialog) {
            if (showUpdateDialog && updateStatus is AppUpdateStatus.UpdateAvailable) {
                showUpdateDialog = false
            }
        }

        // In-App Update Dialog
        if (showUpdateDialog) {
            AppUpdateDialog(
                isArabic = isArabic,
                updateStatus = updateStatus,
                apkDownloadState = apkDownloadState,
                lastCheckTimestamp = lastUpdateCheckTimestamp,
                onDismiss = { showUpdateDialog = false },
                onCheckNow = { viewModel.checkForUpdates() },
                onStartInAppDownload = { url ->
                    viewModel.startInAppUpdateDownload(context = context, url = url)
                },
                onInstallApk = { viewModel.installDownloadedApk(context) },
                onCancelDownload = { viewModel.cancelUpdateDownload() },
                onOpenFallbackUrl = { url ->
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

        // Circular Reveal Transition Animation Overlay for Light/Dark Mode Switch
        if (revealTargetDark != null) {
            CircularRevealThemeOverlay(
                isTargetDark = revealTargetDark!!,
                center = switchCenterOffset,
                onMiddlePoint = {
                    viewModel.setDarkMode(revealTargetDark!!)
                },
                onAnimationEnd = {
                    revealTargetDark = null
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
 * in a compact container.
 */
@Composable
private fun ThemeModeSection(
    isDarkMode: Boolean,
    isArabic: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onSwitchPositionChanged: (Offset) -> Unit = {}
) {
    Column {
        SectionHeader(
            icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
            title = if (isArabic) "المظهر والسمات" else "Theme & Mode",
            accentColor = NeonAmber
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = if (isDarkMode) ElectricCyan.copy(alpha = 0.30f) else NeonAmber.copy(alpha = 0.30f)
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
                            .background(if (isDarkMode) ElectricCyan.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = if (isDarkMode) ElectricCyan else NeonAmber,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (isDarkMode) {
                            if (isArabic) "الوضع الليلي" else "Night Mode"
                        } else {
                            if (isArabic) "الوضع النهاري" else "Day Mode"
                        },
                        color = TextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                        text = if (isArabic) "فيزياء المياه التفاعلية" else "Fluid Water Touch Physics",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isArabic) "تموجات وانعكاس المياه عند اللمس والتمرير" else "Real-time water ripples & touch waves",
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
    modifier: Modifier = Modifier
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
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
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
    onOpenUpdate: () -> Unit
) {
    val hasUpdate = updateStatus is AppUpdateStatus.UpdateAvailable
    val title = when {
        hasUpdate && isArabic -> "تحديث جديد متاح (${(updateStatus as AppUpdateStatus.UpdateAvailable).latestVersion})"
        hasUpdate -> "New Update Available (${(updateStatus as AppUpdateStatus.UpdateAvailable).latestVersion})"
        isArabic -> "تحديث التطبيق"
        else -> "App Updates"
    }
    SingleActionNavButton(
        icon = Icons.Default.SystemUpdate,
        title = title,
        accentColor = if (hasUpdate) NeonAmber else EmeraldSavings,
        onClick = onOpenUpdate,
        testTag = "open_app_update_button"
    )
}

/**
 * Dedicated In-App Update Dialog (checks GitHub release & downloads/installs APK inside the app)
 */
@Composable
private fun AppUpdateDialog(
    isArabic: Boolean,
    updateStatus: AppUpdateStatus,
    apkDownloadState: ApkDownloadState,
    lastCheckTimestamp: Long,
    onDismiss: () -> Unit,
    onCheckNow: () -> Unit,
    onStartInAppDownload: (String) -> Unit,
    onInstallApk: () -> Unit,
    onCancelDownload: () -> Unit,
    onOpenFallbackUrl: (String) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val dialogBg = if (isLight) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val accentColor = if (updateStatus is AppUpdateStatus.UpdateAvailable) NeonAmber else EmeraldSavings

    val downloadUrl = when (updateStatus) {
        is AppUpdateStatus.UpdateAvailable -> updateStatus.downloadUrl
        is AppUpdateStatus.ReadyDirectLink -> updateStatus.downloadUrl
        is AppUpdateStatus.UpToDate -> updateStatus.directUrl
        is AppUpdateStatus.Error -> updateStatus.fallbackUrl
        else -> AppUpdateChecker.UPDATE_URL
    }

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
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isArabic) "تحديث التطبيق" else "App Updates",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Current Installed Version
                Text(
                    text = if (isArabic) {
                        "الإصدار الحالي: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"
                    } else {
                        "Current Version: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"
                    },
                    color = TextSecondary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )

                if (lastCheckTimestamp > 0L) {
                    val formatted = remember(lastCheckTimestamp) {
                        SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(Date(lastCheckTimestamp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) "آخر فحص: $formatted" else "Last checked: $formatted",
                        color = TextMuted,
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) Color.White else Color(0x221E293B))
                        .border(
                            BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    when (updateStatus) {
                        is AppUpdateStatus.Checking -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = ElectricCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isArabic) "جاري التحقق من التحديثات..." else "Checking for updates...",
                                    color = ElectricCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        is AppUpdateStatus.UpdateAvailable -> {
                            Column {
                                Text(
                                    text = if (isArabic) {
                                        "يتوفر إصدار جديد: ${updateStatus.latestVersion}"
                                    } else {
                                        "New version available: ${updateStatus.latestVersion}"
                                    },
                                    color = NeonAmber,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (updateStatus.releaseNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = updateStatus.releaseNotes,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        is AppUpdateStatus.UpToDate -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSavings,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "لديك أحدث إصدار من التطبيق" else "You are on the latest version",
                                    color = EmeraldSavings,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        is AppUpdateStatus.ReadyDirectLink -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSavings,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) {
                                        "رابط التحديث المباشر جاهز للتنزيل"
                                    } else {
                                        "Direct update package link is ready"
                                    },
                                    color = EmeraldSavings,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        is AppUpdateStatus.Error -> {
                            Text(
                                text = if (isArabic) updateStatus.messageAr else updateStatus.messageEn,
                                color = NeonAmber,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        is AppUpdateStatus.Idle -> {
                            Text(
                                text = if (isArabic) "اضغط فحص التحديثات للتحقق من توفر نسخة أحدث" else "Tap Check for Updates to look for new releases",
                                color = TextSecondary,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // In-App APK Download / Progress / Install Section
                when (apkDownloadState) {
                    is ApkDownloadState.Idle -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldSavings.copy(alpha = 0.18f))
                                .border(BorderStroke(1.2.dp, EmeraldSavings), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = EmeraldSavings),
                                    onClick = { onStartInAppDownload(downloadUrl) }
                                )
                                .padding(horizontal = 14.dp)
                                .testTag("dialog_update_download_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = EmeraldSavings,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "تحميل وتحديث داخل التطبيق" else "Download & Update In-App",
                                    color = EmeraldSavings,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    is ApkDownloadState.Downloading -> {
                        val hasTotal = apkDownloadState.totalBytes > 0L && apkDownloadState.progressPercent >= 0
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldSavings.copy(alpha = 0.08f))
                                .border(BorderStroke(1.dp, EmeraldSavings.copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "جاري التحميل..." else "Downloading...",
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (hasTotal) "${apkDownloadState.progressPercent}%" else "...",
                                    color = EmeraldSavings,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (hasTotal) {
                                LinearProgressIndicator(
                                    progress = { (apkDownloadState.progressPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = EmeraldSavings,
                                    trackColor = Color(0x3364748B),
                                    strokeCap = StrokeCap.Round
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = EmeraldSavings,
                                    trackColor = Color(0x3364748B),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = onCancelDownload,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = if (isArabic) "إلغاء التحميل" else "Cancel",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    is ApkDownloadState.Downloaded,
                    is ApkDownloadState.RequiresInstallPermission -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldSavings.copy(alpha = 0.22f))
                                .border(BorderStroke(1.2.dp, EmeraldSavings), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = EmeraldSavings),
                                    onClick = onInstallApk
                                )
                                .padding(horizontal = 14.dp)
                                .testTag("dialog_update_install_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (apkDownloadState is ApkDownloadState.RequiresInstallPermission) {
                                    if (isArabic) "تثبيت التحديث (اسمح بالمصدر)" else "Install Update (Allow Source)"
                                } else {
                                    if (isArabic) "تثبيت التحديث الآن" else "Install Update Now"
                                },
                                color = EmeraldSavings,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    is ApkDownloadState.Failed -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isArabic) apkDownloadState.messageAr else apkDownloadState.messageEn,
                                color = NeonAmber,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldSavings.copy(alpha = 0.18f))
                                    .border(BorderStroke(1.2.dp, EmeraldSavings), RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, color = EmeraldSavings),
                                        onClick = { onStartInAppDownload(downloadUrl) }
                                    )
                                    .padding(horizontal = 14.dp)
                                    .testTag("dialog_update_retry_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isArabic) "إعادة التحميل" else "Retry Download",
                                    color = EmeraldSavings,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = { onOpenFallbackUrl(apkDownloadState.fallbackUrl) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = if (isArabic) "فتح عبر المتصفح" else "Open in Browser",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Re-check Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(BorderStroke(1.2.dp, ElectricCyan), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = ElectricCyan),
                            onClick = onCheckNow
                        )
                        .padding(horizontal = 14.dp)
                        .testTag("dialog_update_check_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "إعادة فحص التحديثات" else "Check Again",
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
                                text = ShoppingViewModel.DEVELOPER_NAME,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = EmeraldSavings,
                                modifier = Modifier.size(17.dp)
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
