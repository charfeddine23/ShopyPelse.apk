package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity

/**
 * Coordinates horizontal scroll regions (e.g. Frequent Items, Category Filter Strips)
 * with the root HorizontalPager so swiping inside those regions never switches pages.
 */
@Stable
class PagerSwipeBlockController {
    private val activeTokens = mutableStateMapOf<Any, Boolean>()

    val isBlocked: Boolean
        get() = activeTokens.isNotEmpty()

    fun setBlocked(token: Any, blocked: Boolean) {
        if (blocked) {
            activeTokens[token] = true
        } else {
            activeTokens.remove(token)
        }
    }
}

@Composable
fun rememberPagerSwipeBlockController(): PagerSwipeBlockController {
    return remember { PagerSwipeBlockController() }
}

val LocalPagerSwipeBlockController = compositionLocalOf<PagerSwipeBlockController?> { null }

/**
 * Disables ancestor HorizontalPager page swiping whenever pointer interaction occurs inside
 * this region, and consumes any leftover horizontal overscroll/fling deltas so page switching
 * cannot be triggered from horizontal strips like Frequent Items or Product Category filters.
 */
fun Modifier.disableParentPagerSwipe(): Modifier = composed {
    val controller = LocalPagerSwipeBlockController.current
    val token = remember { Any() }

    DisposableEffect(controller, token) {
        onDispose {
            controller?.setBlocked(token, false)
        }
    }

    val nestedHorizontalBlocker = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = Offset(available.x, 0f)

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity = Velocity(available.x, 0f)
        }
    }

    this
        .pointerInput(controller, token) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val anyPressed = event.changes.any { it.pressed }
                    controller?.setBlocked(token, anyPressed)
                }
            }
        }
        .nestedScroll(nestedHorizontalBlocker)
}
