package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CyanTintBg
import com.example.ui.theme.CyanTintBorder
import com.example.ui.theme.CyanTintText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.MintTintBg
import com.example.ui.theme.MintTintBorder
import com.example.ui.theme.MintTintText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.QrCodeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QrShareDialog(
    payload: String,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onImportPayload: (String) -> Unit,
    initialImportMode: Boolean = false
) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isImportMode by remember { mutableStateOf(initialImportMode) }
    var importText by remember { mutableStateOf("") }
    var copySuccess by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    LaunchedEffect(payload) {
        if (payload.isNotBlank()) {
            qrBitmap = QrCodeUtil.generateQrBitmap(payload, 450)
        }
    }

    val dialogBg = if (isLight) Color(0xFFFFFFFF) else Color(0xFF131D2E)
    val dialogBorder = if (isLight) LightCardBorder else GlassBorder

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .border(BorderStroke(1.dp, dialogBorder), RoundedCornerShape(22.dp))
            .testTag("qr_share_dialog"),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar: Icon, Title & Close Button
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLight) MintTintBg else EmeraldSavings.copy(alpha = 0.2f))
                                .border(
                                    BorderStroke(1.dp, if (isLight) MintTintBorder else EmeraldSavings.copy(alpha = 0.6f)),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = if (isLight) MintTintText else EmeraldSavings,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "المزامنة المباشرة بـ QR Code" else "Direct QR Sync",
                                color = TextPrimary,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isArabic) "مشاركة واستيراد فوري بدون إنترنت" else "Instant offline list sync",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Harmonized Mode Switcher Segmented Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLight) Color(0xFFF1F5F9) else Color(0x1F2A3852))
                        .border(
                            BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0x28FFFFFF)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 1: Share (مشاركة)
                    val isShareActive = !isImportMode
                    val shareTabBg = if (isShareActive) {
                        if (isLight) Color.White else EmeraldSavings.copy(alpha = 0.22f)
                    } else Color.Transparent
                    val shareTabBorder = if (isShareActive) {
                        BorderStroke(1.dp, if (isLight) MintTintBorder else EmeraldSavings.copy(alpha = 0.7f))
                    } else BorderStroke(0.dp, Color.Transparent)
                    val shareTabColor = if (isShareActive) {
                        if (isLight) MintTintText else EmeraldSavings
                    } else TextSecondary

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(shareTabBg)
                            .border(shareTabBorder, RoundedCornerShape(9.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { isImportMode = false }
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = shareTabColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "مشاركة القائمة" else "Share List",
                                color = shareTabColor,
                                fontSize = 12.5.sp,
                                fontWeight = if (isShareActive) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    // Tab 2: Import (استيراد)
                    val isImportActive = isImportMode
                    val importTabBg = if (isImportActive) {
                        if (isLight) Color.White else ElectricCyan.copy(alpha = 0.22f)
                    } else Color.Transparent
                    val importTabBorder = if (isImportActive) {
                        BorderStroke(1.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.7f))
                    } else BorderStroke(0.dp, Color.Transparent)
                    val importTabColor = if (isImportActive) {
                        if (isLight) CyanTintText else ElectricCyan
                    } else TextSecondary

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(importTabBg)
                            .border(importTabBorder, RoundedCornerShape(9.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { isImportMode = true }
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = importTabColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "استيراد ومسح" else "Import QR",
                                color = importTabColor,
                                fontSize = 12.5.sp,
                                fontWeight = if (isImportActive) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isImportMode) {
                    // SHARE MODE
                    Text(
                        text = if (isArabic) {
                            "امسح رمز QR هذا من أي هاتف آخر لاستيراد القائمة فورياً ودون الحاجة للإنترنت."
                        } else {
                            "Scan this QR code from another device to transfer your shopping list offline."
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Canvas Container with Glowing Border & Clean Rounded Frame
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(
                                BorderStroke(1.5.dp, if (isLight) MintTintBorder else EmeraldSavings.copy(alpha = 0.6f)),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Shopping List QR Code",
                                modifier = Modifier.size(190.dp)
                            )
                        } else {
                            Text(
                                text = if (isArabic) "جاري توليد كود QR..." else "Generating QR...",
                                color = Color.DarkGray,
                                fontSize = 12.sp
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(12.dp))

                    // Copy Code Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLight) Color(0xFFF1F5F9) else Color(0x1AFFFFFF))
                            .border(
                                BorderStroke(1.dp, if (isLight) Color(0xFFCBD5E1) else Color(0x28FFFFFF)),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    if (payload.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(payload))
                                        copySuccess = true
                                        coroutineScope.launch {
                                            delay(2000)
                                            copySuccess = false
                                        }
                                    }
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (copySuccess) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (copySuccess) EmeraldSavings else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (copySuccess) {
                                    if (isArabic) "تم نسخ الكود للحافظة!" else "Copied to clipboard!"
                                } else {
                                    if (isArabic) "نسخ نص كود الـ QR" else "Copy QR Payload"
                                },
                                color = if (copySuccess) EmeraldSavings else TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // IMPORT MODE
                    Text(
                        text = if (isArabic) {
                            "الصق بيانات كود QR المشفر لاستيراد عناصر قائمة التسوق إلى جهازك فوراً:"
                        } else {
                            "Paste shopping list QR string below to import items to your list:"
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = {
                            Text(
                                text = "SOUK|...",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(105.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = if (isLight) LightCardBorder else GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = if (isLight) Color(0xFFF8FAFC) else Color(0x1406141D),
                            unfocusedContainerColor = if (isLight) Color(0xFFF8FAFC) else Color(0x1406141D)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Paste from Clipboard Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLight) CyanTintBg else ElectricCyan.copy(alpha = 0.15f))
                            .border(
                                BorderStroke(1.dp, if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.45f)),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    val clip = clipboardManager.getText()
                                    if (clip != null && clip.text.isNotBlank()) {
                                        importText = clip.text
                                    }
                                }
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = if (isLight) CyanTintText else ElectricCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "📋 لصق النص من الحافظة" else "📋 Paste from Clipboard",
                                color = if (isLight) CyanTintText else ElectricCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isImportMode) {
                // Confirm Import Action Button
                val isImportEnabled = importText.isNotBlank()
                val importBtnBrush = if (isImportEnabled) {
                    Brush.horizontalGradient(listOf(EmeraldSavings, EmeraldSavings.copy(alpha = 0.85f)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(importBtnBrush)
                        .clickable(
                            enabled = isImportEnabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = {
                                onImportPayload(importText)
                                onDismiss()
                            }
                        )
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "تأكيد واستيراد القائمة" else "Import Items Now",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // In Share Mode: Done Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    EmeraldSavings,
                                    EmeraldSavings.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = onDismiss
                        )
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "تم / إغلاق" else "Done",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = null
    )
}
