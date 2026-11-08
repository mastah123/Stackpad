package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.domain.reminders.DueDates
import com.stackpad.app.domain.reminders.DueStatus
import com.stackpad.app.ui.components.SmallIconButton
import java.time.LocalDate

/** Calendar icon when a checklist item has no due date, otherwise a chip showing the date. */
@Composable
fun DueDateButton(
    dueDate: String?,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val date = DueDates.parse(dueDate)
    if (date == null) {
        SmallIconButton(
            icon = Icons.Filled.Event,
            contentDescription = stringResource(R.string.cd_set_due_date),
            onClick = onClick,
            size = 28.dp,
            iconSize = 16.dp,
            modifier = modifier,
        )
        return
    }

    val today = remember { LocalDate.now() }
    val status = DueDates.status(date, today)
    val overdue = status == DueStatus.OVERDUE && !checked
    val colors = MaterialTheme.colorScheme
    val label = when (status) {
        DueStatus.TODAY -> stringResource(R.string.due_today)
        DueStatus.TOMORROW -> stringResource(R.string.due_tomorrow)
        else -> DueDates.shortLabel(date, today)
    }
    val description = if (overdue) {
        stringResource(R.string.cd_due_overdue, label)
    } else {
        stringResource(R.string.cd_due_date, label)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (overdue) colors.errorContainer else colors.surface,
        modifier = modifier
            .padding(start = 4.dp, top = 3.dp, end = 2.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (overdue) colors.onErrorContainer else colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
        )
    }
}

/** Date picker for a checklist item's due date, with a way to clear it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDateDialog(
    initial: String?,
    onConfirm: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial?.let(DueDates::toPickerMillis))
    val selected = state.selectedDateMillis

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { selected?.let { onConfirm(DueDates.fromPickerMillis(it)) } },
                enabled = selected != null,
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            Row {
                if (initial != null) {
                    TextButton(onClick = onClear) { Text(stringResource(R.string.due_remove)) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
            }
        },
    ) {
        DatePicker(state = state)
    }
}
