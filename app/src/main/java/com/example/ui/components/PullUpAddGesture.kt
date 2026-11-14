package com.example.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Immutable
data class PullUpGestureState(
    val pullOffsetPx: Float = 0f,
    val progress: Float = 0f,
    val isDragging: Boolean = false,
    val isThresholdReached: Boolean = false
)

internal object SingleFingerPullUpDetector {
    // Tight vertical cone (~14 degrees max deviation from straight up: |dy| >= 4 * |dx|)
    private const val STRICT_UPWARD_SLOPE_RATIO = 4.0f

    fun isDominantUpwardDrag(dx: Float, dy: Float, touchSlopPx: Float): Boolean {
        return dy < -touchSlopPx && abs(dy) >= abs(dx) * STRICT_UPWARD_SLOPE_RATIO
    }

    fun isAbandonedForOtherGesture(dx: Float, dy: Float, touchSlopPx: Float): Boolean {
        val downwardDrag = dy > touchSlopPx * 0.5f
        val offAxisHorizontal = abs(dx) > touchSlopPx * 0.6f && abs(dy) < abs(dx) * STRICT_UPWARD_SLOPE_RATIO
        val outsideConeAfterTravel = abs(dy) > touchSlopPx && abs(dy) < abs(dx) * STRICT_UPWARD_SLOPE_RATIO
        return downwardDrag || offAxisHorizontal || outsideConeAfterTravel
    }

    fun computeVisualPullOffsetPx(dy: Float, maxVisualPullPx: Float): Float {
        return ((-dy).coerceAtLeast(0f) * 0.42f).coerceIn(0f, maxVisualPullPx)
    }

    fun isTriggerThresholdReached(dx: Float, dy: Float, thresholdPx: Float): Boolean {
        return -dy >= thresholdPx && abs(dy) >= abs(dx) * STRICT_UPWARD_SLOPE_RATIO
    }
}

/**
 * Detects a strict straight-up single-finger pull gesture on the empty shopping list card
 * without interfering with diagonal/horizontal tab paging, downward list scrolling,
 * button taps, or the parent two-finger full-screen gesture.
 */
fun Modifier.singleFingerPullUpToAdd(
    enabled: Boolean = true,
    onStateChanged: (PullUpGestureState) -> Unit,
    onThresholdReachedHaptic: () -> Unit,
    onTriggered: () -> Unit
): Modifier = composed {
    val latestEnabled by rememberUpdatedState(enabled)
    val latestOnStateChanged by rememberUpdatedState(onStateChanged)
    val latestOnThresholdReachedHaptic by rememberUpdatedState(onThresholdReachedHaptic)
    val latestOnTriggered by rememberUpdatedState(onTriggered)
    val touchSlopPx = LocalViewConfiguration.current.touchSlop

    pointerInput(touchSlopPx) {
        val triggerThresholdPx = 56.dp.toPx()
        val maxVisualPullPx = 36.dp.toPx()

        awaitEachGesture {
            var trackedId: PointerId? = null
            var startX = 0f
            var startY = 0f
            var isDraggingUp = false
            var abandoned = false
            var thresholdReached = false
            var hapticFired = false

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pressed = event.changes.filter { it.pressed }

                if (pressed.isEmpty()) {
                    val shouldTrigger = latestEnabled && isDraggingUp && thresholdReached && !abandoned
                    if (isDraggingUp || thresholdReached) {
                        latestOnStateChanged(PullUpGestureState())
                    }
                    if (shouldTrigger) {
                        latestOnTriggered()
                    }
                    break
                }

                if (!latestEnabled || pressed.size != 1) {
                    if (isDraggingUp || thresholdReached) {
                        latestOnStateChanged(PullUpGestureState())
                    }
                    isDraggingUp = false
                    thresholdReached = false
                    hapticFired = false
                    abandoned = true
                    continue
                }

                val pointer = pressed[0]
                if (trackedId == null) {
                    trackedId = pointer.id
                    startX = pointer.position.x
                    startY = pointer.position.y
                    continue
                }

                if (pointer.id != trackedId || abandoned) {
                    if (pointer.id != trackedId) {
                        if (isDraggingUp || thresholdReached) {
                            latestOnStateChanged(PullUpGestureState())
                        }
                        isDraggingUp = false
                        thresholdReached = false
                        abandoned = true
                    }
                    continue
                }

                val dx = pointer.position.x - startX
                val dy = pointer.position.y - startY

                if (!isDraggingUp) {
                    if (SingleFingerPullUpDetector.isAbandonedForOtherGesture(dx, dy, touchSlopPx)) {
                        abandoned = true
                        continue
                    }
                    if (SingleFingerPullUpDetector.isDominantUpwardDrag(dx, dy, touchSlopPx)) {
                        isDraggingUp = true
                    }
                }

                if (isDraggingUp) {
                    if (abs(dy) < abs(dx) * 3.0f || dy >= 0f) {
                        latestOnStateChanged(PullUpGestureState())
                        isDraggingUp = false
                        thresholdReached = false
                        hapticFired = false
                        abandoned = true
                        continue
                    }

                    event.changes.forEach { it.consume() }
                    val visualOffset = SingleFingerPullUpDetector.computeVisualPullOffsetPx(dy, maxVisualPullPx)
                    val nowReached = SingleFingerPullUpDetector.isTriggerThresholdReached(dx, dy, triggerThresholdPx)
                    thresholdReached = nowReached

                    if (nowReached && !hapticFired) {
                        hapticFired = true
                        latestOnThresholdReachedHaptic()
                    } else if (!nowReached) {
                        hapticFired = false
                    }

                    val progress = ((-dy) / triggerThresholdPx).coerceIn(0f, 1f)
                    latestOnStateChanged(
                        PullUpGestureState(
                            pullOffsetPx = visualOffset,
                            progress = progress,
                            isDragging = true,
                            isThresholdReached = nowReached
                        )
                    )
                }
            }
        }
    }
}
