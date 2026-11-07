package com.stackpad.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.Folder
import com.stackpad.app.domain.notes.FolderNames
import com.stackpad.app.domain.notes.Tags
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.ui.components.TextInputDialog
import com.stackpad.app.ui.theme.NoteColors

/** Bottom sheet for filing a note: folder, tags and color. Changes apply immediately. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteOptionsSheet(
    folderId: String?,
    tags: List<String>,
    color: Int,
    folders: List<Folder>,
    knownTags: List<String>,
    onSelectFolder: (String?) -> Unit,
    onCreateFolder: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onSelectColor: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var newFolderDialog by remember { mutableStateOf(false) }
    var tagText by remember { mutableStateOf("") }
    val commitTag = {
        onAddTag(tagText)
        tagText = ""
    }

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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SheetLabel(stringResource(R.string.options_folder))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = folderId == null || folders.none { it.id == folderId },
                    onClick = { onSelectFolder(null) },
                    label = { Text(stringResource(R.string.folder_none)) },
                )
                folders.forEach { folder ->
                    FilterChip(
                        selected = folder.id == folderId,
                        onClick = { onSelectFolder(folder.id) },
                        label = { Text(folder.name) },
                    )
                }
                AssistChip(
                    onClick = { newFolderDialog = true },
                    label = { Text(stringResource(R.string.folder_new)) },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                )
            }

            SheetLabel(stringResource(R.string.options_tags))
            if (tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { onRemoveTag(tag) },
                            label = { Text("#$tag") },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cd_remove_tag, tag),
                                )
                            },
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                InlineTextField(
                    text = tagText,
                    onTextChange = { tagText = it.take(Tags.MAX_LENGTH + 1) },
                    placeholder = stringResource(R.string.tag_hint),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { commitTag() }),
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                )
                TextButton(onClick = commitTag, enabled = tagText.isNotBlank() && tags.size < Tags.MAX_PER_NOTE) {
                    Text(stringResource(R.string.action_add))
                }
            }
            val suggestions = knownTags.filterNot { it in tags }.take(MAX_SUGGESTIONS)
            if (suggestions.isNotEmpty() && tags.size < Tags.MAX_PER_NOTE) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestions.forEach { tag ->
                        SuggestionChip(onClick = { onAddTag(tag) }, label = { Text("#$tag") })
                    }
                }
            }

            SheetLabel(stringResource(R.string.options_color))
            ColorPicker(selected = color, onSelect = onSelectColor)
        }
    }

    if (newFolderDialog) {
        TextInputDialog(
            title = stringResource(R.string.folder_new),
            placeholder = stringResource(R.string.folder_name_hint),
            confirmLabel = stringResource(R.string.action_create),
            maxLength = FolderNames.MAX_LENGTH,
            onConfirm = {
                newFolderDialog = false
                onCreateFolder(it)
            },
            onDismiss = { newFolderDialog = false },
        )
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
private fun ColorPicker(selected: Int, onSelect: (Int) -> Unit) {
    val names = stringArrayResource(R.array.note_color_names)
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (index in 0 until NoteColors.count) {
            val accent = NoteColors.accent(index)
            val isSelected = index == selected
            val outline = MaterialTheme.colorScheme.outline
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent ?: MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(role = Role.RadioButton) { onSelect(index) }
                    .semantics {
                        contentDescription = names.getOrElse(index) { "" }
                        this.selected = isSelected
                    },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isSelected -> Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (accent == null) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black.copy(alpha = 0.75f),
                    )
                    accent == null -> Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = outline,
                    )
                }
            }
        }
    }
}

private const val MAX_SUGGESTIONS = 12
