package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.NeonBadge
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

    // Storage Access Framework Launcher to save backup file to Google Drive or local device
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

    // Storage Access Framework Launcher to restore backup from Google Drive or local file
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
            // Top Bar
            item {
                val isLight = MaterialTheme.colorScheme.background == BackgroundLight
                val backBtnBg = if (isLight) Color.White else Color(0x33FFFFFF)
                val backBtnBorder = if (isLight) BorderStroke(1.dp, LightCardBorder) else BorderStroke(1.dp, Color(0x20FFFFFF))

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
                            .size(42.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isLight) CyanTintText else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isArabic) "الإعدادات" else "Settings",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "تخصيص التطبيق، مزامنة ونقل البيانات" else "Preferences, data sync & app settings",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 1. Language Settings Card
            item {
                SettingsSectionHeader(
                    icon = Icons.Default.Translate,
                    title = if (isArabic) "إعدادات اللغة" else "Language",
                    accentColor = ElectricCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isArabic) "اختر لغة واجهة التطبيق:" else "Choose application language:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LanguageOptionPill(
                                title = "العربية (RTL)",
                                subtitle = "الافتراضية بالدينار الجزائري",
                                isSelected = isArabic,
                                onClick = { viewModel.setLanguage(true) },
                                modifier = Modifier.weight(1f)
                            )

                            LanguageOptionPill(
                                title = "English (LTR)",
                                subtitle = "Standard Latin Layout",
                                isSelected = !isArabic,
                                onClick = { viewModel.setLanguage(false) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Appearance: Dark / Light Mode
            item {
                SettingsSectionHeader(
                    icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = if (isArabic) "المظهر (الوضع الليلي والنهاري)" else "Theme (Dark & Light Mode)",
                    accentColor = NeonAmber
                )

                Spacer(modifier = Modifier.height(6.dp))

                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isArabic) "نمط الواجهة والتصميم الزجاجي:" else "Interface Liquid Glass styling:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThemeOptionPill(
                                icon = Icons.Default.DarkMode,
                                title = if (isArabic) "الوضع الليلي" else "Dark Mode",
                                subtitle = "Liquid Glass M5",
                                isSelected = isDarkMode,
                                onClick = { viewModel.setDarkMode(true) },
                                modifier = Modifier.weight(1f)
                            )

                            ThemeOptionPill(
                                icon = Icons.Default.LightMode,
                                title = if (isArabic) "الوضع النهاري" else "Light Mode",
                                subtitle = "Frosted Daylight",
                                isSelected = !isDarkMode,
                                onClick = { viewModel.setDarkMode(false) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. QR Code Data Transfer & Offline Sharing (Prominent & Coordinated)
            item {
                SettingsSectionHeader(
                    icon = Icons.Default.QrCode,
                    title = if (isArabic) "نقل ومشاركة القوائم عبر كود QR" else "QR Code List Transfer & Sync",
                    accentColor = EmeraldSavings
                )

                Spacer(modifier = Modifier.height(6.dp))

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = EmeraldSavings
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(EmeraldSavings.copy(alpha = 0.2f))
                                        .border(BorderStroke(1.dp, EmeraldSavings), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = EmeraldSavings,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isArabic) "المزامنة المباشرة بالـ QR" else "Direct QR Transfer",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isArabic) "نقل واستيراد القوائم فلاشياً بدون إنترنت" else "Share & import lists offline instantly",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            NeonBadge(
                                text = if (isArabic) "دون إنترنت" else "OFFLINE",
                                accentColor = EmeraldSavings
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isArabic) {
                                "يمكنك عرض كود QR لمسحه من هاتف آخر ونقل كامل قائمة المشتريات فوراً، أو استيراد قائمة جديدة عبر لصق الكود."
                            } else {
                                "Display a QR code to transfer your shopping list to another device, or import a shared list by pasting/scanning."
                            },
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Share QR / Import QR
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlowingActionButton(
                                text = if (isArabic) "مشاركة القائمة بالـ QR" else "Share via QR",
                                icon = Icons.Default.Share,
                                color = EmeraldSavings,
                                onClick = { showQrDialog = true },
                                modifier = Modifier.weight(1f)
                            )

                            GlowingActionButton(
                                text = if (isArabic) "استيراد قائمة" else "Import List",
                                icon = Icons.Default.Download,
                                color = ElectricCyan,
                                onClick = { showQrDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Google Drive Cloud Backup & Restore Card
            item {
                SettingsSectionHeader(
                    icon = Icons.Default.CloudDone,
                    title = if (isArabic) "النسخ الاحتياطي في Google Drive" else "Google Drive Cloud Backup & Restore",
                    accentColor = ElectricCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = ElectricCyan
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ElectricCyan.copy(alpha = 0.2f))
                                        .border(BorderStroke(1.dp, ElectricCyan), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
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

                            NeonBadge(
                                text = if (isArabic) "سحابي" else "CLOUD",
                                accentColor = ElectricCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Last backup info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
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

                        // Compact & Harmonious Action Buttons: Backup & Restore
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Backup Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldSavings.copy(alpha = 0.18f))
                                    .border(BorderStroke(1.dp, EmeraldSavings), RoundedCornerShape(12.dp))
                                    .clickable {
                                        createDocLauncher.launch("ShopyPelse_Backup_${System.currentTimeMillis()}.json")
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = EmeraldSavings,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Backup",
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ElectricCyan.copy(alpha = 0.18f))
                                    .border(BorderStroke(1.dp, ElectricCyan), RoundedCornerShape(12.dp))
                                    .clickable {
                                        openDocLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Restore",
                                        color = ElectricCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Sync Simulator
                        val isLight = MaterialTheme.colorScheme.background == BackgroundLight
                        val syncBg = if (isLight) CyanTintBg else Color(0x18FFFFFF)
                        val syncBorder = if (isLight) BorderStroke(1.dp, CyanTintBorder) else BorderStroke(1.dp, Color(0x20FFFFFF))
                        val syncText = if (isLight) CyanTintText else ElectricCyan

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(syncBg)
                                .border(syncBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.markGoogleDriveBackupDone()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "⚡ مزامنة وتحديث حالة Drive الآن" else "⚡ Refresh Cloud Status Now",
                                color = syncText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 5. Developer & App Information Card
            item {
                SettingsSectionHeader(
                    icon = Icons.Default.Info,
                    title = if (isArabic) "معلومات التطبيق والمطور" else "App & Developer Info",
                    accentColor = ElectricCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = ElectricCyan
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        val isLightCard = MaterialTheme.colorScheme.background == BackgroundLight
                        val devBannerBg = if (isLightCard) CyanTintBg else Color(0x2800E5FF)
                        val devBannerBorder = if (isLightCard) BorderStroke(1.dp, CyanTintBorder) else BorderStroke(1.dp, Color(0x6600E5FF))

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
                                        modifier = Modifier.size(16.dp)
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

                        InfoRow(
                            label = if (isArabic) "اسم التطبيق" else "App Name",
                            value = "ShopyPelse (شوبي بلس)"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow(
                            label = if (isArabic) "رقم الإصدار" else "Version Code",
                            value = ShoppingViewModel.APP_VERSION
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow(
                            label = if (isArabic) "تاريخ آخر تحديث" else "Last Updated",
                            value = if (isArabic) ShoppingViewModel.LAST_UPDATE_DATE_AR else ShoppingViewModel.LAST_UPDATE_DATE_EN
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow(
                            label = if (isArabic) "الواجهة والتقنيات" else "Tech Stack",
                            value = "Jetpack Compose • Liquid Glass M5 • Room DB"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Dedicated Offline QR Share & Import Dialog
        if (showQrDialog) {
            QrShareDialog(
                payload = viewModel.getQrSharePayload(),
                isArabic = isArabic,
                onDismiss = { showQrDialog = false },
                onImportPayload = { payload ->
                    viewModel.importListFromQr(payload)
                }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

@Composable
private fun LanguageOptionPill(
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
        if (isSelected) ElectricCyan.copy(alpha = 0.2f) else Color(0x18FFFFFF)
    }
    val pillBorder = if (isLight) {
        if (isSelected) BorderStroke(1.5.dp, CyanTintBorder) else BorderStroke(1.dp, LightCardBorder)
    } else {
        if (isSelected) BorderStroke(1.5.dp, ElectricCyan) else BorderStroke(1.dp, Color(0x20FFFFFF))
    }
    val titleColor = if (isLight) {
        if (isSelected) CyanTintText else TextPrimary
    } else {
        if (isSelected) ElectricCyan else TextPrimary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(pillBg)
            .border(pillBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                color = titleColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ThemeOptionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val pillBg = if (isLight) {
        if (isSelected) PeachTintBg else Color.White
    } else {
        if (isSelected) NeonAmber.copy(alpha = 0.2f) else Color(0x18FFFFFF)
    }
    val pillBorder = if (isLight) {
        if (isSelected) BorderStroke(1.5.dp, PeachTintBorder) else BorderStroke(1.dp, LightCardBorder)
    } else {
        if (isSelected) BorderStroke(1.5.dp, NeonAmber) else BorderStroke(1.dp, Color(0x20FFFFFF))
    }
    val pillAccent = if (isLight) PeachTintText else NeonAmber

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(pillBg)
            .border(pillBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) pillAccent else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    color = if (isSelected) pillAccent else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

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
