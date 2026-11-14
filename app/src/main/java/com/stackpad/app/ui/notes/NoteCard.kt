package com.stackpad.app.ui.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.ui.components.SmallIconButton
import com.stackpad.app.ui.components.highlightMatches
import com.stackpad.app.ui.theme.noteCardColor
import com.stackpad.app.util.formatDate

@OptIn(ExperimentalFoundationApi::class)
/** Compact row for one note: title, two-line preview, tags, date, and pin/trash actions. */
@Composable
fun NoteCard(
    note: NoteSummary,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    /** Search words to mark in the title, preview and tags. */
    highlight: List<String> = emptyList(),
    /** True while several notes are being selected: the card shows a checkbox instead of pin and trash. */
    selecting: Boolean = false,
    selected: Boolean = false,
    onLongClick: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val mark = colors.tertiary.copy(alpha = 0.3f)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.primaryContainer else noteCardColor(note.color),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.locked) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.cd_locked_note),
                            modifier = Modifier.padding(end = 4.dp).size(14.dp),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = highlightMatches(
                            note.title.ifBlank { stringResource(R.string.note_untitled) },
                            highlight,
                            mark,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (note.preview.isNotBlank()) {
                    Text(
                        text = highlightMatches(note.preview, highlight, mark),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (note.tags.isNotEmpty()) {
                    Text(
                        text = highlightMatches(note.tags.joinToString("  ") { "#$it" }, highlight, mark),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text(
                    text = formatDate(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (selecting) {
                Icon(
                    imageVector = if (selected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = stringResource(if (selected) R.string.cd_block_selected else R.string.cd_block_not_selected),
                    tint = if (selected) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            } else {
            SmallIconButton(
                icon = if (note.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = stringResource(if (note.pinned) R.string.cd_unpin else R.string.cd_pin),
                onClick = onTogglePin,
                tint = if (note.pinned) colors.primary else colors.onSurfaceVariant,
            )
            SmallIconButton(
                icon = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.cd_trash_note),
                onClick = onDelete,
            )
            }
        }
    }
}
