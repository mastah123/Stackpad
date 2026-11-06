package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.domain.formula.FormulaErrors
import com.stackpad.app.domain.formula.SheetEvaluator
import com.stackpad.app.domain.formula.columnLabel
import com.stackpad.app.domain.sheet.SheetOps
import com.stackpad.app.domain.sheet.SheetOps.columnCount
import com.stackpad.app.domain.sheet.SheetOps.rowCount
import com.stackpad.app.domain.sheet.SheetOps.withCell
import com.stackpad.app.domain.sheet.SheetOps.withColumnAdded
import com.stackpad.app.domain.sheet.SheetOps.withLastColumnRemoved
import com.stackpad.app.domain.sheet.SheetOps.withLastRowRemoved
import com.stackpad.app.domain.sheet.SheetOps.withRowAdded
import com.stackpad.app.ui.components.BlockSurface
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.ui.components.SmallIconButton

private val CellWidth = 76.dp
private val CellHeight = 28.dp
private val RowHeaderWidth = 26.dp

/**
 * Simple spreadsheet. Tap a cell to edit it; text starting with `=` is evaluated
 * (`=A1+B1*2`, parentheses, `+ - * /`).
 */
@Composable
fun SpreadsheetBlockView(
    block: SheetBlock,
    onBlockFocused: () -> Unit,
    onEdit: ((SheetBlock) -> SheetBlock) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val evaluator = remember(block.cells) { SheetEvaluator(block.cells) }
    var editing by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val rows = block.rowCount
    val columns = block.columnCount

    BlockSurface(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.block_table),
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
                modifier = Modifier.weight(1f),
            )
            SheetAction(stringResource(R.string.table_add_row), rows < SheetOps.MAX_ROWS) {
                onEdit { it.withRowAdded() }
            }
            SheetAction(stringResource(R.string.table_remove_row), rows > 1) {
                editing = null
                onEdit { it.withLastRowRemoved() }
            }
            SheetAction(stringResource(R.string.table_add_column), columns < SheetOps.MAX_COLUMNS) {
                onEdit { it.withColumnAdded() }
            }
            SheetAction(stringResource(R.string.table_remove_column), columns > 1) {
                editing = null
                onEdit { it.withLastColumnRemoved() }
            }
            SmallIconButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_remove_table),
                onClick = onRemove,
                size = 28.dp,
                iconSize = 16.dp,
            )
        }

        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Row {
                Spacer(Modifier.size(width = RowHeaderWidth, height = CellHeight))
                repeat(columns) { c ->
                    HeaderCell(label = columnLabel(c), width = CellWidth)
                }
            }
            repeat(rows) { r ->
                Row {
                    HeaderCell(label = (r + 1).toString(), width = RowHeaderWidth)
                    repeat(columns) { c ->
                        SheetCell(
                            raw = block.cells[r][c],
                            display = evaluator.display(r, c),
                            isEditing = editing == (r to c),
                            onStartEdit = {
                                editing = r to c
                                onBlockFocused()
                            },
                            onTextChange = { text -> onEdit { it.withCell(r, c, text) } },
                            onEndEdit = { if (editing == (r to c)) editing = null },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetAction(label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (enabled) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
    )
}

@Composable
private fun HeaderCell(label: String, width: Dp) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(width = width, height = CellHeight)
            .border(0.5.dp, colors.outlineVariant)
            .background(colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SheetCell(
    raw: String,
    display: String,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onTextChange: (String) -> Unit,
    onEndEdit: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(width = CellWidth, height = CellHeight)
            .border(0.5.dp, colors.outlineVariant)
            .background(colors.surface),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (isEditing) {
            val focusRequester = remember { FocusRequester() }
            var gainedFocus by remember { mutableStateOf(false) }
            InlineTextField(
                text = raw,
                onTextChange = onTextChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onEndEdit() }),
                focusRequester = focusRequester,
                onFocusChange = { focused ->
                    if (focused) gainedFocus = true else if (gainedFocus) onEndEdit()
                },
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
        } else {
            val isError = display.startsWith("#") && display in ERROR_CODES
            Text(
                text = display,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) colors.error else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onStartEdit)
                    .padding(horizontal = 4.dp, vertical = 5.dp),
            )
        }
    }
}

private val ERROR_CODES = setOf(
    FormulaErrors.SYNTAX,
    FormulaErrors.DIV_ZERO,
    FormulaErrors.CYCLE,
    FormulaErrors.REF,
    FormulaErrors.VALUE,
    FormulaErrors.NUM,
)
