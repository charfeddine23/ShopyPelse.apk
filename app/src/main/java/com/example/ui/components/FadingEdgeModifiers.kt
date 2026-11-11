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
 * Applies a smooth horizontal fading edge effect (left and right edges fade out into transparency)
 * during horizontal scrolling using an offscreen graphicsLayer and a horizontal gradient mask with BlendMode.DstIn.
 *
 * @param fadeWidth The width of the fade gradient on both left and right edges.
 */
fun Modifier.horizontalFadingEdge(
    fadeWidth: Dp = 20.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fadeWidthPx = fadeWidth.toPx()
        val width = size.width
        if (width > fadeWidthPx * 2f && fadeWidthPx > 0f) {
            val leftStop = (fadeWidthPx / width).coerceIn(0f, 0.49f)
            val rightStop = ((width - fadeWidthPx) / width).coerceIn(0.51f, 1f)

            drawRect(
                brush = Brush.horizontalGradient(
                    0.0f to Color.Transparent,
                    leftStop to Color.Black,
                    rightStop to Color.Black,
                    1.0f to Color.Transparent
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }
