package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.NeonAmber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal val TutorialViolet = Color(0xFFA855F7)
internal val TutorialSky = Color(0xFF38BDF8)

/**
 * Renders ultra-crisp, GPU-accelerated vector Canvas animations tailored to each tutorial step.
 */
@Composable
fun TutorialStepAnimatedScene(
    stepIndex: Int,
    accentColor: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tutorial_scene_$stepIndex")
    val linearCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "linear_cycle"
    )
    val smoothWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "smooth_wave"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (stepIndex) {
            0 -> WelcomePulseScene(linearCycle = linearCycle, smoothWave = smoothWave, accentColor = accentColor, isDarkMode = isDarkMode)
            1 -> PullUpAddScene(linearCycle = linearCycle, accentColor = accentColor, isDarkMode = isDarkMode)
            2 -> PriceComparisonScene(smoothWave = smoothWave, linearCycle = linearCycle, accentColor = accentColor, isDarkMode = isDarkMode)
            3 -> SupermarketBudgetScene(linearCycle = linearCycle, smoothWave = smoothWave, accentColor = accentColor, isDarkMode = isDarkMode)
            4 -> SmartGesturesFullScreenScene(linearCycle = linearCycle, accentColor = accentColor, isDarkMode = isDarkMode)
            else -> OfflineQrAndUpdateScene(linearCycle = linearCycle, smoothWave = smoothWave, accentColor = accentColor, isDarkMode = isDarkMode)
        }
    }
}

