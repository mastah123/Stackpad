package com.stackpad.app.ui.editor.blocks

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import kotlinx.coroutines.launch

/**
 * When the row's position inside its parent changes (the list was re-sorted), the row slides
 * from its old position to the new one instead of jumping. Does nothing while [enabled] is false.
 */
fun Modifier.slideOnReorder(enabled: Boolean): Modifier = composed {
    val offset = remember { Animatable(0f) }
    var lastTop by remember { mutableStateOf<Float?>(null) }
    val scope = rememberCoroutineScope()
    this
        .onGloballyPositioned { coordinates ->
            val top = coordinates.positionInParent().y
            val previous = lastTop
            lastTop = top
            if (enabled && previous != null && previous != top) {
                scope.launch {
                    offset.snapTo(offset.value + previous - top)
                    offset.animateTo(0f, tween(durationMillis = 220))
                }
            }
        }
        .graphicsLayer { translationY = offset.value }
}
