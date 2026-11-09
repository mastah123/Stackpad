package com.stackpad.app.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.domain.templates.NoteTemplate
import com.stackpad.app.ui.components.ConfirmDialog
import com.stackpad.app.ui.components.SmallIconButton

/** Pick a template to add to the note. Saved templates can be deleted. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerSheet(
    templates: List<NoteTemplate>,
    onPick: (NoteTemplate) -> Unit,
    onDelete: (NoteTemplate) -> Unit,
    onDismiss: () -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<NoteTemplate?>(null) }
    val builtIn = templates.filter { it.builtIn }
    val saved = templates.filterNot { it.builtIn }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(stringResource(R.string.templates_title), style = MaterialTheme.typography.titleMedium)
            Label(stringResource(R.string.templates_built_in))
            builtIn.forEach { TemplateRow(it, onClick = { onPick(it) }, onDelete = null) }
            Label(stringResource(R.string.templates_yours))
            if (saved.isEmpty()) {
                Text(
                    stringResource(R.string.templates_none_saved),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            saved.forEach { TemplateRow(it, onClick = { onPick(it) }, onDelete = { pendingDelete = it }) }
        }
    }

    pendingDelete?.let { template ->
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_template_title, template.name),
            message = stringResource(R.string.dialog_delete_template_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                onDelete(template)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun TemplateRow(template: NoteTemplate, onClick: () -> Unit, onDelete: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(template.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (onDelete != null) {
            SmallIconButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_delete_template, template.name),
                onClick = onDelete,
            )
        }
    }
}
