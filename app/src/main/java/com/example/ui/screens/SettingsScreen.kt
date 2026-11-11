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
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
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
import com.example.ui.viewmodel.ShoppingViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

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

    var showQrDialog by remember { mutableStateOf(false) }
    var qrDialogInitialMode by remember { mutableStateOf(false) }
    var showDriveDialog by remember { mutableStateOf(false) }

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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp)
    ) {
        Text(
            text = if (isArabic) "الإعدادات" else "Settings",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = if (isArabic) "تخصيص التطبيق، مزامنة ونقل البيانات" else "Preferences, data sync & app settings",
            color = TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
 * along with dedicated selection cards.
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
            title = "Theme",
            accentColor = NeonAmber
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = if (isDarkMode) ElectricCyan.copy(alpha = 0.35f) else NeonAmber.copy(alpha = 0.35f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Interactive Row with description and Custom Glowing Switch Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDarkMode) {
                                if (isArabic) "الوضع الليلي نشط" else "Night Mode Active"
                            } else {
                                if (isArabic) "الوضع النهاري نشط" else "Day Mode Active"
                            },
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "تبديل المظهر بين الداكن والفاتح" else "Toggle between Dark and Light mode",
                            color = TextSecondary,
                            fontSize = 11.5.sp
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
    Column {
        SectionHeader(
            icon = Icons.Default.QrCode,
            title = if (isArabic) "نقل ومشاركة القوائم عبر كود QR" else "QR Code List Transfer & Sync",
            accentColor = EmeraldSavings
        )

        Spacer(modifier = Modifier.height(10.dp))

        SingleActionNavButton(
            icon = Icons.Default.QrCode,
            title = if (isArabic) "نقل ومشاركة القوائم (QR Code)" else "Transfer & Share Lists via QR",
            accentColor = EmeraldSavings,
            onClick = onOpenTransfer,
            testTag = "open_qr_transfer_button"
        )
    }
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
    Column {
        SectionHeader(
            icon = Icons.Default.CloudDone,
            title = if (isArabic) "النسخ الاحتياطي في Google Drive" else "Google Drive Cloud Backup & Restore",
            accentColor = ElectricCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        SingleActionNavButton(
            icon = Icons.Default.CloudDone,
            title = if (isArabic) "النسخ الاحتياطي والاستعادة (Google Drive)" else "Google Drive Backup & Restore",
            accentColor = ElectricCyan,
            onClick = onOpenDrive,
            testTag = "open_google_drive_button"
        )
    }
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
 * 5. App & Developer Info Section
 * Displays "Developed by Kahlessenane Charfeddine" with verified badge,
 * app name "Shopy Pulse (شوبي بلس)", version (v1.0 Build 1), last update date,
 * and tech stack (Jetpack Compose • Room DB).
 */
@Composable
private fun DeveloperInfoSection(
    isArabic: Boolean
) {
    Column {
        SectionHeader(
            icon = Icons.Default.Info,
            title = if (isArabic) "معلومات التطبيق والمطور" else "App & Developer Info",
            accentColor = ElectricCyan
        )

        Spacer(modifier = Modifier.height(8.dp))

        NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = ElectricCyan.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                val isLightCard = MaterialTheme.colorScheme.background == BackgroundLight
                val devBannerBg = if (isLightCard) CyanTintBg else Color(0x2800E5FF)
                val devBannerBorder = if (isLightCard) BorderStroke(1.dp, CyanTintBorder) else BorderStroke(1.dp, Color(0x6600E5FF))

                // Developer Banner with Avatar and Verified Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(devBannerBg)
                        .border(devBannerBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .border(BorderStroke(2.dp, Color.White), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF031422),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ShoppingViewModel.DEVELOPER_NAME,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
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
                            text = if (isArabic) "مهندس ومطور التطبيق الرسمي" else "Lead Mobile Software Engineer & UI/UX Designer",
                            color = if (isLightCard) CyanTintText else ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Name
                InfoKeyValueRow(
                    label = if (isArabic) "اسم التطبيق" else "App Name",
                    value = "Shopy Pulse (شوبي بلس)"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Version Code
                InfoKeyValueRow(
                    label = if (isArabic) "رقم الإصدار" else "Version Code",
                    value = ShoppingViewModel.APP_VERSION
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Last Update Date
                InfoKeyValueRow(
                    label = if (isArabic) "تاريخ آخر تحديث" else "Last Updated",
                    value = if (isArabic) ShoppingViewModel.LAST_UPDATE_DATE_AR else ShoppingViewModel.LAST_UPDATE_DATE_EN
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tech Stack
                InfoKeyValueRow(
                    label = if (isArabic) "الواجهة والتقنيات" else "Tech Stack",
                    value = "Jetpack Compose • Room DB"
                )
            }
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
