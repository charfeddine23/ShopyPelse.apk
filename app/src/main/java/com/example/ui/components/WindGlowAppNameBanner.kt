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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawBehind
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
import com.example.ui.theme.MintTintBorder
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated glowing wind background and banner for the app name ("Shopy Pulse" / "شوبي بلس").
 * Features ethereal, multi-colored wind streams, pulsating radial glow, and drifting light wisps
 * designed for silky 60fps animations in both Dark and Light modes.
 */
@Composable
fun WindGlowAppNameBanner(
    isArabic: Boolean,
    modifier: Modifier = Modifier,
    trailingAction: (@Composable () -> Unit)? = null
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    // Infinite transitions driving hyper-smooth wind currents and breathing aura
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

    // Palette tokens for the wind currents
    val cyanWindColor = if (isLight) Color(0xFF06B6D4) else ElectricCyan
    val emeraldWindColor = if (isLight) Color(0xFF10B981) else EmeraldSavings
    val violetWindColor = if (isLight) Color(0xFF8B5CF6) else Color(0xFFA78BFA)
    val amberWindColor = if (isLight) Color(0xFFF59E0B) else NeonAmber

    val containerBg = if (isLight) {
        Color(0xE6FFFFFF)
    } else {
        Color(0xD90E1726)
    }

    val containerBorder = if (isLight) {
        BorderStroke(1.2.dp, CyanTintBorder.copy(alpha = 0.55f))
    } else {
        BorderStroke(1.2.dp, ElectricCyan.copy(alpha = 0.35f * glowPulse))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(containerBg)
            .border(containerBorder, RoundedCornerShape(22.dp))
            .testTag("wind_glow_app_banner")
    ) {
        // Glowing Wind Streams Canvas (Behind the text)
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(22.dp))
        ) {
            val w = size.width
            val h = size.height

            // 1. Ambient Central Wind Aurora Bloom
            val bloomCenter = Offset(w * 0.35f, h * 0.5f)
            val bloomRadius = w * 0.75f
            val bloomColors = if (isLight) {
                listOf(
                    cyanWindColor.copy(alpha = 0.16f * glowPulse),
                    emeraldWindColor.copy(alpha = 0.12f * glowPulse),
                    violetWindColor.copy(alpha = 0.08f * glowPulse),
                    Color.Transparent
                )
            } else {
                listOf(
                    cyanWindColor.copy(alpha = 0.28f * glowPulse),
                    emeraldWindColor.copy(alpha = 0.20f * glowPulse),
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

            // 2. Wind Stream Ribbon 1 (Top Cyan Flow)
            val path1 = Path()
            val steps = 24
            val dx = w / steps
            for (i in 0..steps) {
                val x = i * dx
                val normX = (x / w).toDouble()
                val wave = sin(normX * 2.2 * PI + basePhase) * (6f * breezeSway) +
                        cos(normX * 1.5 * PI + basePhase * 0.5) * 3f
                val y = (h * 0.28f + wave).toFloat()
                if (i == 0) path1.moveTo(x, y) else path1.lineTo(x, y)
            }
            drawPath(
                path = path1,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        cyanWindColor.copy(alpha = 0.45f * glowPulse),
                        emeraldWindColor.copy(alpha = 0.50f * glowPulse),
                        violetWindColor.copy(alpha = 0.35f * glowPulse),
                        Color.Transparent
                    )
                ),
                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Wind Stream Ribbon 2 (Main Center-Bottom Emerald & Cyan Current)
            val path2 = Path()
            for (i in 0..steps) {
                val x = i * dx
                val normX = (x / w).toDouble()
                val wave = sin(normX * 2.8 * PI - basePhase * 1.2) * 8f +
                        cos(normX * 1.8 * PI + basePhase) * (4f * breezeSway)
                val y = (h * 0.68f + wave).toFloat()
                if (i == 0) path2.moveTo(x, y) else path2.lineTo(x, y)
            }
            drawPath(
                path = path2,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        violetWindColor.copy(alpha = 0.30f * glowPulse),
                        emeraldWindColor.copy(alpha = 0.55f * glowPulse),
                        cyanWindColor.copy(alpha = 0.45f * glowPulse),
                        Color.Transparent
                    )
                ),
                style = Stroke(width = 4.0.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Wind Stream Ribbon 3 (Lower Ethereal Amber/Mint Breeze)
            val path3 = Path()
            for (i in 0..steps) {
                val x = i * dx
                val normX = (x / w).toDouble()
                val wave = cos(normX * 3.2 * PI + basePhase * 1.5) * 5f
                val y = (h * 0.88f + wave).toFloat()
                if (i == 0) path3.moveTo(x, y) else path3.lineTo(x, y)
            }
            drawPath(
                path = path3,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        amberWindColor.copy(alpha = 0.25f * glowPulse),
                        emeraldWindColor.copy(alpha = 0.35f * glowPulse),
                        Color.Transparent
                    )
                ),
                style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round)
            )

            // 5. Drifting Wind Spark Wisps (Floating glowing specks riding the wind)
            val sparkOffsets = listOf(0.12f, 0.34f, 0.58f, 0.76f, 0.92f)
            for ((idx, offsetRatio) in sparkOffsets.withIndex()) {
                val sparkX = ((offsetRatio + windProgress * dir) % 1.0f).let { if (it < 0) it + 1f else it } * w
                val sparkNorm = (sparkX / w).toDouble()
                val sparkY = (h * (0.35f + 0.35f * sin(sparkNorm * 3 * PI + idx).toFloat())).coerceIn(12f, h - 12f)
                val sparkColor = if (idx % 2 == 0) cyanWindColor else emeraldWindColor

                // Soft outer glow of spark
                drawCircle(
                    color = sparkColor.copy(alpha = 0.30f * glowPulse),
                    radius = 5.dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
                // Sharp center dot
                drawCircle(
                    color = sparkColor.copy(alpha = 0.85f * glowPulse),
                    radius = 2.2.dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
            }
        }

        // Content Row: App Emblem + Glowing App Name + Subtitle + Trailing Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Glowing App Emblem Icon Container with pulsating neon border
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isLight) CyanTintBg else Color(0x3300E5FF)
                        )
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (isLight) CyanTintBorder else ElectricCyan.copy(alpha = 0.6f * glowPulse)
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shopy_pulse_logo),
                        contentDescription = "Shopy Pulse Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // App Name and Tagline
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isArabic) "شوبي بلس" else "Shopy Pulse",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.4).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Dynamic Wind Glow Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isLight) {
                                        Color(0xFFE0F2FE)
                                    } else {
                                        ElectricCyan.copy(alpha = 0.18f * glowPulse)
                                    }
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isLight) Color(0xFF38BDF8) else ElectricCyan.copy(alpha = 0.5f * glowPulse)
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
