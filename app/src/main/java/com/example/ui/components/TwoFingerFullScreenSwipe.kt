package com.example.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

enum class TwoFingerSwipeDirection {
    UP,
    DOWN
}

internal object TwoFingerSwipeDetector {
    fun evaluateVerticalSwipe(
        dy1: Float,
        dy2: Float,
        dx1: Float,
        dx2: Float,
        swipeThresholdPx: Float,
        minPerFingerDyPx: Float
    ): TwoFingerSwipeDirection? {
        val avgDy = (dy1 + dy2) / 2f
        val avgDx = (dx1 + dx2) / 2f
        val sameVerticalDirection = (dy1 < 0f && dy2 < 0f) || (dy1 > 0f && dy2 > 0f)
        val bothMovedEnough = abs(dy1) >= minPerFingerDyPx && abs(dy2) >= minPerFingerDyPx
        val verticalDominant = abs(avgDy) > abs(avgDx) * 1.15f

        if (!sameVerticalDirection || !bothMovedEnough || !verticalDominant) {
            return null
        }

        return when {
            avgDy <= -swipeThresholdPx -> TwoFingerSwipeDirection.UP
            avgDy >= swipeThresholdPx -> TwoFingerSwipeDirection.DOWN
            else -> null
        }
    }
}

/**
 * Detects a vertical two-finger swipe across the screen using [PointerEventPass.Initial]
 * without interfering with standard single-finger scrolling, tapping, or horizontal paging.
 *
 * - Swipe up with two fingers -> [onSwipeUp] (enable Full Screen mode)
 * - Swipe down with two fingers -> [onSwipeDown] (disable Full Screen mode)
 */
fun Modifier.twoFingerFullScreenSwipe(
    enabled: Boolean,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit
): Modifier = composed {
    val latestEnabled by rememberUpdatedState(enabled)
    val latestOnSwipeUp by rememberUpdatedState(onSwipeUp)
    val latestOnSwipeDown by rememberUpdatedState(onSwipeDown)

    pointerInput(Unit) {
        val swipeThresholdPx = 56.dp.toPx()
        val minPerFingerDyPx = 24.dp.toPx()
        val consumeSlopPx = 12.dp.toPx()

        awaitEachGesture {
            var trackedId1: PointerId? = null
            var trackedId2: PointerId? = null
            var startX1 = 0f
            var startY1 = 0f
            var startX2 = 0f
            var startY2 = 0f
            var triggered = false

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pressed = event.changes.filter { it.pressed }

                if (pressed.isEmpty()) {
                    break
                }

                if (!latestEnabled) {
                    trackedId1 = null
                    trackedId2 = null
                    continue
                }

                if (pressed.size == 2 && !triggered) {
                    val p1 = pressed[0]
                    val p2 = pressed[1]

                    if (trackedId1 != p1.id || trackedId2 != p2.id) {
                        trackedId1 = p1.id
                        trackedId2 = p2.id
                        startX1 = p1.position.x
                        startY1 = p1.position.y
                        startX2 = p2.position.x
                        startY2 = p2.position.y
                    } else {
                        val dy1 = p1.position.y - startY1
                        val dy2 = p2.position.y - startY2
                        val dx1 = p1.position.x - startX1
                        val dx2 = p2.position.x - startX2

                        val avgDy = (dy1 + dy2) / 2f
                        val avgDx = (dx1 + dx2) / 2f
                        val sameSign = (dy1 < 0f && dy2 < 0f) || (dy1 > 0f && dy2 > 0f)
                        if (sameSign && abs(avgDy) > consumeSlopPx && abs(avgDy) > abs(avgDx)) {
                            event.changes.forEach { it.consume() }
                        }

                        val direction = TwoFingerSwipeDetector.evaluateVerticalSwipe(
                            dy1 = dy1,
                            dy2 = dy2,
                            dx1 = dx1,
                            dx2 = dx2,
                            swipeThresholdPx = swipeThresholdPx,
                            minPerFingerDyPx = minPerFingerDyPx
                        )

                        if (direction != null) {
                            triggered = true
                            event.changes.forEach { it.consume() }
                            when (direction) {
                                TwoFingerSwipeDirection.UP -> latestOnSwipeUp()
                                TwoFingerSwipeDirection.DOWN -> latestOnSwipeDown()
                            }
                        }
                    }
                } else {
                    if (triggered) {
                        event.changes.forEach { it.consume() }
                    }
                    if (pressed.size != 2) {
                        trackedId1 = null
                        trackedId2 = null
                    }
                }
            }
        }
    }
}
