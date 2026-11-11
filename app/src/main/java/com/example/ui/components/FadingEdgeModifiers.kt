package com.example.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a smooth, realistic horizontal fading edge effect (left and right edges fade out into transparency)
 * during horizontal scrolling using an offscreen graphicsLayer and an atmospheric multi-stop fog gradient mask
 * with BlendMode.DstIn.
 *
 * @param fadeWidth The width of the fog fade gradient on both left and right edges.
 */
fun Modifier.horizontalFadingEdge(
    fadeWidth: Dp = 26.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fadeWidthPx = fadeWidth.toPx()
        val width = size.width
        if (width > fadeWidthPx * 2f && fadeWidthPx > 0f) {
            val leftStop = (fadeWidthPx / width).coerceIn(0f, 0.49f)
            val rightStop = ((width - fadeWidthPx) / width).coerceIn(0.51f, 1f)

            // Multi-stop realistic atmospheric mist / fog curve (cubic ease)
            drawRect(
                brush = Brush.horizontalGradient(
                    0.00f to Color.Transparent,
                    leftStop * 0.20f to Color.Black.copy(alpha = 0.05f),
                    leftStop * 0.40f to Color.Black.copy(alpha = 0.22f),
                    leftStop * 0.65f to Color.Black.copy(alpha = 0.58f),
                    leftStop * 0.85f to Color.Black.copy(alpha = 0.88f),
                    leftStop to Color.Black,
                    rightStop to Color.Black,
                    rightStop + (1f - rightStop) * 0.15f to Color.Black.copy(alpha = 0.88f),
                    rightStop + (1f - rightStop) * 0.35f to Color.Black.copy(alpha = 0.58f),
                    rightStop + (1f - rightStop) * 0.60f to Color.Black.copy(alpha = 0.22f),
                    rightStop + (1f - rightStop) * 0.80f to Color.Black.copy(alpha = 0.05f),
                    1.00f to Color.Transparent
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }
