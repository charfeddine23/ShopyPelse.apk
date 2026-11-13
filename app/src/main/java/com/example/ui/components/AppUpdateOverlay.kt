package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppUpdateStatus

private val UpdateScreenBackground = Color(0xFF131419)
private val UpdateAccentRed = Color(0xFFDF0E16)
private val UpdateSubtitleRed = Color(0xFFD61F26)
private val UpdateMutedGray = Color(0xFF8E929B)

/**
 * Full-screen In-App Update prompt matching the requested visual design:
 * top-left close icon, concentric red-ring rocket badge, bold title,
 * red accent subtitle, description/version line, and full-width red Update pill button.
 */
@Composable
fun AppUpdateOverlay(
    updateInfo: AppUpdateStatus.UpdateAvailable,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onUpdateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val layoutDir = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UpdateScreenBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Consume background taps
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("app_update_overlay")
    ) {
        // Top-Left Close Button ('X') matching reference screenshot
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .testTag("update_overlay_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = if (isArabic) "إغلاق" else "Close",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Main Centered Content
        CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.6f))

                // Concentric Red Rocket Emblem
                RocketUpdateBadge(
                    modifier = Modifier.size(144.dp)
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Bold White Title
                Text(
                    text = if (isArabic) "تحديث جديد متاح" else "New Update Available",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Red Accent Subtitle
                Text(
                    text = if (isArabic) "تحديث جديد" else "New Update",
                    color = UpdateSubtitleRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Muted Subtitle / Version Info
                val secondaryText = when {
                    updateInfo.releaseNotes.isNotBlank() ->
                        "${updateInfo.latestVersion} • ${updateInfo.releaseNotes}"
                    isArabic ->
                        "تحديث جديد متاح (${updateInfo.latestVersion})"
                    else ->
                        "New version available (${updateInfo.latestVersion})"
                }
                Text(
                    text = secondaryText,
                    color = UpdateMutedGray,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Full-width Crimson Red Pill Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(50))
                        .background(UpdateAccentRed)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = { onUpdateClick(updateInfo.downloadUrl) }
                        )
                        .testTag("update_overlay_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "تحديث" else "Update",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Custom Canvas Vector Rocket inside concentric red rings, matching the attached design.
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
            style = Stroke(width = 2.6.dp.toPx())
        )

        // Tiny cosmic square sparkles inside the dark circle (matching screenshot)
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
