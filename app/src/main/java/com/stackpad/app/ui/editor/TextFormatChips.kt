package com.stackpad.app.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.LineSpacing
import com.stackpad.app.data.model.TextFont
import com.stackpad.app.domain.text.FormatSpans
import com.stackpad.app.data.model.TextSize
import com.stackpad.app.ui.icons.AppIcons
import com.stackpad.app.ui.theme.TextHighlights

/** What the Format chips show as chosen. A null means the selection is mixed (nothing is ticked). */
data class TextLookState(
    val font: TextFont?,
    val size: TextSize,
    val highlight: Int?,
    val spacing: LineSpacing?,
)

/**
 * The four drop-down chips of the Format row: Font, Size, Highlight and Line height. Font and
 * Highlight apply to the selected letters, Line height to the touched lines, Size to the block.
 * They go inside the caller's Row.
 */
@Composable
fun TextFormatChips(
    look: TextLookState,
    onFont: (TextFont) -> Unit,
    onSize: (TextSize) -> Unit,
    onHighlight: (Int) -> Unit,
    onSpacing: (LineSpacing) -> Unit,
) {
    LookChip(
        label = stringResource(R.string.format_font),
        options = TextFont.values().toList(),
        selected = look.font,
        optionLabel = { stringResource(it.labelRes()) },
        onSelect = onFont,
    )
    LookChip(
        label = stringResource(R.string.format_size),
        options = TextSize.values().toList(),
        selected = look.size,
        optionLabel = { stringResource(it.labelRes()) },
        onSelect = onSize,
    )
    LookChip(
        label = stringResource(R.string.format_highlight),
        options = (0..FormatSpans.HIGHLIGHT_COUNT).toList(),
        selected = look.highlight,
        optionLabel = { stringResource(highlightLabel(it)) },
        swatch = { TextHighlights.swatch(it) },
        onSelect = onHighlight,
    )
    LookChip(
        label = stringResource(R.string.format_line_height),
        options = LineSpacing.values().toList(),
        selected = look.spacing,
        optionLabel = { stringResource(it.labelRes()) },
        onSelect = onSpacing,
    )
}

@Composable
private fun <T> LookChip(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    swatch: ((T) -> Color?)? = null,
) {
    val colors = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Box(Modifier.padding(horizontal = 3.dp)) {
        Surface(
            shape = shape,
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
            modifier = Modifier.height(32.dp).clip(shape).clickable(role = Role.Button) { open = true },
        ) {
            Row(modifier = Modifier.padding(start = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
                Spacer(Modifier.width(4.dp))
                Icon(AppIcons.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    leadingIcon = {
                        val color = swatch?.invoke(option)
                        when {
                            option == selected -> Icon(AppIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            color != null -> Box(Modifier.size(18.dp).clip(CircleShape).background(color))
                            else -> Spacer(Modifier.size(18.dp))
                        }
                    },
                    onClick = {
                        open = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

private fun TextFont.labelRes(): Int = when (this) {
    TextFont.DEFAULT -> R.string.font_default
    TextFont.SERIF -> R.string.font_serif
    TextFont.MONO -> R.string.font_mono
    TextFont.HANDWRITING -> R.string.font_handwriting
}

private fun TextSize.labelRes(): Int = when (this) {
    TextSize.SMALL -> R.string.size_small
    TextSize.NORMAL -> R.string.size_normal
    TextSize.LARGE -> R.string.size_large
    TextSize.HUGE -> R.string.size_huge
}

private fun LineSpacing.labelRes(): Int = when (this) {
    LineSpacing.TIGHT -> R.string.spacing_tight
    LineSpacing.NORMAL -> R.string.spacing_normal
    LineSpacing.RELAXED -> R.string.spacing_relaxed
    LineSpacing.LOOSE -> R.string.spacing_loose
}

private fun highlightLabel(index: Int): Int = when (index) {
    1 -> R.string.highlight_yellow
    2 -> R.string.highlight_green
    3 -> R.string.highlight_blue
    4 -> R.string.highlight_pink
    5 -> R.string.highlight_purple
    6 -> R.string.highlight_orange
    else -> R.string.highlight_none
}
