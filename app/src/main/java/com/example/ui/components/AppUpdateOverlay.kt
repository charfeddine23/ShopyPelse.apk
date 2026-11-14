package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.NeonAmber
import com.example.util.ApkDownloadState
import com.example.util.AppUpdateStatus
import java.util.Locale

private val CardGradientTop = Color(0xFF1A1D26)
private val CardGradientBottom = Color(0xFF101218)
private val UpdateAccentRed = Color(0xFFDF0E16)
private val UpdateAccentCrimson = Color(0xFFB91C1C)
private val UpdateSubtitleRed = Color(0xFFEF4444)
private val UpdateMutedGray = Color(0xFF94A3B8)

/**
 * Compact, luxurious floating In-App Update card centered over a dimmed backdrop.
 * Features the crimson rocket badge, version pill, concise notes, and live in-app
 * APK download progress + direct PackageInstaller trigger.
 */
@Composable
fun AppUpdateOverlay(
    updateInfo: AppUpdateStatus.UpdateAvailable,
    apkDownloadState: ApkDownloadState,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onUpdateClick: (String) -> Unit,
    onInstallClick: () -> Unit,
    onCancelDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val layoutDir = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    val cardShape = RoundedCornerShape(28.dp)

    // Dimmed Scrim Backdrop centering the compact luxury card
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Prevent pass-through clicks to underlying UI
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("app_update_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Compact Floating Luxury Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 340.dp)
                .wrapContentHeight()
                .shadow(
                    elevation = 24.dp,
                    shape = cardShape,
                    ambientColor = UpdateAccentRed.copy(alpha = 0.35f),
                    spotColor = UpdateAccentRed.copy(alpha = 0.50f)
                )
                .clip(cardShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CardGradientTop, CardGradientBottom)
                    )
                )
                .border(
                    BorderStroke(
                        1.2.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                UpdateSubtitleRed.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.16f),
                                ElectricCyan.copy(alpha = 0.30f),
                                UpdateAccentRed.copy(alpha = 0.45f)
                            )
                        )
                    ),
                    cardShape
                )
                .testTag("app_update_luxury_card")
        ) {
            // Subtle Top Specular Glass Hairline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.45f),
                                UpdateSubtitleRed.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Ambient subtle radial red glow behind the rocket at top center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                UpdateAccentRed.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            radius = 340f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: 'X' close button + sleek luxury version pill
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .testTag("update_overlay_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isArabic) "إغلاق" else "Close",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Compact Luxury Version Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(UpdateAccentRed.copy(alpha = 0.16f))
                                .border(
                                    BorderStroke(1.dp, UpdateSubtitleRed.copy(alpha = 0.55f)),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 11.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = updateInfo.latestVersion,
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Content in dynamic RTL/LTR direction
                CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Compact Animated Crimson Rocket Emblem
                        RocketUpdateBadge(
                            modifier = Modifier.size(92.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bold White Title
                        Text(
                            text = if (isArabic) "تحديث جديد متاح" else "New Update Available",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.2).sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Red Accent Subtitle
                        Text(
                            text = if (isArabic) "تحديث جديد" else "New Update",
                            color = UpdateSubtitleRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Concise Version Transition / Release Notes
                        val secondaryText = when {
                            updateInfo.releaseNotes.isNotBlank() -> updateInfo.releaseNotes.trim()
                            isArabic -> "إصدار ${updateInfo.latestVersion} جاهز للتحميل والتثبيت مباشرة"
                            else -> "Version ${updateInfo.latestVersion} is ready to download and install"
                        }
                        Text(
                            text = secondaryText,
                            color = UpdateMutedGray,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // In-App Download & Install Controls
                        AnimatedContent(
                            targetState = apkDownloadState,
                            contentKey = { it::class },
                            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                            label = "update_action_state"
                        ) { state ->
                            when (state) {
                                is ApkDownloadState.Idle -> {
                                    LuxuryUpdateActionButton(
                                        text = if (isArabic) "تحديث" else "Update",
                                        icon = Icons.Default.Download,
                                        gradientColors = listOf(Color(0xFFE5151E), UpdateAccentCrimson),
                                        onClick = { onUpdateClick(updateInfo.downloadUrl) },
                                        testTag = "update_overlay_action_button"
                                    )
                                }

                                is ApkDownloadState.Downloading -> {
                                    CompactDownloadProgressSection(
                                        state = state,
                                        isArabic = isArabic,
                                        onCancel = onCancelDownload
                                    )
                                }

                                is ApkDownloadState.Downloaded -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(EmeraldSavings.copy(alpha = 0.14f))
                                                .border(
                                                    BorderStroke(1.dp, EmeraldSavings.copy(alpha = 0.45f)),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = EmeraldSavings,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isArabic) "اكتمل التحميل بنجاح" else "Download complete",
                                                color = EmeraldSavings,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        LuxuryUpdateActionButton(
                                            text = if (isArabic) "تثبيت التحديث" else "Install Update",
                                            icon = Icons.Default.InstallMobile,
                                            gradientColors = listOf(EmeraldSavings, Color(0xFF059669)),
                                            onClick = onInstallClick,
                                            testTag = "update_overlay_install_button"
                                        )
                                    }
                                }

                                is ApkDownloadState.RequiresInstallPermission -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(NeonAmber.copy(alpha = 0.14f))
                                                .border(
                                                    BorderStroke(1.dp, NeonAmber.copy(alpha = 0.45f)),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = NeonAmber,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isArabic) {
                                                    "اسمح بالتثبيت من هذا المصدر ثم اضغط تثبيت"
                                                } else {
                                                    "Allow installs from this source, then tap Install"
                                                },
                                                color = NeonAmber,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        LuxuryUpdateActionButton(
                                            text = if (isArabic) "تثبيت الآن" else "Install Now",
                                            icon = Icons.Default.InstallMobile,
                                            gradientColors = listOf(EmeraldSavings, Color(0xFF059669)),
                                            onClick = onInstallClick,
                                            testTag = "update_overlay_permission_install_button"
                                        )
                                    }
                                }

                                is ApkDownloadState.Failed -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (isArabic) state.messageAr else state.messageEn,
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 11.5.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        LuxuryUpdateActionButton(
                                            text = if (isArabic) "إعادة المحاولة" else "Retry",
                                            icon = Icons.Default.Refresh,
                                            gradientColors = listOf(Color(0xFFE5151E), UpdateAccentCrimson),
                                            onClick = { onUpdateClick(updateInfo.downloadUrl) },
                                            testTag = "update_overlay_retry_button"
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LuxuryUpdateActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    testTag: String
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = gradientColors.first().copy(alpha = 0.45f),
                spotColor = gradientColors.first().copy(alpha = 0.55f)
            )
            .clip(shape)
            .background(Brush.horizontalGradient(gradientColors))
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CompactDownloadProgressSection(
    state: ApkDownloadState.Downloading,
    isArabic: Boolean,
    onCancel: () -> Unit
) {
    val hasTotal = state.totalBytes > 0L && state.progressPercent >= 0
    val animatedProgress by animateFloatAsState(
        targetValue = if (hasTotal) (state.progressPercent / 100f).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "apk_progress_anim"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x22FFFFFF))
            .border(BorderStroke(1.dp, Color(0x28FFFFFF)), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("update_overlay_progress_box"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "جاري التحميل..." else "Downloading...",
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (hasTotal) "${state.progressPercent}%" else formatBytesMb(state.downloadedBytes),
                color = Color(0xFFFCA5A5),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (hasTotal) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50)),
                color = UpdateSubtitleRed,
                trackColor = Color(0xFF262A36),
                strokeCap = StrokeCap.Round
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50)),
                color = UpdateSubtitleRed,
                trackColor = Color(0xFF262A36),
                strokeCap = StrokeCap.Round
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val sizeLabel = if (state.totalBytes > 0L) {
            "${formatBytesMb(state.downloadedBytes)} / ${formatBytesMb(state.totalBytes)}"
        } else {
            formatBytesMb(state.downloadedBytes)
        }
        Text(
            text = sizeLabel,
            color = UpdateMutedGray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onCancel
                )
                .testTag("update_overlay_cancel_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isArabic) "إلغاء التحميل" else "Cancel",
                color = UpdateMutedGray,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatBytesMb(bytes: Long): String {
    if (bytes <= 0L) return "0.0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 0.1) {
        String.format(Locale.US, "%.1f MB", mb)
    } else {
        val kb = bytes / 1024.0
        String.format(Locale.US, "%.0f KB", kb)
    }
}

/**
 * Custom Canvas Vector Rocket inside concentric red rings, matching the reference design.
 */
@Composable
private fun RocketUpdateBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "rocket_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val maxR = minOf(w, h) / 2f

        // Outer translucent dark-red aura ring
        drawCircle(
            color = Color(0xFF280E12),
            radius = maxR * ringPulse,
            center = Offset(cx, cy)
        )

        // Middle translucent dark-red ring
        drawCircle(
            color = Color(0xFF421015),
            radius = maxR * 0.84f,
            center = Offset(cx, cy)
        )

        // Inner dark obsidian space background
        val innerR = maxR * 0.68f
        drawCircle(
            color = Color(0xFF170B0D),
            radius = innerR,
            center = Offset(cx, cy)
        )

        // Crisp bright red border ring
        drawCircle(
            color = Color(0xFFDF0E16),
            radius = innerR,
            center = Offset(cx, cy),
            style = Stroke(width = 2.2.dp.toPx())
        )

        // Tiny cosmic square sparkles inside the dark circle
        val dotSize = maxR * 0.055f
        drawRect(
            color = Color(0xFF992228),
            topLeft = Offset(cx - innerR * 0.56f, cy - innerR * 0.44f),
            size = Size(dotSize, dotSize)
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(cx + innerR * 0.54f, cy - innerR * 0.50f),
            size = Size(dotSize * 1.15f, dotSize * 1.15f)
        )
        drawRect(
            color = Color(0xFFEAB308),
            topLeft = Offset(cx - innerR * 0.52f, cy + innerR * 0.42f),
            size = Size(dotSize, dotSize)
        )
        drawRect(
            color = Color(0xFFEF4444),
            topLeft = Offset(cx + innerR * 0.48f, cy + innerR * 0.36f),
            size = Size(dotSize, dotSize)
        )

        // Rocket exhaust flame (outer orange + inner yellow)
        val flameTopY = cy + innerR * 0.34f
        val flameTipY = flameTopY + innerR * 0.38f * flameScale
        val outerFlame = Path().apply {
            moveTo(cx - innerR * 0.13f, flameTopY)
            lineTo(cx, flameTipY)
            lineTo(cx + innerR * 0.13f, flameTopY)
            close()
        }
        drawPath(path = outerFlame, color = Color(0xFFF97316))

        val innerFlame = Path().apply {
            moveTo(cx - innerR * 0.075f, flameTopY)
            lineTo(cx, flameTopY + innerR * 0.24f * flameScale)
            lineTo(cx + innerR * 0.075f, flameTopY)
            close()
        }
        drawPath(path = innerFlame, color = Color(0xFFFACC15))

        // Left & Right Rocket Fins (crimson red)
        val leftFin = Path().apply {
            moveTo(cx - innerR * 0.16f, cy + innerR * 0.08f)
            lineTo(cx - innerR * 0.39f, cy + innerR * 0.35f)
            lineTo(cx - innerR * 0.14f, cy + innerR * 0.33f)
            close()
        }
        drawPath(path = leftFin, color = Color(0xFFB91C1C))

        val rightFin = Path().apply {
            moveTo(cx + innerR * 0.16f, cy + innerR * 0.08f)
            lineTo(cx + innerR * 0.39f, cy + innerR * 0.35f)
            lineTo(cx + innerR * 0.14f, cy + innerR * 0.33f)
            close()
        }
        drawPath(path = rightFin, color = Color(0xFFB91C1C))

        // Rocket White Fuselage Body
        val bodyTop = cy - innerR * 0.34f
        val bodyBottom = cy + innerR * 0.36f
        val bodyPath = Path().apply {
            moveTo(cx - innerR * 0.15f, bodyTop)
            quadraticTo(cx - innerR * 0.22f, cy + innerR * 0.05f, cx - innerR * 0.13f, bodyBottom)
            lineTo(cx + innerR * 0.13f, bodyBottom)
            quadraticTo(cx + innerR * 0.22f, cy + innerR * 0.05f, cx + innerR * 0.15f, bodyTop)
            close()
        }
        drawPath(path = bodyPath, color = Color.White)

        // Rocket Red Nose Cone
        val nosePath = Path().apply {
            moveTo(cx, cy - innerR * 0.62f)
            quadraticTo(cx - innerR * 0.14f, cy - innerR * 0.45f, cx - innerR * 0.15f, bodyTop)
            lineTo(cx + innerR * 0.15f, bodyTop)
            quadraticTo(cx + innerR * 0.14f, cy - innerR * 0.45f, cx, cy - innerR * 0.62f)
            close()
        }
        drawPath(path = nosePath, color = Color(0xFFDC2626))

        // Cyan Porthole Window with Dark Border
        val windowCenter = Offset(cx, cy - innerR * 0.06f)
        drawCircle(
            color = Color(0xFF1E293B),
            radius = innerR * 0.135f,
            center = windowCenter
        )
        drawCircle(
            color = Color(0xFF22D3EE),
            radius = innerR * 0.105f,
            center = windowCenter
        )
        // Subtle window reflection dot
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = innerR * 0.03f,
            center = Offset(windowCenter.x - innerR * 0.035f, windowCenter.y - innerR * 0.035f)
        )

        // Subtle Rocket Nozzle base
        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = Offset(cx - innerR * 0.09f, bodyBottom - 1.dp.toPx()),
            size = Size(innerR * 0.18f, innerR * 0.045f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}