@Composable
private fun WelcomePulseScene(
    linearCycle: Float,
    smoothWave: Float,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseR = minOf(size.width, size.height) * 0.25f

        // Expanding concentric sonar pulse rings
        for (i in 0 until 3) {
            val phase = (linearCycle + i / 3f) % 1f
            val r = baseR * (1.05f + phase * 1.45f)
            val ringAlpha = (1f - phase) * 0.42f
            drawCircle(
                color = accentColor.copy(alpha = ringAlpha),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Orbiting luxury neon micro-particles
        val orbitRx = baseR * 1.65f
        val orbitRy = baseR * 1.15f
        for (i in 0 until 6) {
            val angle = (linearCycle * 2f * PI + i * (PI / 3.0))
            val px = cx + (cos(angle) * orbitRx).toFloat()
            val py = cy + (sin(angle) * orbitRy).toFloat()
            val dotColor = when (i % 3) {
                0 -> ElectricCyan
                1 -> EmeraldSavings
                else -> NeonAmber
            }
            val dotR = (3.dp + (i % 2).dp).toPx()
            drawCircle(
                color = dotColor.copy(alpha = 0.25f),
                radius = dotR * 2.4f,
                center = Offset(px, py)
            )
            drawCircle(
                color = dotColor,
                radius = dotR,
                center = Offset(px, py)
            )
        }

        // Central frosted glass core
        val coreR = baseR * (0.96f + smoothWave * 0.06f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.34f),
                    EmeraldSavings.copy(alpha = 0.16f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = coreR * 1.75f
            ),
            radius = coreR * 1.75f,
            center = Offset(cx, cy)
        )

        drawCircle(
            brush = Brush.linearGradient(
                colors = if (isDarkMode) {
                    listOf(Color(0xFF172742), Color(0xFF0C1526))
                } else {
                    listOf(Color.White, Color(0xFFE0F2FE))
                }
            ),
            radius = coreR,
            center = Offset(cx, cy)
        )

        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(ElectricCyan, EmeraldSavings, TutorialViolet)
            ),
            radius = coreR,
            center = Offset(cx, cy),
            style = Stroke(width = 2.4.dp.toPx())
        )

        // Crisp vector shopping cart silhouette with pulse wave inside
        val strokeW = 2.6.dp.toPx()
        val cartColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
        val cartPath = Path().apply {
            moveTo(cx - coreR * 0.44f, cy - coreR * 0.24f)
            lineTo(cx - coreR * 0.26f, cy - coreR * 0.24f)
            lineTo(cx - coreR * 0.14f, cy + coreR * 0.16f)
            lineTo(cx + coreR * 0.32f, cy + coreR * 0.16f)
            lineTo(cx + coreR * 0.44f, cy - coreR * 0.12f)
            lineTo(cx - coreR * 0.20f, cy - coreR * 0.12f)
        }
        drawPath(
            path = cartPath,
            color = cartColor,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(color = EmeraldSavings, radius = 3.2.dp.toPx(), center = Offset(cx - coreR * 0.10f, cy + coreR * 0.34f))
        drawCircle(color = ElectricCyan, radius = 3.2.dp.toPx(), center = Offset(cx + coreR * 0.28f, cy + coreR * 0.34f))

        // Heartbeat / price pulse arc above cart
        val pulsePath = Path().apply {
            val py = cy - coreR * 0.40f
            moveTo(cx - coreR * 0.36f, py)
            lineTo(cx - coreR * 0.16f, py)
            lineTo(cx - coreR * 0.06f, py - coreR * 0.18f * (0.6f + 0.4f * smoothWave))
            lineTo(cx + coreR * 0.06f, py + coreR * 0.14f * (0.6f + 0.4f * smoothWave))
            lineTo(cx + coreR * 0.16f, py)
            lineTo(cx + coreR * 0.36f, py)
        }
        drawPath(
            path = pulsePath,
            color = EmeraldSavings,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun PullUpAddScene(
    linearCycle: Float,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val phoneW = minOf(w * 0.62f, 170.dp.toPx())
        val phoneH = minOf(h * 0.84f, 190.dp.toPx())
        val phoneLeft = cx - phoneW / 2f
        val phoneTop = (h - phoneH) / 2f

        val cardBg = if (isDarkMode) Color(0xFF101C30) else Color.White
        val surfaceBg = if (isDarkMode) Color(0xFF182843) else Color(0xFFF1F5F9)

        // Outer mini glass frame
        drawRoundRect(
            color = cardBg,
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.7f), EmeraldSavings.copy(alpha = 0.45f))),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
            style = Stroke(width = 1.6.dp.toPx())
        )

        // Phase: 0.0..0.55 pull up, 0.55..0.85 sheet popped & confirmed, 0.85..1.0 reset
        val pullT = (linearCycle / 0.52f).coerceIn(0f, 1f)
        val sheetPop = ((linearCycle - 0.48f) / 0.18f).coerceIn(0f, 1f)
        val fadeReset = if (linearCycle > 0.86f) 1f - ((linearCycle - 0.86f) / 0.14f) else 1f

        val innerLift = pullT * 18.dp.toPx() * fadeReset

        // Empty list card gliding up
        val emptyCardW = phoneW * 0.82f
        val emptyCardH = phoneH * 0.44f
        val emptyLeft = cx - emptyCardW / 2f
        val emptyTop = phoneTop + phoneH * 0.24f - innerLift

        drawRoundRect(
            color = surfaceBg.copy(alpha = fadeReset),
            topLeft = Offset(emptyLeft, emptyTop),
            size = Size(emptyCardW, emptyCardH),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
        )
        drawRoundRect(
            color = accentColor.copy(alpha = (0.35f + 0.55f * pullT) * fadeReset),
            topLeft = Offset(emptyLeft, emptyTop),
            size = Size(emptyCardW, emptyCardH),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
            style = Stroke(width = 1.3.dp.toPx())
        )

        // Glowing Pull-Up Pill inside card
        val pillW = emptyCardW * 0.72f
        val pillH = 22.dp.toPx()
        val pillLeft = cx - pillW / 2f
        val pillTop = emptyTop + emptyCardH - pillH - 12.dp.toPx()
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(
                    (if (pullT > 0.7f) EmeraldSavings else accentColor).copy(alpha = 0.85f * fadeReset),
                    ElectricCyan.copy(alpha = 0.7f * fadeReset)
                )
            ),
            topLeft = Offset(pillLeft, pillTop),
            size = Size(pillW, pillH),
            cornerRadius = CornerRadius(11.dp.toPx(), 11.dp.toPx())
        )

        // Animated popping bottom sheet card showing "+ Item Added"
        if (sheetPop > 0.02f) {
            val sH = phoneH * 0.34f * sheetPop
            val sTop = phoneTop + phoneH - sH - 6.dp.toPx()
            drawRoundRect(
                color = if (isDarkMode) Color(0xFF0E293B) else Color(0xFFECFDF5),
                topLeft = Offset(phoneLeft + 8.dp.toPx(), sTop),
                size = Size(phoneW - 16.dp.toPx(), sH),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )
            drawRoundRect(
                color = EmeraldSavings.copy(alpha = sheetPop * fadeReset),
                topLeft = Offset(phoneLeft + 8.dp.toPx(), sTop),
                size = Size(phoneW - 16.dp.toPx(), sH),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Crisp green checkmark inside popped sheet
            val ckCx = cx
            val ckCy = sTop + sH * 0.52f
            val ckR = 11.dp.toPx() * sheetPop
            drawCircle(color = EmeraldSavings.copy(alpha = 0.22f * fadeReset), radius = ckR * 1.4f, center = Offset(ckCx, ckCy))
            val ckPath = Path().apply {
                moveTo(ckCx - ckR * 0.55f, ckCy)
                lineTo(ckCx - ckR * 0.1f, ckCy + ckR * 0.45f)
                lineTo(ckCx + ckR * 0.65f, ckCy - ckR * 0.4f)
            }
            drawPath(
                path = ckPath,
                color = EmeraldSavings.copy(alpha = fadeReset),
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Animated single-finger touch trail + pointer dot
        val fingerStartY = phoneTop + phoneH * 0.78f
        val fingerEndY = phoneTop + phoneH * 0.36f
        val fingerY = fingerStartY + (fingerEndY - fingerStartY) * pullT
        val fingerAlpha = if (linearCycle < 0.58f) 1f else (1f - ((linearCycle - 0.58f) / 0.15f)).coerceIn(0f, 1f)
        if (fingerAlpha > 0.01f) {
            drawLine(
                brush = Brush.verticalGradient(listOf(ElectricCyan.copy(alpha = fingerAlpha), Color.Transparent)),
                start = Offset(cx, fingerY),
                end = Offset(cx, fingerStartY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = ElectricCyan.copy(alpha = 0.28f * fingerAlpha),
                radius = 16.dp.toPx(),
                center = Offset(cx, fingerY)
            )
            drawCircle(
                color = Color.White.copy(alpha = fingerAlpha),
                radius = 7.dp.toPx(),
                center = Offset(cx, fingerY)
            )
        }
    }
}

@Composable
private fun PriceComparisonScene(
    smoothWave: Float,
    linearCycle: Float,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val barWidth = 34.dp.toPx()
        val gap = 22.dp.toPx()
        val totalGroupW = barWidth * 3 + gap * 2
        val startX = (w - totalGroupW) / 2f
        val baselineY = h * 0.80f
        val maxBarH = h * 0.52f

        val baseColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        // Store 1 (medium), Store 2 (lowest = best!), Store 3 (tallest)
        val ratios = listOf(
            0.74f + 0.06f * smoothWave,
            0.46f - 0.04f * smoothWave,
            0.92f - 0.05f * smoothWave
        )
        val barColors = listOf(
            listOf(ElectricCyan.copy(alpha = 0.85f), ElectricCyan.copy(alpha = 0.35f)),
            listOf(EmeraldSavings, EmeraldSavings.copy(alpha = 0.55f)),
            listOf(NeonAmber.copy(alpha = 0.85f), NeonAmber.copy(alpha = 0.35f))
        )

        // Baseline track
        drawLine(
            color = if (isDarkMode) Color(0x33FFFFFF) else Color(0x330F172A),
            start = Offset(startX - 18.dp.toPx(), baselineY),
            end = Offset(startX + totalGroupW + 18.dp.toPx(), baselineY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        ratios.forEachIndexed { idx, ratio ->
            val bx = startX + idx * (barWidth + gap)
            val bh = maxBarH * ratio
            val by = baselineY - bh

            // Track silhouette
            drawRoundRect(
                color = baseColor.copy(alpha = 0.45f),
                topLeft = Offset(bx, baselineY - maxBarH),
                size = Size(barWidth, maxBarH),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )

            // Animated rising value bar
            drawRoundRect(
                brush = Brush.verticalGradient(barColors[idx]),
                topLeft = Offset(bx, by),
                size = Size(barWidth, bh),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )

            if (idx == 1) {
                // Best-value glowing halo & crown star above lowest price bar
                val starY = by - 18.dp.toPx() - smoothWave * 4.dp.toPx()
                val starCx = bx + barWidth / 2f
                drawCircle(
                    color = EmeraldSavings.copy(alpha = 0.25f + 0.15f * smoothWave),
                    radius = 15.dp.toPx(),
                    center = Offset(starCx, starY)
                )
                drawCircle(
                    color = EmeraldSavings,
                    radius = 8.dp.toPx(),
                    center = Offset(starCx, starY)
                )
                // Mini crisp check inside best badge
                val check = Path().apply {
                    moveTo(starCx - 3.5.dp.toPx(), starY)
                    lineTo(starCx - 1.dp.toPx(), starY + 2.8.dp.toPx())
                    lineTo(starCx + 4.dp.toPx(), starY - 2.8.dp.toPx())
                }
                drawPath(
                    path = check,
                    color = Color.White,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        // Scanning horizontal laser accent
        val scanY = baselineY - maxBarH * ((linearCycle * 1.1f) % 1f)
        drawLine(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, accentColor.copy(alpha = 0.65f), Color.Transparent)),
            start = Offset(startX - 16.dp.toPx(), scanY),
            end = Offset(startX + totalGroupW + 16.dp.toPx(), scanY),
            strokeWidth = 1.6.dp.toPx()
        )
    }
}

@Composable
private fun SupermarketBudgetScene(
    linearCycle: Float,
    smoothWave: Float,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f + 6.dp.toPx()
        val radius = minOf(size.width, size.height) * 0.34f
        val strokeW = 12.dp.toPx()

        val trackColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        // Background 270° gauge arc
        drawArc(
            color = trackColor,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // Smooth animated sweep progress
        val progress = 0.25f + 0.65f * smoothWave
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(ElectricCyan, EmeraldSavings, NeonAmber, ElectricCyan),
                center = Offset(cx, cy)
            ),
            startAngle = 135f,
            sweepAngle = 270f * progress,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // Glowing needle head at tip of arc
        val tipAngleRad = ((135f + 270f * progress) * PI / 180.0)
        val tipX = cx + (cos(tipAngleRad) * radius).toFloat()
        val tipY = cy + (sin(tipAngleRad) * radius).toFloat()
        drawCircle(color = EmeraldSavings.copy(alpha = 0.35f), radius = 12.dp.toPx(), center = Offset(tipX, tipY))
        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(tipX, tipY))

        // Inner checklist rows simulating live cart check-off
        val rowW = radius * 1.1f
        for (i in 0 until 3) {
            val ry = cy - 18.dp.toPx() + i * 18.dp.toPx()
            val rx = cx - rowW / 2f
            val checked = ((linearCycle * 3f) % 3f) > i
            val dotC = if (checked) EmeraldSavings else accentColor.copy(alpha = 0.4f)
            drawCircle(color = dotC, radius = 4.5.dp.toPx(), center = Offset(rx + 6.dp.toPx(), ry))
            drawRoundRect(
                color = if (checked) EmeraldSavings.copy(alpha = 0.55f) else trackColor,
                topLeft = Offset(rx + 18.dp.toPx(), ry - 3.dp.toPx()),
                size = Size(rowW - 20.dp.toPx(), 6.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }
    }
}

@Composable
private fun SmartGesturesFullScreenScene(
    linearCycle: Float,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val frameW = minOf(w * 0.60f, 165.dp.toPx())
        val frameH = minOf(h * 0.84f, 188.dp.toPx())
        val left = cx - frameW / 2f
        val top = (h - frameH) / 2f

        val cardBg = if (isDarkMode) Color(0xFF0E182B) else Color.White
        drawRoundRect(
            color = cardBg,
            topLeft = Offset(left, top),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(accentColor, ElectricCyan)),
            topLeft = Offset(left, top),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
            style = Stroke(width = 1.6.dp.toPx())
        )

        // Two-finger animation cycle:
        // 0.00..0.38 -> swipe UP -> status bar hides
        // 0.38..0.55 -> hold full screen glow
        // 0.55..0.90 -> swipe DOWN -> status bar shows
        val isSwipingUp = linearCycle < 0.48f
        val moveFrac = if (isSwipingUp) {
            (linearCycle / 0.38f).coerceIn(0f, 1f)
        } else {
            ((linearCycle - 0.52f) / 0.38f).coerceIn(0f, 1f)
        }
        val statusBarVisibleAlpha = if (isSwipingUp) {
            (1f - moveFrac * 1.4f).coerceIn(0f, 1f)
        } else {
            (moveFrac * 1.4f).coerceIn(0f, 1f)
        }

        // Top mini status bar pill (fades out cleanly while inner card stays stationary!)
        drawRoundRect(
            color = (if (isDarkMode) Color.White else Color(0xFF0F172A)).copy(alpha = 0.65f * statusBarVisibleAlpha),
            topLeft = Offset(cx - 26.dp.toPx(), top + 9.dp.toPx()),
            size = Size(52.dp.toPx(), 5.dp.toPx()),
            cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
        )

        // Stationary inner card showing pages never shift
        val contentTop = top + 24.dp.toPx()
        val contentH = frameH - 38.dp.toPx()
        drawRoundRect(
            color = if (isDarkMode) Color(0xFF172742) else Color(0xFFF1F5F9),
            topLeft = Offset(left + 12.dp.toPx(), contentTop),
            size = Size(frameW - 24.dp.toPx(), contentH),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
        )

        // Two synchronized finger dots moving up then down
        val lowY = top + frameH * 0.74f
        val highY = top + frameH * 0.36f
        val fingerY = if (isSwipingUp) {
            lowY + (highY - lowY) * moveFrac
        } else {
            highY + (lowY - highY) * moveFrac
        }
        val fingerColor = if (isSwipingUp) EmeraldSavings else NeonAmber
        val fingerSpacing = 22.dp.toPx()

        listOf(cx - fingerSpacing, cx + fingerSpacing).forEach { fx ->
            val trailStart = if (isSwipingUp) lowY else highY
            drawLine(
                brush = Brush.verticalGradient(
                    colors = if (isSwipingUp) {
                        listOf(fingerColor.copy(alpha = 0.8f), Color.Transparent)
                    } else {
                        listOf(Color.Transparent, fingerColor.copy(alpha = 0.8f))
                    }
                ),
                start = Offset(fx, fingerY),
                end = Offset(fx, trailStart),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = fingerColor.copy(alpha = 0.25f), radius = 13.dp.toPx(), center = Offset(fx, fingerY))
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(fx, fingerY))
        }
    }
}

@Composable
private fun OfflineQrAndUpdateScene(
    linearCycle: Float,
    smoothWave: Float,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val qrSize = minOf(size.width, size.height) * 0.56f
        val qrLeft = cx - qrSize / 2f
        val qrTop = cy - qrSize / 2f

        // Backing glow card
        drawRoundRect(
            color = if (isDarkMode) Color(0xFF0E1B2E) else Color.White,
            topLeft = Offset(qrLeft - 14.dp.toPx(), qrTop - 14.dp.toPx()),
            size = Size(qrSize + 28.dp.toPx(), qrSize + 28.dp.toPx()),
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
        )
        drawRoundRect(
            brush = Brush.linearGradient(listOf(ElectricCyan, EmeraldSavings)),
            topLeft = Offset(qrLeft - 14.dp.toPx(), qrTop - 14.dp.toPx()),
            size = Size(qrSize + 28.dp.toPx(), qrSize + 28.dp.toPx()),
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
            style = Stroke(width = 1.6.dp.toPx())
        )

        // Crisp 5x5 stylized QR matrix
        val cells = 5
        val cellSize = qrSize / cells
        val pattern = listOf(
            listOf(1, 1, 0, 1, 1),
            listOf(1, 0, 1, 0, 1),
            listOf(0, 1, 1, 1, 0),
            listOf(1, 0, 1, 0, 1),
            listOf(1, 1, 0, 1, 0)
        )
        val moduleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
        for (r in 0 until cells) {
            for (c in 0 until cells) {
                if (pattern[r][c] == 1) {
                    val isCorner = (r == 0 && c == 0) || (r == 0 && c == 4) || (r == 4 && c == 0)
                    drawRoundRect(
                        color = if (isCorner) accentColor else moduleColor.copy(alpha = 0.85f),
                        topLeft = Offset(qrLeft + c * cellSize + 3.dp.toPx(), qrTop + r * cellSize + 3.dp.toPx()),
                        size = Size(cellSize - 6.dp.toPx(), cellSize - 6.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // Animated sweeping laser scanner beam
        val beamY = qrTop + qrSize * smoothWave
        drawLine(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, ElectricCyan, EmeraldSavings, Color.Transparent)),
            start = Offset(qrLeft - 10.dp.toPx(), beamY),
            end = Offset(qrLeft + qrSize + 10.dp.toPx(), beamY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Orbiting dashed sync halo around QR
        val haloR = qrSize * 0.78f
        drawCircle(
            color = EmeraldSavings.copy(alpha = 0.35f),
            radius = haloR,
            center = Offset(cx, cy),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), linearCycle * 100f)
            )
        )
    }
}
