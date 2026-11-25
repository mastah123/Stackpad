package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.domain.sheet.SheetOps
import com.stackpad.app.domain.sheet.SheetOps.withColumnCleared
import com.stackpad.app.domain.sheet.SheetOps.withColumnInserted
import com.stackpad.app.domain.sheet.SheetOps.withColumnMoved
import com.stackpad.app.domain.sheet.SheetOps.withColumnRemoved
import com.stackpad.app.domain.sheet.SheetOps.withRowCleared
import com.stackpad.app.domain.sheet.SheetOps.withRowInserted
import com.stackpad.app.domain.sheet.SheetOps.withRowMoved
import com.stackpad.app.domain.sheet.SheetOps.withRowRemoved

internal val HandleSize = 24.dp

/** Space kept free at the right and bottom of the grid so a handle on the last border is not cut off. */
internal val HandleOverhang = 14.dp

/** Which header the handle belongs to: a column (top row cell) or a row (left-most cell). */
internal sealed interface HandleSpec {
    val index: Int

    data class Column(override val index: Int) : HandleSpec
    data class Row(override val index: Int) : HandleSpec
}

internal enum class HandleAction { INSERT_BEFORE, INSERT_AFTER, MOVE_BACK, MOVE_FORWARD, CLEAR, DELETE }

internal fun HandleSpec.apply(block: SheetBlock, action: HandleAction): SheetBlock = when (this) {
    is HandleSpec.Column -> when (action) {
        HandleAction.INSERT_BEFORE -> block.withColumnInserted(index)
        HandleAction.INSERT_AFTER -> block.withColumnInserted(index + 1)
        HandleAction.MOVE_BACK -> block.withColumnMoved(index, index - 1)
        HandleAction.MOVE_FORWARD -> block.withColumnMoved(index, index + 1)
        HandleAction.CLEAR -> block.withColumnCleared(index)
        HandleAction.DELETE -> block.withColumnRemoved(index)
    }
    is HandleSpec.Row -> when (action) {
        HandleAction.INSERT_BEFORE -> block.withRowInserted(index)
        HandleAction.INSERT_AFTER -> block.withRowInserted(index + 1)
        HandleAction.MOVE_BACK -> block.withRowMoved(index, index - 1)
        HandleAction.MOVE_FORWARD -> block.withRowMoved(index, index + 1)
        HandleAction.CLEAR -> block.withRowCleared(index)
        HandleAction.DELETE -> block.withRowRemoved(index)
    }
}

internal fun HandleSpec.isEnabled(action: HandleAction, rows: Int, columns: Int): Boolean {
    val count = if (this is HandleSpec.Column) columns else rows
    val max = if (this is HandleSpec.Column) SheetOps.MAX_COLUMNS else SheetOps.MAX_ROWS
    return when (action) {
        HandleAction.INSERT_BEFORE, HandleAction.INSERT_AFTER -> count < max
        HandleAction.MOVE_BACK -> index > 0
        HandleAction.MOVE_FORWARD -> index < count - 1
        HandleAction.CLEAR -> true
        HandleAction.DELETE -> count > 1
    }
}

private fun HandleSpec.label(action: HandleAction): Int = if (this is HandleSpec.Column) {
    when (action) {
        HandleAction.INSERT_BEFORE -> R.string.table_insert_column_left
        HandleAction.INSERT_AFTER -> R.string.table_insert_column_right
        HandleAction.MOVE_BACK -> R.string.table_move_column_left
        HandleAction.MOVE_FORWARD -> R.string.table_move_column_right
        HandleAction.CLEAR -> R.string.table_clear_column
        HandleAction.DELETE -> R.string.table_delete_column
    }
} else {
    when (action) {
        HandleAction.INSERT_BEFORE -> R.string.table_insert_row_above
        HandleAction.INSERT_AFTER -> R.string.table_insert_row_below
        HandleAction.MOVE_BACK -> R.string.table_move_row_up
        HandleAction.MOVE_FORWARD -> R.string.table_move_row_down
        HandleAction.CLEAR -> R.string.table_clear_row
        HandleAction.DELETE -> R.string.table_delete_row
    }
}

/**
 * The small rounded handle on a cell border. Tap opens the row/column menu; dragging resizes
 * (horizontally for a column, vertically for a row). [onDragDp] reports the movement in dp.
 */
@Composable
internal fun TableHandle(
    spec: HandleSpec,
    rows: Int,
    columns: Int,
    menuExpanded: Boolean,
    onTap: () -> Unit,
    onDismissMenu: () -> Unit,
    onAction: (HandleAction) -> Unit,
    onDragStart: () -> Unit,
    onDragDp: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current.density
    val tap by rememberUpdatedState(onTap)
    val start by rememberUpdatedState(onDragStart)
    val drag by rememberUpdatedState(onDragDp)
    val end by rememberUpdatedState(onDragEnd)
    val shape = RoundedCornerShape(7.dp)
    val isColumn = spec is HandleSpec.Column
    val description = stringResource(if (isColumn) R.string.cd_table_column_handle else R.string.cd_table_row_handle)
    val gripColor = colors.inverseOnSurface

    Box(
        modifier = modifier
            .size(HandleSize)
            .background(colors.inverseSurface, shape)
            .border(1.dp, colors.outlineVariant, shape)
            .semantics {
                contentDescription = description
                role = Role.Button
            }
            .pointerInput(Unit) { detectTapGestures { tap() } }
            .pointerInput(isColumn) {
                if (isColumn) {
                    detectHorizontalDragGestures(
                        onDragStart = { start() },
                        onDragEnd = { end() },
                        onDragCancel = { end() },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            drag(amount / density)
                        },
                    )
                } else {
                    detectVerticalDragGestures(
                        onDragStart = { start() },
                        onDragEnd = { end() },
                        onDragCancel = { end() },
                        onVerticalDrag = { change, amount ->
                            change.consume()
                            drag(amount / density)
                        },
                    )
                }
            },
    ) {
        Canvas(Modifier.size(HandleSize)) {
            val radius = 1.6.dp.toPx()
            val gap = 5.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)
            for (i in -1..1) {
                val offset = if (isColumn) Offset(center.x + i * gap, center.y) else Offset(center.x, center.y + i * gap)
                drawCircle(gripColor, radius, offset)
            }
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismissMenu) {
            HandleAction.values().forEach { action ->
                DropdownMenuItem(
                    text = { Text(stringResource(spec.label(action))) },
                    enabled = spec.isEnabled(action, rows, columns),
                    onClick = { onAction(action) },
                )
            }
        }
    }
}
