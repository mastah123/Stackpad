package com.stackpad.app.ui.editor.blocks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.stackpad.app.R
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.domain.checklist.ChecklistOps
import com.stackpad.app.ui.components.BlockSurface
import com.stackpad.app.ui.components.CompactCheckbox
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.ui.components.SmallIconButton

/**
 * Checklist card: rounded corners, a progress tracker, drag handles to reorder items,
 * and an "x" that removes the whole checklist.
 */
@Composable
fun ChecklistBlockView(
    block: ChecklistBlock,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onBlockFocused: () -> Unit,
    onEdit: ((ChecklistBlock) -> ChecklistBlock) -> Unit,
    onDueDateSet: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var dueEditId by remember { mutableStateOf<String?>(null) }
    val done = block.items.count { it.checked }
    val total = block.items.size
    val progress by animateFloatAsState(
        targetValue = if (total == 0) 0f else done / total.toFloat(),
        label = "checklistProgress",
    )

    val requesters = remember { HashMap<String, FocusRequester>() }
    var pendingFocusId by remember { mutableStateOf<String?>(null) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val heights = remember { mutableStateMapOf<String, Int>() }
    val latestBlock by rememberUpdatedState(block)
    val latestEdit by rememberUpdatedState(onEdit)

    /** Swaps an item with its neighbour; resolved against the newest block so it is never stale. */
    fun move(itemId: String, direction: Int) {
        latestEdit { b ->
            val from = b.items.indexOfFirst { it.id == itemId }
            val to = from + direction
            if (from < 0 || to !in b.items.indices) {
                b
            } else {
                val reordered = b.items.toMutableList()
                reordered.add(to, reordered.removeAt(from))
                b.copy(items = reordered)
            }
        }
    }

    BlockSurface(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InlineTextField(
                text = block.title,
                onTextChange = { text -> onEdit { it.copy(title = text) } },
                placeholder = stringResource(R.string.checklist_title_hint),
                textStyle = MaterialTheme.typography.titleMedium,
                singleLine = true,
                onFocusChange = { if (it) onBlockFocused() },
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.checklist_progress, done, total),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
            SmallIconButton(
                icon = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.cd_sort_checklist),
                onClick = { onEdit { it.copy(items = ChecklistOps.uncheckedFirst(it.items)) } },
                enabled = ChecklistOps.canSort(block.items),
                size = 28.dp,
                iconSize = 16.dp,
            )
            SmallIconButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_remove_checklist),
                onClick = onRemove,
                size = 28.dp,
                iconSize = 16.dp,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colors.primary,
            trackColor = colors.outlineVariant,
        )
        Spacer(Modifier.height(4.dp))

        block.items.forEachIndexed { index, item ->
            key(item.id) {
                val isDragging = draggingId == item.id
                ChecklistRow(
                    item = item,
                    isDragging = isDragging,
                    dragOffsetProvider = { dragOffset },
                    focusRequester = requesters.getOrPut(item.id) { FocusRequester() },
                    onHeight = { heights[item.id] = it },
                    onEditDueDate = { dueEditId = item.id },
                    onFocused = onBlockFocused,
                    onCheckedChange = { checked ->
                        onEdit { b -> b.copy(items = b.items.map { if (it.id == item.id) it.copy(checked = checked) else it }) }
                    },
                    onTextChange = { text ->
                        onEdit { b -> b.copy(items = b.items.map { if (it.id == item.id) it.copy(text = text) else it }) }
                    },
                    onSplit = { before, after ->
                        val newItem = ChecklistItem(BlockFactory.newId(), after)
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
                        if (block.items.size > 1) {
                            pendingFocusId = block.items.getOrNull(index - 1)?.id
                            onEdit { b -> b.copy(items = b.items.filterNot { it.id == item.id }) }
                        }
                    },
                    onDragStart = {
                        draggingId = item.id
                        dragOffset = 0f
                    },
                    onDrag = { delta ->
                        dragOffset += delta
                        val items = latestBlock.items
                        val at = items.indexOfFirst { it.id == item.id }
                        if (at >= 0 && dragOffset > 0 && at < items.lastIndex) {
                            val neighbour = heights[items[at + 1].id] ?: 0
                            if (neighbour > 0 && dragOffset > neighbour / 2f) {
                                move(item.id, +1)
                                dragOffset -= neighbour
                            }
                        } else if (at > 0 && dragOffset < 0) {
                            val neighbour = heights[items[at - 1].id] ?: 0
                            if (neighbour > 0 && -dragOffset > neighbour / 2f) {
                                move(item.id, -1)
                                dragOffset += neighbour
                            }
                        }
                    },
                    onDragEnd = {
                        draggingId = null
                        dragOffset = 0f
                    },
                    onMoveUp = { move(item.id, -1) },
                    onMoveDown = { move(item.id, +1) },
                )
                DisposableEffect(item.id) {
                    onDispose {
                        requesters.remove(item.id)
                        heights.remove(item.id)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    val newItem = ChecklistItem(BlockFactory.newId())
                    pendingFocusId = newItem.id
                    onEdit { it.copy(items = it.items + newItem) }
                }
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = colors.primary,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.checklist_add_item),
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
            )
        }
    }

    dueEditId?.let { id ->
        val item = block.items.firstOrNull { it.id == id }
        if (item == null) {
            dueEditId = null
        } else {
            fun setDueDate(date: String?) {
                onEdit { b -> b.copy(items = b.items.map { if (it.id == id) it.copy(dueDate = date) else it }) }
                dueEditId = null
            }
            DueDateDialog(
                initial = item.dueDate,
                onConfirm = { date ->
                    setDueDate(date)
                    onDueDateSet()
                },
                onClear = { setDueDate(null) },
                onDismiss = { dueEditId = null },
            )
        }
    }

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
private fun ChecklistRow(
    item: ChecklistItem,
    isDragging: Boolean,
    dragOffsetProvider: () -> Float,
    focusRequester: FocusRequester,
    onHeight: (Int) -> Unit,
    onEditDueDate: () -> Unit,
    onFocused: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onSplit: (before: String, after: String) -> Unit,
    onBackspaceWhenEmpty: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val moveUpLabel = stringResource(R.string.cd_move_item_up)
    val moveDownLabel = stringResource(R.string.cd_move_item_down)
    var value by remember(item.id) {
        mutableStateOf(TextFieldValue(item.text, TextRange(item.text.length)))
    }
    LaunchedEffect(item.text) {
        if (value.text != item.text) value = TextFieldValue(item.text, TextRange(item.text.length))
    }

    // The drag gesture is installed once, so always call the newest callbacks.
    val currentDragStart by rememberUpdatedState(onDragStart)
    val currentDrag by rememberUpdatedState(onDrag)
    val currentDragEnd by rememberUpdatedState(onDragEnd)

    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = if (item.checked) colors.onSurfaceVariant else colors.onSurface,
        textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { onHeight(it.height) }
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer { translationY = if (isDragging) dragOffsetProvider() else 0f }
            .background(
                color = if (isDragging) colors.surface else colors.surface.copy(alpha = 0f),
                shape = RoundedCornerShape(8.dp),
            )
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(moveUpLabel) {
                        onMoveUp()
                        true
                    },
                    CustomAccessibilityAction(moveDownLabel) {
                        onMoveDown()
                        true
                    },
                )
            },
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 28.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { currentDragStart() },
                        onDragEnd = { currentDragEnd() },
                        onDragCancel = { currentDragEnd() },
                    ) { change, amount ->
                        change.consume()
                        currentDrag(amount.y)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = stringResource(R.string.cd_drag_handle),
                modifier = Modifier.size(18.dp),
                tint = colors.onSurfaceVariant,
            )
        }
        CompactCheckbox(checked = item.checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(4.dp))
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
                .padding(top = 5.dp)
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
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            cursorBrush = SolidColor(colors.primary),
        )
        DueDateButton(dueDate = item.dueDate, checked = item.checked, onClick = onEditDueDate)
    }
}
