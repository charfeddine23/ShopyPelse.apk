package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CyanTintBg
import com.example.ui.theme.CyanTintBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Top App Bar component for 'Shopy Pulse' featuring an ambient glowing wind effect layer
 * positioned strictly behind the card container, with smooth edge fade gradients on all sides
 * and 100% crisp foreground typography and iconography.
 */
@Composable
fun WindGlowAppNameBanner(
    isArabic: Boolean,
    modifier: Modifier = Modifier,
    isGlowEnabled: Boolean = true,
    trailingAction: (@Composable () -> Unit)? = null
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    // Infinite transitions for silky 60fps wind currents and breathing aura
    val infiniteTransition = rememberInfiniteTransition(label = "wind_glow_transition")

    // 1. Horizontal wind drift phase (0..1)
    val windProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wind_flow_progress"
    )

    // 2. Breathing luminous glow intensity
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // 3. Vertical breeze undulation
    val breezeSway by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breeze_sway"
    )

    // Palette tokens for ambient glow
    val cyanWindColor = if (isLight) Color(0xFF06B6D4) else ElectricCyan
    val emeraldWindColor = if (isLight) Color(0xFF10B981) else EmeraldSavings
    val violetWindColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFFA78BFA)
    val amberWindColor = if (isLight) Color(0xFFF59E0B) else NeonAmber

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wind_glow_app_banner")
    ) {
        if (isGlowEnabled) {
            val path1 = remember { Path() }
            val path2 = remember { Path() }
            // LAYER 1: STRICTLY BEHIND - Ambient glowing wind effect with soft edge fade (NO solid border)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .align(Alignment.Center)
            ) {
            val w = size.width
            val h = size.height

            // 1. Soft radial bloom centered behind title with full alpha fade on all edges
            val bloomCenter = Offset(if (isArabic) w * 0.68f else w * 0.32f, h * 0.5f)
            val bloomRadius = w * 0.65f
            val bloomColors = if (isLight) {
                listOf(
                    cyanWindColor.copy(alpha = 0.22f * glowPulse),
                    emeraldWindColor.copy(alpha = 0.15f * glowPulse),
                    violetWindColor.copy(alpha = 0.08f * glowPulse),
                    Color.Transparent
                )
            } else {
                listOf(
                    cyanWindColor.copy(alpha = 0.32f * glowPulse),
                    emeraldWindColor.copy(alpha = 0.22f * glowPulse),
                    violetWindColor.copy(alpha = 0.14f * glowPulse),
                    Color.Transparent
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = bloomColors,
                    center = bloomCenter,
                    radius = bloomRadius
                ),
                center = bloomCenter,
                radius = bloomRadius
            )

            // Direction multiplier for RTL vs LTR
            val dir = if (isArabic) -1f else 1f
            val basePhase = windProgress * 2.0 * PI * dir

            // 2. Wind Stream Ribbon 1 (Top Cyan Flow with 4-way soft alpha fade)
            path1.reset()
            val steps = 30
            val dx = w / steps
            for (i in 0..steps) {
                val x = i * dx
                val normX = (x / w).toDouble()
                val wave = sin(normX * 2.2 * PI + basePhase) * (6f * breezeSway) +
                        cos(normX * 1.5 * PI + basePhase * 0.5) * 3f
                val y = (h * 0.30f + wave).toFloat()
                if (i == 0) path1.moveTo(x, y) else path1.lineTo(x, y)
            }
            drawPath(
                path = path1,
                brush = Brush.horizontalGradient(
                    0.00f to Color.Transparent,
                    0.15f to cyanWindColor.copy(alpha = 0.40f * glowPulse),
                    0.50f to emeraldWindColor.copy(alpha = 0.45f * glowPulse),
                    0.85f to violetWindColor.copy(alpha = 0.30f * glowPulse),
                    1.00f to Color.Transparent
                ),
                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Wind Stream Ribbon 2 (Main Lower Stream with soft alpha fade)
            path2.reset()
            for (i in 0..steps) {
                val x = i * dx
                val normX = (x / w).toDouble()
                val wave = sin(normX * 2.8 * PI - basePhase * 1.2) * 7f +
                        cos(normX * 1.8 * PI + basePhase) * (4f * breezeSway)
                val y = (h * 0.70f + wave).toFloat()
                if (i == 0) path2.moveTo(x, y) else path2.lineTo(x, y)
            }
            drawPath(
                path = path2,
                brush = Brush.horizontalGradient(
                    0.00f to Color.Transparent,
                    0.18f to violetWindColor.copy(alpha = 0.28f * glowPulse),
                    0.55f to emeraldWindColor.copy(alpha = 0.50f * glowPulse),
                    0.82f to cyanWindColor.copy(alpha = 0.40f * glowPulse),
                    1.00f to Color.Transparent
                ),
                style = Stroke(width = 3.6.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Drifting glowing wind sparks along the wind
            val sparkOffsets = listOf(0.15f, 0.35f, 0.60f, 0.82f)
            for ((idx, offsetRatio) in sparkOffsets.withIndex()) {
                val sparkX = ((offsetRatio + windProgress * dir) % 1.0f).let { if (it < 0) it + 1f else it } * w
                val sparkNorm = (sparkX / w).toDouble()
                val edgeAlpha = (sin(sparkNorm * PI)).toFloat().coerceIn(0f, 1f)
                val sparkY = (h * (0.35f + 0.30f * sin(sparkNorm * 3 * PI + idx).toFloat())).coerceIn(10f, h - 10f)
                val sparkColor = if (idx % 2 == 0) cyanWindColor else emeraldWindColor

                drawCircle(
                    color = sparkColor.copy(alpha = 0.25f * glowPulse * edgeAlpha),
                    radius = 5.dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
                drawCircle(
                    color = sparkColor.copy(alpha = 0.80f * glowPulse * edgeAlpha),
                    radius = 2.dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
            }
        }
        }

        // LAYER 2: FOREGROUND CARD CONTAINER - Sits strictly in front of the glow layer
        // Protected by modern soft-glass surface so title, icons, and text are crisp and unaffected by color blending
        val cardBg = if (isLight) {
            Color(0xF0FFFFFF)
        } else {
            Color(0xE6101726)
        }
        val cardBorder = if (isLight) {
            BorderStroke(1.dp, CyanTintBorder.copy(alpha = 0.35f))
        } else {
            BorderStroke(1.dp, Color(0x3338BDF8))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .border(cardBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // App Emblem Icon Container (Crisp & Sharp)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (isLight) CyanTintBg else Color(0x3300E5FF)
                            )
                            .border(
                                BorderStroke(
                                    1.2.dp,
                                    if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.6f)
                                ),
                                RoundedCornerShape(13.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shopy_pulse_logo),
                            contentDescription = "Shopy Pulse Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // App Title & Tagline (100% Crisp, completely unaffected by glow blending)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isArabic) "شوبي بلس" else "Shopy Pulse",
                                color = TextPrimary,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.4).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Pulse Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isLight) Color(0xFFE0F2FE) else ElectricCyan.copy(alpha = 0.16f)
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isLight) Color(0xFF38BDF8) else ElectricCyan.copy(alpha = 0.45f)
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isLight) Color(0xFF0284C7) else ElectricCyan,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isArabic) "نبض ذكي" else "PULSE",
                                        color = if (isLight) Color(0xFF0284C7) else ElectricCyan,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (isArabic) "مقارنة الأسعار الذكية وقائمة التسوق" else "Smart Price Comparison & Shopping",
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (trailingAction != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingAction()
                }
            }
        }
    }
}
