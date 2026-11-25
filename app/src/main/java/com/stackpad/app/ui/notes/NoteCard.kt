package com.stackpad.app.ui.notes

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.ui.components.highlightMatches
import com.stackpad.app.domain.notes.AutoTitle
import com.stackpad.app.ui.theme.NoteColors
import com.stackpad.app.ui.theme.noteCardColor
import com.stackpad.app.util.formatListStamp

@OptIn(ExperimentalFoundationApi::class)
/**
 * One note. In the list it is a flat row (time, bold title, preview, tag chips, three-dot menu) with a
 * left accent bar for pinned or colored notes; in the grid the same content sits in a rounded card.
 */
@Composable
fun NoteCard(
    note: NoteSummary,
    onClick: () -> Unit,
    actions: NoteCardActions,
    modifier: Modifier = Modifier,
    grid: Boolean = false,
    /** Search words to mark in the title, preview and tags. */
    highlight: List<String> = emptyList(),
    /** True while several notes are being selected: the card shows a checkbox instead of the menu. */
    selecting: Boolean = false,
    selected: Boolean = false,
    onLongClick: () -> Unit = {},
    /** Lines of plain preview shown while searching (matches are highlighted). */
    previewLines: Int = 2,
    /** Tallest the formatted preview may grow; the grid allows more than the list. */
    previewMaxHeight: Dp = 96.dp,
    /** How many blocks the formatted preview shows at most. */
    previewItems: Int = 5,
) {
    val colors = MaterialTheme.colorScheme
    val mark = colors.tertiary.copy(alpha = 0.3f)
    val cardColor = when {
        note.color != 0 -> noteCardColor(note.color)
        note.pinned -> colors.surfaceContainer
        else -> colors.surfaceContainerLow
    }
    val background by animateColorAsState(
        targetValue = if (selected) colors.primaryContainer else cardColor,
        animationSpec = tween(180),
        label = "noteBackground",
    )
    val accent = NoteColors.accent(note.color) ?: if (note.pinned) colors.primary else null
    val shape = RoundedCornerShape(12.dp)
    val barWidth = with(LocalDensity.current) { ACCENT_BAR_WIDTH.toPx() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), shape)
            .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
            .drawBehind { if (accent != null) drawRect(accent, size = Size(barWidth, size.height)) },
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f).padding(top = 4.dp)) {
                Text(
                    text = formatListStamp(note.updatedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    if (note.locked) {
                        Icon(
                            AppIcons.Lock,
                            contentDescription = stringResource(R.string.cd_locked_note),
                            modifier = Modifier.padding(end = 4.dp).size(14.dp),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = highlightMatches(
                            AutoTitle.orDefault(note.title, note.createdAt),
                            highlight,
                            mark,
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface,
                        maxLines = if (grid) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val formatted = if (grid && highlight.isEmpty() && !note.locked && note.blocksJson.isNotBlank()) {
                    rememberPreviewItems(note, previewItems)
                } else {
                    null
                }
                if (formatted != null) {
                    if (formatted.isNotEmpty()) {
                        NotePreviewView(
                            items = formatted,
                            maxHeight = previewMaxHeight,
                            fadeInto = if (selected) colors.primaryContainer else background,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                } else if (note.preview.isNotBlank()) {
                    Text(
                        text = highlightMatches(note.preview, highlight, mark),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                        maxLines = previewLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                if (note.pinned || note.tags.isNotEmpty()) {
                    TagChipsRow(
                        tags = note.tags,
                        pinned = note.pinned,
                        highlight = highlight,
                        mark = mark,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
            if (selecting) {
                Icon(
                    imageVector = if (selected) AppIcons.CheckBox else AppIcons.CheckBoxOutlineBlank,
                    contentDescription = stringResource(if (selected) R.string.cd_block_selected else R.string.cd_block_not_selected),
                    tint = if (selected) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            } else {
                NoteCardMenu(pinned = note.pinned, actions = actions)
            }
        }
    }
}

private val ACCENT_BAR_WIDTH = 4.dp
