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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.em
import com.stackpad.app.data.model.LineSpacing
import com.stackpad.app.domain.text.FormatSpans
import com.stackpad.app.ui.theme.TextHighlights
import com.stackpad.app.ui.theme.family
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextLevel
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextFont
import com.stackpad.app.domain.text.UrlUtils
import com.stackpad.app.ui.theme.textStyle

/**
 * A run of text lines in one field: Enter adds a line, any line can be a heading, and text can have
 * inline bold/italic/underline. URLs are highlighted and open when tapped.
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

    val typography = MaterialTheme.typography
    val highlights = (1..FormatSpans.HIGHLIGHT_COUNT).mapNotNull { index ->
        val color = TextHighlights.background(index) ?: return@mapNotNull null
        FormatSpans.highlightType(index)!! to SpanStyle(background = color)
    }
    val spanStyles = remember(typography, highlights) {
        mapOf(
            SpanType.H1 to TextLevel.H1.textStyle(typography).toSpanStyle(),
            SpanType.H2 to TextLevel.H2.textStyle(typography).toSpanStyle(),
            SpanType.H3 to TextLevel.H3.textStyle(typography).toSpanStyle(),
        ) + TextFont.values().mapNotNull { font ->
            val type = FormatSpans.fontType(font) ?: return@mapNotNull null
            font.family()?.let { type to SpanStyle(fontFamily = it) }
        } + highlights
    }
    val lineHeights = remember {
        LineSpacing.values().mapNotNull { spacing ->
            val type = FormatSpans.lineType(spacing) ?: return@mapNotNull null
            spacing.multiplier?.let { type to it.em }
        }.toMap()
    }
    val transformation = remember(block.spans, colors.primary, spanStyles) {
        RichTextTransformation(block.spans, colors.primary, spanStyles, lineHeights)
    }
    // No fixed line height: a line with a heading is taller than a body line.
    val baseStyle = TextLevel.PARAGRAPH.textStyle(typography)
    val fontSize = baseStyle.fontSize * block.size.scale
    val textStyle = baseStyle.copy(
        color = colors.onSurface,
        fontSize = fontSize,
    )

    BasicTextField(
        value = value,
        onValueChange = { updated ->
            val textChanged = updated.text != value.text
            value = updated
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
