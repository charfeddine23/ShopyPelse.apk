package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.Screen
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
 * Production-ready Settings screen for 'Shopy Pulse'.
 * Follows Dark Slate aesthetic with Neumorphic & Soft Glass cards,
 * glowing neon borders, tactile feedback, and modular Compose architecture.
 */
@Composable
fun SettingsScreen(
    viewModel: ShoppingViewModel,
    onBack: () -> Unit,
    onOpenQrShare: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val lastBackupDate by viewModel.lastBackupDate.collectAsStateWithLifecycle()

    var showQrDialog by remember { mutableStateOf(false) }
    var qrDialogInitialMode by remember { mutableStateOf(false) }

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
            // Screen Header & Back Button
            item {
                SettingsHeader(
                    isArabic = isArabic,
                    onBack = onBack
                )
            }

            // Section 1: Header & Language Settings
            item {
                LanguageSettingsCard(
                    isArabic = isArabic,
                    onLanguageSelect = { arabicSelected ->
                        viewModel.setLanguage(arabicSelected)
                    }
                )
            }

            // Section 2: Theme Toggle Card (Night Mode & Day Mode)
            item {
                ThemeToggleCard(
                    isDarkMode = isDarkMode,
                    isArabic = isArabic,
                    onDarkModeSelect = { darkSelected ->
                        viewModel.setDarkMode(darkSelected)
                    }
                )
            }

            // Section 3: QR Code Sync Section ("دون إنترنت", "مشاركة القائمة" vibrant green, "استيراد القائمة" cyan)
            item {
                QrCodeSyncCard(
                    isArabic = isArabic,
                    onShareQr = {
                        qrDialogInitialMode = false
                        showQrDialog = true
                    },
                    onImportQr = {
                        qrDialogInitialMode = true
                        showQrDialog = true
                    }
                )
            }

            // Section 4: Google Drive Sync Section ("سحابي", last backup status, "Backup" & "Restore")
            item {
                GoogleDriveSyncCard(
                    isArabic = isArabic,
                    lastBackupDate = lastBackupDate,
                    onBackup = {
                        createDocLauncher.launch("ShopyPulse_Backup_${System.currentTimeMillis()}.json")
                    },
                    onRestore = {
                        openDocLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    }
                )
            }

            // Section 5: App & Developer Info Card
            item {
                AppAndDeveloperInfoCard(
                    isArabic = isArabic
                )
            }

            // Bottom Spacing for Floating Navigation Bar
            item {
                Spacer(modifier = Modifier.height(32.dp))
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
    }
}

/**
 * Top Screen Header with frosted circular back button
 */
@Composable
private fun SettingsHeader(
    isArabic: Boolean,
    onBack: () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val backBtnBg = if (isLight) Color.White else Color(0x331E293B)
    val backBtnBorder = if (isLight) BorderStroke(1.dp, LightCardBorder) else BorderStroke(1.dp, Color(0x33475569))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .clip(CircleShape)
                .background(backBtnBg)
                .border(backBtnBorder, CircleShape)
                .size(44.dp)
                .testTag("settings_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (isLight) CyanTintText else ElectricCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = if (isArabic) "الإعدادات" else "Settings",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = if (isArabic) "تخصيص التطبيق، مزامنة ونقل البيانات" else "Preferences, data sync & app settings",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Section 1: Header & Language Settings
 */
@Composable
private fun LanguageSettingsCard(
    isArabic: Boolean,
    onLanguageSelect: (Boolean) -> Unit
) {
    Column {
        SectionTitleHeader(
            icon = Icons.Default.Translate,
            title = if (isArabic) "إعدادات اللغة" else "Language Settings",
            accentColor = ElectricCyan
        )

        Spacer(modifier = Modifier.height(6.dp))

        NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = ElectricCyan.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) "اختر لغة واجهة التطبيق:" else "Choose application interface language:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LanguageSelectionPill(
                        title = "Arabic",
                        subtitle = "العربية",
                        isSelected = isArabic,
                        onClick = { onLanguageSelect(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("language_arabic")
                    )

                    LanguageSelectionPill(
                        title = "English",
                        subtitle = "الإنجليزية",
                        isSelected = !isArabic,
                        onClick = { onLanguageSelect(false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("language_english")
                    )
                }
            }
        }
    }
}

/**
 * Pill for selecting Language with active glowing neon border and checkmark
 */
@Composable
private fun LanguageSelectionPill(
    title: String,
    subtitle: String,
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
            .clip(RoundedCornerShape(16.dp))
            .background(pillBg)
            .border(pillBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = ElectricCyan),
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = if (isSelected) titleColor.copy(alpha = 0.8f) else TextSecondary,
                    fontSize = 11.sp
                )
            }
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
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
 * Section 2: Theme Toggle Card (Options for Night Mode & Day Mode)
 */
