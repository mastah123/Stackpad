package com.stackpad.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Borderless text field that keeps its own cursor state, so typing never jumps even though
 * the committed text round-trips through a view model.
 */
@Composable
fun InlineTextField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
    onFocusChange: (Boolean) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    var value by remember { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }

    LaunchedEffect(text) {
        if (value.text != text) value = TextFieldValue(text, TextRange(text.length))
    }

    val style = textStyle.let { if (it.color == androidx.compose.ui.graphics.Color.Unspecified) it.copy(color = colors.onSurface) else it }
    var focused by remember { mutableStateOf(false) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val fieldModifier = modifier
        .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
        .onFocusChanged { focused = it.isFocused; onFocusChange(it.isFocused) }
        .keepCaretVisible(value.selection, layout, focused)

    BasicTextField(
        value = value,
        onValueChange = { updated ->
            val changed = updated.text != value.text
            value = updated
            if (changed) onTextChange(updated.text)
        },
        modifier = fieldModifier,
        textStyle = style,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(colors.primary),
        onTextLayout = { layout = it },
        decorationBox = { innerTextField ->
            Box {
                if (value.text.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = style.copy(color = colors.onSurfaceVariant.copy(alpha = 0.6f)),
                    )
                }
                innerTextField()
            }
        },
    )
}
