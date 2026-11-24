package com.stackpad.app.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch

/** Share of the screen width the side menu takes. */
private const val MENU_WIDTH_FRACTION = 0.8f
private const val SLIDE_MILLIS = 260
private const val FLING_SPEED = 700f

/** Open/closed state of [PushMenuLayout]. [fraction] is 0 when closed and 1 when fully open. */
@Stable
class PushMenuState {
    internal val fraction = Animatable(0f)

    /** True while the menu is open or opening. */
    val isOpen: Boolean get() = fraction.targetValue > 0f

    suspend fun open() {
        fraction.animateTo(1f, tween(SLIDE_MILLIS))
    }

    suspend fun close() {
        fraction.animateTo(0f, tween(SLIDE_MILLIS))
    }

    /** Finishes a drag: a quick fling decides the direction, otherwise whichever side is nearer. */
    internal suspend fun settle(velocity: Float) {
        when {
            velocity > FLING_SPEED -> open()
            velocity < -FLING_SPEED -> close()
            fraction.value > 0.5f -> open()
            else -> close()
        }
    }
}

@Composable
fun rememberPushMenuState(): PushMenuState = remember { PushMenuState() }

/**
 * Side menu that slides in from the left and pushes [content] to the right instead of covering it.
 * The menu is 80% of the screen wide. Tapping the visible part of the content, pressing back, or
 * swiping left closes it; swiping right opens it while [gesturesEnabled].
 */
@Composable
fun PushMenuLayout(
    state: PushMenuState,
    gesturesEnabled: Boolean,
    menu: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val menuWidth = maxWidth * MENU_WIDTH_FRACTION
        val menuPx = with(LocalDensity.current) { menuWidth.toPx() }
        val dragState = rememberDraggableState { delta ->
            scope.launch { state.fraction.snapTo((state.fraction.value + delta / menuPx).coerceIn(0f, 1f)) }
        }
        BackHandler(enabled = state.isOpen) { scope.launch { state.close() } }

        Box(
            modifier = Modifier.fillMaxSize().draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                enabled = gesturesEnabled || state.isOpen,
                onDragStopped = { velocity -> state.settle(velocity) },
            ),
        ) {
            Box(
                modifier = Modifier
                    .width(menuWidth)
                    .fillMaxHeight()
                    .graphicsLayer { translationX = (state.fraction.value - 1f) * menuPx },
            ) { menu() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = state.fraction.value * menuPx },
            ) {
                content()
                if (state.fraction.value > 0f) {
                    // Catches taps on the notes that stay visible, so they close the menu instead of opening a note.
                    Box(
                        Modifier.fillMaxSize().pointerInput(Unit) {
                            detectTapGestures { scope.launch { state.close() } }
                        },
                    )
                }
            }
        }
    }
}
