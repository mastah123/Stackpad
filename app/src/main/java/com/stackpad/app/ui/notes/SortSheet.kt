package com.stackpad.app.ui.notes

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.domain.notes.ListOrder
import com.stackpad.app.domain.notes.NoteGroup
import com.stackpad.app.domain.notes.NoteSort

/** Sort icon that opens the Sort by / Group by sheet. */
@Composable
fun SortButton(order: ListOrder, onChange: (ListOrder) -> Unit) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(AppIcons.SwapVert, contentDescription = stringResource(R.string.cd_sort))
    }
    if (open) SortSheet(order = order, onChange = onChange, onDismiss = { open = false })
}

/**
 * Bottom sheet with Sort by (date edited, date created, title, and the direction) and Group by
 * (default, none, Abc, year, week, month). Changes apply straight away; the sheet stays open.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(order: ListOrder, onChange: (ListOrder) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.sort_by),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onChange(order.copy(reversed = !order.reversed)) }) {
                    Text(stringResource(directionLabel(order)))
                    Icon(
                        imageVector = if (order.reversed) AppIcons.ArrowUpward else AppIcons.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
            NoteSort.values().forEach { sort ->
                SheetOption(stringResource(sort.label()), order.sort == sort) {
                    // A new field starts in its usual direction.
                    onChange(order.copy(sort = sort, reversed = if (sort == order.sort) order.reversed else false))
                }
            }
            Text(
                text = stringResource(R.string.group_by),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp),
            )
            NoteGroup.values().forEach { group ->
                SheetOption(stringResource(group.label()), order.group == group) { onChange(order.copy(group = group)) }
            }
        }
    }
}

@Composable
private fun SheetOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) colors.secondaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) colors.onSecondaryContainer else colors.onSurface,
        )
        if (selected) Icon(AppIcons.Check, contentDescription = null, tint = colors.primary)
    }
}

private fun directionLabel(order: ListOrder): Int = when {
    order.sort == NoteSort.TITLE && order.reversed -> R.string.sort_dir_z_a
    order.sort == NoteSort.TITLE -> R.string.sort_dir_a_z
    order.reversed -> R.string.sort_dir_old_new
    else -> R.string.sort_dir_new_old
}

private fun NoteSort.label(): Int = when (this) {
    NoteSort.LAST_EDITED -> R.string.sort_last_edited
    NoteSort.CREATED -> R.string.sort_created
    NoteSort.TITLE -> R.string.sort_title
}

private fun NoteGroup.label(): Int = when (this) {
    NoteGroup.DEFAULT -> R.string.group_default
    NoteGroup.NONE -> R.string.group_none
    NoteGroup.ABC -> R.string.group_abc
    NoteGroup.YEAR -> R.string.group_year
    NoteGroup.WEEK -> R.string.group_week
    NoteGroup.MONTH -> R.string.group_month
}
