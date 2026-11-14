package com.stackpad.app.ui.editor.blocks

import com.stackpad.app.ui.components.keepCaretVisible
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.text.UrlUtils
import com.stackpad.app.ui.theme.textStyle

/**
 * One line of text with its own heading level and inline bold/italic/underline.
 * URLs are highlighted and open when tapped. Enter starts a new text block, so every line is independent.
 */
@Composable
fun TextBlockView(
    block: TextBlock,
    placeholder: String?,
    requestFocus: Boolean,
    focusCaret: Int?,
    onFocusHandled: () -> Unit,
    onTextChange: (text: String, caret: Int) -> Unit,
    onSelectionChange: (start: Int, end: Int) -> Unit,
    onBackspaceAtStart: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var value by remember(block.id) { mutableStateOf(TextFieldValue(block.text)) }
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val currentOnOpenUrl by rememberUpdatedState(onOpenUrl)
    val currentText by rememberUpdatedState(block.text)

    LaunchedEffect(block.text) {
        if (value.text != block.text) value = TextFieldValue(block.text, TextRange(block.text.length))
    }
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            val caret = (focusCaret ?: block.text.length).coerceIn(0, block.text.length)
            value = TextFieldValue(block.text, TextRange(caret))
            runCatching { focusRequester.requestFocus() }
            onFocusHandled()
        }
    }

    val transformation = remember(block.spans, colors.primary) {
        RichTextTransformation(block.spans, colors.primary)
    }
    val textStyle = block.level.textStyle().copy(color = colors.onSurface)

    BasicTextField(
        value = value,
        onValueChange = { updated ->
            val textChanged = updated.text != value.text
            if ('\n' in updated.text) {
                // Keep only the first line here; the view model creates the other blocks.
                val first = updated.text.substringBefore('\n')
                value = TextFieldValue(first, TextRange(first.length))
            } else {
                value = updated
            }
            if (textChanged) onTextChange(updated.text, updated.selection.end)
            if (focused) onSelectionChange(updated.selection.min, updated.selection.max)
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .keepCaretVisible(value.selection, layout, focused)
            .pointerInput(Unit) {
                // Watches taps without consuming them, so the field still places its cursor.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val up = waitForUpOrCancellation(PointerEventPass.Initial) ?: return@awaitEachGesture
                    val quick = up.uptimeMillis - down.uptimeMillis < viewConfiguration.longPressTimeoutMillis
                    val still = (up.position - down.position).getDistance() < viewConfiguration.touchSlop
                    if (quick && still) {
                        layout?.let { result ->
                            tappedUrl(result, currentText, up.position)?.let(currentOnOpenUrl)
                        }
                    }
                }
            }
            .onFocusChanged { state ->
                focused = state.isFocused
                if (state.isFocused) onSelectionChange(value.selection.min, value.selection.max)
            }
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Backspace &&
                    event.type == KeyEventType.KeyDown &&
                    value.selection.collapsed &&
                    value.selection.start == 0
                ) {
                    onBackspaceAtStart()
                    true
                } else {
                    false
                }
            },
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        cursorBrush = SolidColor(colors.primary),
        visualTransformation = transformation,
        onTextLayout = { layout = it },
        decorationBox = { innerTextField ->
            Box {
                if (value.text.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = colors.onSurfaceVariant.copy(alpha = 0.6f)),
                    )
                }
                innerTextField()
            }
        },
    )
}

/** The URL under [position] in [text], or null when the tap landed elsewhere. */
private fun tappedUrl(layout: TextLayoutResult, text: String, position: Offset): String? {
    if (text.isEmpty()) return null
    val offset = layout.getOffsetForPosition(position)
    return UrlUtils.findUrls(text).firstOrNull { url ->
        offset in url.start until url.end ||
            (offset == url.end && layout.getBoundingBox(url.end - 1).contains(position))
    }?.url
}