@Composable
private fun ThemeToggleCard(
    isDarkMode: Boolean,
    isArabic: Boolean,
    onDarkModeSelect: (Boolean) -> Unit
) {
    Column {
        SectionTitleHeader(
            icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
            title = "Theme",
            accentColor = NeonAmber
        )

        Spacer(modifier = Modifier.height(6.dp))

        NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = NeonAmber.copy(alpha = 0.35f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeOptionCard(
                        icon = Icons.Default.DarkMode,
                        title = if (isArabic) "الوضع الليلي" else "Night Mode",
                        isSelected = isDarkMode,
                        accentColor = ElectricCyan,
                        onClick = { onDarkModeSelect(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_night")
                    )

                    ThemeOptionCard(
                        icon = Icons.Default.LightMode,
                        title = if (isArabic) "الوضع النهاري" else "Day Mode",
                        isSelected = !isDarkMode,
                        accentColor = NeonAmber,
                        onClick = { onDarkModeSelect(false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_day")
                    )
                }
            }
        }
    }
}

/**
 * Neumorphic Card for Night / Day Mode Toggle
 */
@Composable
private fun ThemeOptionCard(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val cardBg = if (isLight) {
        if (isSelected) PeachTintBg else Color.White
    } else {
        if (isSelected) accentColor.copy(alpha = 0.18f) else Color(0x181E293B)
    }
    val cardBorder = if (isLight) {
        if (isSelected) BorderStroke(1.5.dp, PeachTintBorder) else BorderStroke(1.dp, LightCardBorder)
    } else {
        if (isSelected) BorderStroke(1.5.dp, accentColor) else BorderStroke(1.dp, Color(0x33475569))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(cardBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = accentColor),
                onClick = onClick
            )
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = if (isSelected) accentColor else TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Section 3: QR Code Sync Section
 * Features an offline badge ("دون إنترنت"), descriptive text,
 * and dual action buttons: "مشاركة القائمة" in vibrant green and "استيراد القائمة" in cyan.
 */
@Composable
private fun QrCodeSyncCard(
    isArabic: Boolean,
    onShareQr: () -> Unit,
    onImportQr: () -> Unit
) {
    Column {
        SectionTitleHeader(
            icon = Icons.Default.QrCode,
            title = if (isArabic) "نقل ومشاركة القوائم عبر كود QR" else "QR Code List Transfer & Sync",
            accentColor = EmeraldSavings
        )

        Spacer(modifier = Modifier.height(6.dp))

        NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = EmeraldSavings.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header with Icon, Title and Offline Badge ("دون إنترنت")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldSavings.copy(alpha = 0.16f))
                                .border(BorderStroke(1.dp, EmeraldSavings.copy(alpha = 0.6f)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = EmeraldSavings,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isArabic) "المزامنة المباشرة بـ QR Code" else "Direct QR Sync",
                                color = TextPrimary,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) "نقل واستيراد القوائم فلاشياً بدون إنترنت" else "Share & import lists offline instantly",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Offline Badge
                    NeonBadgeChip(
                        text = if (isArabic) "دون إنترنت" else "OFFLINE",
                        accentColor = EmeraldSavings
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Descriptive Text
                Text(
                    text = if (isArabic) {
                        "يمكنك عرض كود QR لمسحه من هاتف آخر ونقل كامل قائمة المشتريات فوراً، أو استيراد قائمة جديدة عبر لصق الكود."
                    } else {
                        "Display a QR code to transfer your shopping list to another device, or import a shared list by pasting/scanning."
                    },
                    color = TextSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dual Action Buttons:
                // Button 1: "مشاركة القائمة" in vibrant green
                // Button 2: "استيراد القائمة" in cyan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // "مشاركة القائمة" Button (Vibrant Green)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(EmeraldSavings, EmeraldSavings.copy(alpha = 0.85f))
                                )
                            )
                            .border(BorderStroke(1.dp, EmeraldSavings), RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = Color.White),
                                onClick = onShareQr
                            )
                            .padding(horizontal = 8.dp)
                            .testTag("qr_share_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "مشاركة القائمة" else "Share via QR",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // "استيراد القائمة" Button (Cyan)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ElectricCyan, ElectricCyan.copy(alpha = 0.85f))
                                )
                            )
                            .border(BorderStroke(1.dp, ElectricCyan), RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = Color.White),
                                onClick = onImportQr
                            )
                            .padding(horizontal = 8.dp)
                            .testTag("qr_import_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color(0xFF04121B),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "استيراد القائمة" else "Import List",
                                color = Color(0xFF04121B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section 4: Google Drive Sync Section
 * Features a cloud badge ("سحابي"), last backup status, and "Backup" / "Restore" buttons.
 */
@Composable
private fun GoogleDriveSyncCard(
    isArabic: Boolean,
    lastBackupDate: String,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    Column {
        SectionTitleHeader(
            icon = Icons.Default.CloudDone,
            title = if (isArabic) "النسخ الاحتياطي في Google Drive" else "Google Drive Cloud Backup & Restore",
            accentColor = ElectricCyan
        )

        Spacer(modifier = Modifier.height(6.dp))

        NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = ElectricCyan.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header with Cloud Icon, Title, and Cloud Badge ("سحابي")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricCyan.copy(alpha = 0.2f))
                                .border(BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Google Drive Sync",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) "حفظ القوائم والأسعار في سحابتك" else "Sync lists & prices to your cloud",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Cloud Badge ("سحابي")
                    NeonBadgeChip(
                        text = if (isArabic) "سحابي" else "CLOUD",
                        accentColor = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Last backup status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lastBackupDate.isNotBlank()) {
                            if (isArabic) "آخر نسخ احتياطي: $lastBackupDate" else "Last Backup: $lastBackupDate"
                        } else {
                            if (isArabic) "لم يتم عمل نسخة احتياطية بعد" else "No backup recorded yet"
                        },
                        color = if (lastBackupDate.isNotBlank()) EmeraldSavings else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dual Action Buttons: "Backup" / "Restore"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Backup Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldSavings.copy(alpha = 0.18f))
                            .border(BorderStroke(1.2.dp, EmeraldSavings), RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = EmeraldSavings),
                                onClick = onBackup
                            )
                            .padding(horizontal = 8.dp)
                            .testTag("drive_backup_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = EmeraldSavings,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "نسخ احتياطي" else "Backup",
                                color = EmeraldSavings,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Restore Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricCyan.copy(alpha = 0.18f))
                            .border(BorderStroke(1.2.dp, ElectricCyan), RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = ElectricCyan),
                                onClick = onRestore
                            )
                            .padding(horizontal = 8.dp)
                            .testTag("drive_restore_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "استعادة" else "Restore",
                                color = ElectricCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section 5: App & Developer Info Card
 * Displays "Developed by Kahlessenane Charfeddine" with a verified badge,
 * app name "Shopy Pulse (شوبي بلس)", version (v1.0 Build 1), last update date,
 * and tech stack (Jetpack Compose • Room DB).
 */
@Composable
private fun AppAndDeveloperInfoCard(
    isArabic: Boolean
) {
    Column {
        SectionTitleHeader(
            icon = Icons.Default.Info,
            title = if (isArabic) "معلومات التطبيق والمطور" else "App & Developer Info",
            accentColor = ElectricCyan
        )

        Spacer(modifier = Modifier.height(6.dp))

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
                InfoRow(
                    label = if (isArabic) "اسم التطبيق" else "App Name",
                    value = "Shopy Pulse (شوبي بلس)"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Version Code
                InfoRow(
                    label = if (isArabic) "رقم الإصدار" else "Version Code",
                    value = ShoppingViewModel.APP_VERSION
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Last Update Date
                InfoRow(
                    label = if (isArabic) "تاريخ آخر تحديث" else "Last Updated",
                    value = if (isArabic) ShoppingViewModel.LAST_UPDATE_DATE_AR else ShoppingViewModel.LAST_UPDATE_DATE_EN
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tech Stack
                InfoRow(
                    label = if (isArabic) "الواجهة والتقنيات" else "Tech Stack",
                    value = "Jetpack Compose • Room DB"
                )
            }
        }
    }
}

/**
 * Standard Neumorphic & Soft Glass Card Container
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
 * Section title header with glowing accent icon
 */
@Composable
private fun SectionTitleHeader(
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
 * Neon Badge Chip
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
 * Key-Value Info Row with secondary label and primary value
 */
@Composable
private fun InfoRow(
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
