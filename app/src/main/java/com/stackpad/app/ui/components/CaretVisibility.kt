package com.stackpad.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp

/**
 * Changes whenever the space the editor can use changes (for example when an option row opens
 * above the keyboard), so a focused text field scrolls its caret back into view.
 */
val LocalCaretRevealKey = compositionLocalOf { 0 }

/**
 * Keeps the caret of a focused text field inside the visible area of the scrolling list that
 * contains it: after typing, moving the caret, or when the area shrinks.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.keepCaretVisible(selection: TextRange, layout: TextLayoutResult?, focused: Boolean): Modifier {
    val requester = remember { BringIntoViewRequester() }
    val revealKey = LocalCaretRevealKey.current
    val margin = with(LocalDensity.current) { 12.dp.toPx() }

    LaunchedEffect(selection.end, layout, focused, revealKey) {
        val result = layout ?: return@LaunchedEffect
        if (!focused) return@LaunchedEffect
        withFrameNanos { } // let the new bar height and keyboard inset settle first
        val length = result.layoutInput.text.length
        val caret = result.getCursorRect(selection.end.coerceIn(0, length))
        requester.bringIntoView(Rect(caret.left, caret.top - margin, caret.right, caret.bottom + margin))
    }
    return this.bringIntoViewRequester(requester)
}
