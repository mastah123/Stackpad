package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stackpad.app.R
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListItem
import com.stackpad.app.domain.text.ListMarkers
import com.stackpad.app.ui.components.SmallIconButton

/**
 * Bullet, numbered or lettered list. The marker lives in its own fixed-width column, so long
 * items wrap under the item text and never drop beneath the marker (hanging indent).
 */
@Composable
fun ListBlockView(
    block: ListBlock,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onBlockFocused: () -> Unit,
    onEdit: ((ListBlock) -> ListBlock) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val requesters = remember { HashMap<String, FocusRequester>() }
    var pendingFocusId by remember { mutableStateOf<String?>(null) }

    val longestMarker = block.items.indices.maxOfOrNull { ListMarkers.label(block.kind, it).length } ?: 1
    val markerWidth = with(LocalDensity.current) { (longestMarker * MARKER_CHAR_SP + MARKER_GAP_SP).sp.toDp() }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            block.items.forEachIndexed { index, item ->
                ListItemRow(
                    marker = ListMarkers.label(block.kind, index),
                    markerWidth = markerWidth,
                    item = item,
                    focusRequester = requesters.getOrPut(item.id) { FocusRequester() },
                    onFocused = onBlockFocused,
                    onTextChange = { text ->
                        onEdit { b -> b.copy(items = b.items.map { if (it.id == item.id) it.copy(text = text) else it }) }
                    },
                    onSplit = { before, after ->
                        val newItem = ListItem(BlockFactory.newId(), after)
                        pendingFocusId = newItem.id
                        onEdit { b ->
                            val at = b.items.indexOfFirst { it.id == item.id }
                            if (at < 0) {
                                b
                            } else {
                                val updated = b.items.toMutableList()
                                updated[at] = updated[at].copy(text = before)
                                updated.add(at + 1, newItem)
                                b.copy(items = updated)
                            }
                        }
                    },
                    onBackspaceWhenEmpty = {
                        if (block.items.size <= 1) {
                            onRemove()
                        } else {
                            pendingFocusId = block.items.getOrNull(index - 1)?.id
                            onEdit { b -> b.copy(items = b.items.filterNot { it.id == item.id }) }
                        }
                    },
                )
                DisposableEffect(item.id) {
                    onDispose { requesters.remove(item.id) }
                }
            }
        }
        SmallIconButton(
            icon = Icons.Filled.Close,
            contentDescription = stringResource(R.string.cd_remove_list),
            onClick = onRemove,
            size = 28.dp,
            iconSize = 16.dp,
        )
    }

    // Focus a freshly created block, or an item created by Enter / revealed by Backspace.
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            block.items.firstOrNull()?.let { runCatching { requesters[it.id]?.requestFocus() } }
            onFocusHandled()
        }
    }
    LaunchedEffect(pendingFocusId, block.items.size) {
        val id = pendingFocusId ?: return@LaunchedEffect
        val requester = requesters[id] ?: return@LaunchedEffect
        runCatching { requester.requestFocus() }
        pendingFocusId = null
    }
}

@Composable
private fun ListItemRow(
    marker: String,
    markerWidth: Dp,
    item: ListItem,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onTextChange: (String) -> Unit,
    onSplit: (before: String, after: String) -> Unit,
    onBackspaceWhenEmpty: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface)
    var value by remember(item.id) {
        mutableStateOf(TextFieldValue(item.text, TextRange(item.text.length)))
    }

    LaunchedEffect(item.text) {
        if (value.text != item.text) value = TextFieldValue(item.text, TextRange(item.text.length))
    }

    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = marker,
            style = textStyle.copy(color = colors.onSurfaceVariant, textAlign = TextAlign.End),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.width(markerWidth).padding(end = 6.dp),
        )
        BasicTextField(
            value = value,
            onValueChange = { updated ->
                val newline = updated.text.indexOf('\n')
                if (newline >= 0) {
                    val before = updated.text.substring(0, newline)
                    val after = updated.text.substring(newline + 1).replace("\n", "")
                    value = TextFieldValue(before, TextRange(before.length))
                    onSplit(before, after)
                } else {
                    val changed = updated.text != value.text
                    value = updated
                    if (changed) onTextChange(updated.text)
                }
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocused() }
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
        )
    }
}

private const val MARKER_CHAR_SP = 8
private const val MARKER_GAP_SP = 8
