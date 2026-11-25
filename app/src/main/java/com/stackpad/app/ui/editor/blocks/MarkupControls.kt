package com.stackpad.app.ui.editor.blocks

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.domain.image.MarkStyle

/** Tools, colours and sizes for the Mark up tab, plus undo and clear. */
@Composable
fun MarkupControls(
    tool: MarkupTool,
    onTool: (MarkupTool) -> Unit,
    color: Int,
    onColor: (Int) -> Unit,
    size: MarkStyle.Size,
    onSize: (MarkStyle.Size) -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
    hasMarks: Boolean,
    onClear: () -> Unit,
    enabled: Boolean,
) {
    val usesColor = tool == MarkupTool.DRAW || tool == MarkupTool.HIGHLIGHT || tool == MarkupTool.TEXT
    val usesSize = usesColor
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MarkupTool.entries.forEach { entry ->
                    FilterChip(
                        selected = tool == entry,
                        onClick = { onTool(entry) },
                        enabled = enabled,
                        label = { Text(stringResource(entry.label())) },
                    )
                }
            }
            if (usesColor) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MarkStyle.colors.forEachIndexed { index, value ->
                        val name = stringResource(colorNames[index])
                        Surface(
                            color = Color(value),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(30.dp)
                                .border(if (value == color) 3.dp else 1.dp, if (value == color) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
                                .clip(CircleShape)
                                .clickable(enabled = enabled, role = Role.RadioButton) { onColor(value) }
                                .semantics { contentDescription = name },
                        ) {}
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (usesSize) {
                    MarkStyle.Size.entries.forEach { entry ->
                        FilterChip(
                            selected = size == entry,
                            onClick = { onSize(entry) },
                            enabled = enabled,
                            label = { Text(stringResource(entry.label())) },
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onUndo, enabled = enabled && canUndo) {
                        Icon(AppIcons.Undo, contentDescription = stringResource(R.string.action_undo))
                    }
                    IconButton(onClick = onClear, enabled = enabled && hasMarks) {
                        Icon(AppIcons.DeleteSweep, contentDescription = stringResource(R.string.image_mark_clear))
                    }
                }
            }
        }
    }
}

private val colorNames = listOf(
    R.string.color_red, R.string.color_orange, R.string.color_yellow, R.string.color_green,
    R.string.color_blue, R.string.color_white, R.string.color_black,
)

private fun MarkupTool.label(): Int = when (this) {
    MarkupTool.DRAW -> R.string.image_mark_draw
    MarkupTool.HIGHLIGHT -> R.string.image_mark_highlight
    MarkupTool.BLUR -> R.string.image_mark_blur
    MarkupTool.PIXELATE -> R.string.image_mark_pixelate
    MarkupTool.TEXT -> R.string.image_mark_text
}

private fun MarkStyle.Size.label(): Int = when (this) {
    MarkStyle.Size.SMALL -> R.string.image_mark_size_small
    MarkStyle.Size.MEDIUM -> R.string.image_mark_size_medium
    MarkStyle.Size.LARGE -> R.string.image_mark_size_large
}
