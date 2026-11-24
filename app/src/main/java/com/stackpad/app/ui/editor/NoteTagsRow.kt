package com.stackpad.app.ui.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R

/**
 * One scrollable row above the title: an "Add tag" chip, then the note's tags (tap one to remove it).
 * "Add tag" opens a pop-up with every tag in use; tapping a tag adds or removes it, and "New tag" creates one.
 */
@Composable
fun NoteTagsRow(
    tags: List<String>,
    knownTags: List<String>,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit,
    onNewTag: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            AssistChip(
                onClick = { open = true },
                label = { Text(stringResource(R.string.tag_add_chip)) },
                leadingIcon = { Icon(Icons.Filled.Sell, contentDescription = null, modifier = Modifier.size(18.dp)) },
            )
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                (tags + knownTags.filterNot { it in tags }.sorted()).forEach { tag ->
                    DropdownMenuItem(
                        text = { Text("#$tag") },
                        leadingIcon = {
                            if (tag in tags) Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.cd_tag_on_note))
                        },
                        onClick = { onToggle(tag) },
                    )
                }
                if (tags.isNotEmpty() || knownTags.isNotEmpty()) HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.tag_new)) },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = {
                        open = false
                        onNewTag()
                    },
                )
            }
        }
        tags.forEach { tag ->
            InputChip(
                selected = false,
                onClick = { onRemove(tag) },
                label = { Text("#$tag") },
                trailingIcon = {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_remove_tag, tag))
                },
            )
        }
    }
}
