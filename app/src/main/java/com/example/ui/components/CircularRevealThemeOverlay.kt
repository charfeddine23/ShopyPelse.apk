package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CyanTintBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.NeonAmber
import kotlin.math.hypot
import kotlin.math.max

/**
 * Modern Glass Circular Reveal Effect for smooth Light/Dark theme transitions.
 * Sweeps an expanding circular wavefront of light, neon ring glow, and glass shimmer
 * from the theme switch toggle across the entire screen.
 */
@Composable
fun CircularRevealThemeOverlay(
    isTargetDark: Boolean,
    center: Offset,
    durationMillis: Int = 550,
    onMiddlePoint: () -> Unit,
    onAnimationEnd: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        var triggeredMidpoint = false
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            )
        ) {
            if (!triggeredMidpoint && this.value >= 0.40f) {
                triggeredMidpoint = true
                onMiddlePoint()
            }
        }
        onAnimationEnd()
    }

    val glowColor = if (isTargetDark) ElectricCyan else NeonAmber
    val secondaryGlow = if (isTargetDark) EmeraldSavings else CyanTintBorder
    val clipPath = remember { Path() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
    ) {
        val w = size.width
        val h = size.height
        val effectiveCenter = if (center == Offset.Zero) Offset(w * 0.85f, h * 0.32f) else center

        val maxRadius = hypot(
            max(effectiveCenter.x, w - effectiveCenter.x),
            max(effectiveCenter.y, h - effectiveCenter.y)
        ) * 1.08f

        val currentRadius = progress.value * maxRadius
        if (currentRadius <= 0f) return@Canvas

        clipPath.reset()
        clipPath.addOval(Rect(effectiveCenter, currentRadius))

        // 1. Draw Revealed Background with subtle soft glass aurora gradient
        clipPath(clipPath) {
            val bgColors = if (isTargetDark) {
                listOf(
                    BackgroundDark,
                    Color(0xFF0F172A),
                    BackgroundDark
                )
            } else {
                listOf(
                    BackgroundLight,
                    Color(0xFFE2E8F0),
                    BackgroundLight
                )
            }
            drawRect(
                brush = Brush.radialGradient(
                    colors = bgColors,
                    center = effectiveCenter,
                    radius = maxRadius
                )
            )

            // Inner glass ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = if (isTargetDark) 0.18f else 0.10f),
                        secondaryGlow.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = effectiveCenter,
                    radius = currentRadius
                ),
                center = effectiveCenter,
                radius = currentRadius
            )
        }

        // 2. Wavefront Glass Shockwave Ring on circumference
        val ringAlpha = (1f - (progress.value - 0.72f).coerceAtLeast(0f) / 0.28f).coerceIn(0f, 1f)

        if (ringAlpha > 0f) {
            // Outermost soft glowing aura
            drawCircle(
                color = glowColor.copy(alpha = ringAlpha * 0.35f),
                center = effectiveCenter,
                radius = currentRadius,
                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            )

            // Main neon glass edge ring
            drawCircle(
                color = glowColor.copy(alpha = ringAlpha * 0.90f),
                center = effectiveCenter,
                radius = currentRadius,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Sharp inner white highlight refraction edge
            drawCircle(
                color = Color.White.copy(alpha = ringAlpha * 0.65f),
                center = effectiveCenter,
                radius = (currentRadius - 2.dp.toPx()).coerceAtLeast(0f),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
