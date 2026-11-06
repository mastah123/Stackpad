package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.text.SpanEditing
import com.stackpad.app.ui.theme.textStyle

/** Free text with heading levels and inline bold/italic/underline; URLs are highlighted. */
@Composable
fun TextBlockView(
    block: TextBlock,
    placeholder: String?,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onEdit: ((TextBlock) -> TextBlock) -> Unit,
    onSelectionChange: (start: Int, end: Int) -> Unit,
    onBackspaceWhenEmpty: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var value by remember(block.id) { mutableStateOf(TextFieldValue(block.text)) }
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(block.text) {
        if (value.text != block.text) value = TextFieldValue(block.text, TextRange(block.text.length))
    }
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            value = value.copy(selection = TextRange(value.text.length))
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
            value = updated
            if (textChanged) {
                onEdit {
                    it.copy(
                        text = updated.text,
                        spans = SpanEditing.adjustForEdit(it.spans, it.text, updated.text),
                    )
                }
            }
            if (focused) onSelectionChange(updated.selection.min, updated.selection.max)
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                focused = state.isFocused
                if (state.isFocused) onSelectionChange(value.selection.min, value.selection.max)
            }
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Backspace &&
                    event.type == KeyEventType.KeyDown &&
                    value.text.isEmpty()
                ) {
                    onBackspaceWhenEmpty()
                    true
                } else {
                    false
                }
            },
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.primary),
        visualTransformation = transformation,
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
