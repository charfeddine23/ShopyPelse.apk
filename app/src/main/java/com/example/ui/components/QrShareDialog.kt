package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.QrCodeUtil

@Composable
fun QrShareDialog(
    payload: String,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onImportPayload: (String) -> Unit
) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isImportMode by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    LaunchedEffect(payload) {
        if (payload.isNotBlank()) {
            qrBitmap = QrCodeUtil.generateQrBitmap(payload, 450)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111928),
        modifier = Modifier.testTag("qr_share_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = EmeraldSavings,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isImportMode) {
                            if (isArabic) "استيراد قائمة تسوق" else "Import Shopping List"
                        } else {
                            if (isArabic) "مشاركة القائمة دون إنترنت" else "Offline QR Share"
                        },
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isImportMode) {
                    Text(
                        text = if (isArabic) {
                            "امسح رمز QR هذا من أي هاتف آخر لاستيراد القائمة فورياً ودون الحاجة للإنترنت."
                        } else {
                            "Scan this QR code from another device to transfer your shopping list offline."
                        },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(206.dp)
                            )
                        } else {
                            Text("Generating QR...", color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isArabic) "مشفر وآمن محلياً 100%" else "100% Local & Offline Encoded",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = if (isArabic) {
                            "الصق بيانات قائمة التسوق أو نص QR المشفر:"
                        } else {
                            "Paste shopping list payload or QR string:"
                        },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text("SOUK|...", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldSavings,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (!isImportMode) {
                GlowingActionButton(
                    text = if (isArabic) "استيراد قائمة يدوياً" else "Import Mode",
                    icon = Icons.Default.Download,
                    color = ElectricCyan,
                    onClick = { isImportMode = true }
                )
            } else {
                GlowingActionButton(
                    text = if (isArabic) "تأكيد الاستيراد" else "Import Items",
                    icon = Icons.Default.Download,
                    color = EmeraldSavings,
                    enabled = importText.isNotBlank(),
                    onClick = {
                        onImportPayload(importText)
                        onDismiss()
                    }
                )
            }
        },
        dismissButton = {
            if (isImportMode) {
                TextButton(onClick = { isImportMode = false }) {
                    Text(if (isArabic) "رجوع إلى الرمز" else "Back to QR", color = TextSecondary)
                }
            }
        }
    )
}
