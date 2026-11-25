package com.stackpad.app.ui.editor

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.util.formatStamp

private enum class SheetPage { MAIN, EXPORT }

private const val TILES_PER_ROW = 5

private class Tile(val icon: ImageVector, val label: Int, val danger: Boolean = false, val onClick: () -> Unit)

/**
 * Bottom sheet opened by the editor's 3 dots: the note's title, tags and dates, Add tag / Add color,
 * and a grid of actions. Export opens a second page with the file formats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteOptionsSheet(
    title: String,
    tags: List<String>,
    knownTags: List<String>,
    createdAt: Long,
    updatedAt: Long,
    color: Int,
    pinned: Boolean,
    archived: Boolean,
    locked: Boolean,
    canAddToHome: Boolean,
    onDismiss: () -> Unit,
    onToggleTag: (String) -> Unit,
    onNewTag: () -> Unit,
    onSelectColor: (Int) -> Unit,
    onOption: (NoteOption) -> Unit,
    onShare: (ShareTarget) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var page by remember { mutableStateOf(SheetPage.MAIN) }
    var showColors by remember { mutableStateOf(false) }
    var tagMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
            if (page == SheetPage.EXPORT) {
                ExportPage(onBack = { page = SheetPage.MAIN }, onShare = onShare)
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
                if (tags.isNotEmpty()) {
                    Text(
                        text = tags.joinToString("  ") { "#$it" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
                InfoRow(stringResource(R.string.sheet_created), formatStamp(createdAt))
                InfoRow(stringResource(R.string.sheet_last_edited), formatStamp(updatedAt.coerceAtLeast(createdAt)))
                HorizontalDivider()
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box {
                        AssistChip(
                            onClick = { tagMenu = true },
                            label = { Text(stringResource(R.string.tag_add_chip)) },
                            leadingIcon = { Icon(AppIcons.Sell, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        )
                        TagPickerMenu(
                            expanded = tagMenu,
                            onDismiss = { tagMenu = false },
                            tags = tags,
                            knownTags = knownTags,
                            onToggle = onToggleTag,
                            onNewTag = onNewTag,
                        )
                    }
                    AssistChip(
                        onClick = { showColors = !showColors },
                        label = { Text(stringResource(R.string.sheet_add_color)) },
                        leadingIcon = { Icon(AppIcons.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    )
                }
                if (showColors) {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        NoteColorPicker(selected = color, onSelect = onSelectColor)
                    }
                }
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                val tiles = buildList {
                    add(Tile(if (pinned) AppIcons.PushPin else AppIcons.PushPin, if (pinned) R.string.sheet_unpin else R.string.sheet_pin) { onOption(NoteOption.PIN) })
                    add(Tile(if (archived) AppIcons.Unarchive else AppIcons.Archive, if (archived) R.string.sheet_unarchive else R.string.sheet_archive) { onOption(NoteOption.ARCHIVE) })
                    add(Tile(if (locked) AppIcons.LockOpen else AppIcons.Lock, if (locked) R.string.sheet_unlock else R.string.sheet_lock) { onOption(NoteOption.LOCK) })
                    add(Tile(AppIcons.History, R.string.sheet_history) { onOption(NoteOption.HISTORY) })
                    add(Tile(AppIcons.ContentCopy, R.string.sheet_copy) { onShare(ShareTarget.COPY) })
                    add(Tile(AppIcons.Share, R.string.action_share) { onShare(ShareTarget.TEXT) })
                    add(Tile(AppIcons.FileUpload, R.string.sheet_export) { page = SheetPage.EXPORT })
                    add(Tile(AppIcons.FileCopy, R.string.sheet_duplicate) { onOption(NoteOption.DUPLICATE) })
                    if (canAddToHome) add(Tile(AppIcons.AddToHomeScreen, R.string.sheet_add_to_home) { onOption(NoteOption.ADD_TO_HOME) })
                    add(Tile(AppIcons.Checklist, R.string.sheet_select_blocks) { onOption(NoteOption.SELECT_BLOCKS) })
                    add(Tile(AppIcons.Dashboard, R.string.sheet_templates) { onOption(NoteOption.TEMPLATES) })
                    add(Tile(AppIcons.BookmarkAdd, R.string.sheet_save_template) { onOption(NoteOption.SAVE_TEMPLATE) })
                    add(Tile(AppIcons.Delete, R.string.action_move_to_trash, danger = true) { onOption(NoteOption.TRASH) })
                }
                tiles.chunked(TILES_PER_ROW).forEach { rowTiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowTiles.forEach { TileView(it, Modifier.weight(1f)) }
                        repeat(TILES_PER_ROW - rowTiles.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TileView(tile: Tile, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    val tint: Color = if (tile.danger) colors.error else colors.onSurfaceVariant
    Column(
        modifier = modifier.clip(shape).clickable(onClick = tile.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(52.dp).border(1.dp, colors.outlineVariant, shape),
            shape = shape,
            color = colors.surfaceContainerHigh,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(tile.icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
        }
        Text(
            text = stringResource(tile.label),
            style = MaterialTheme.typography.labelMedium,
            color = if (tile.danger) colors.error else colors.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun ExportPage(onBack: () -> Unit, onShare: (ShareTarget) -> Unit) {
    ExportRow(AppIcons.ArrowBack, R.string.action_back, onBack)
    HorizontalDivider()
    ExportRow(AppIcons.TextFields, R.string.share_text) { onShare(ShareTarget.TEXT) }
    ExportRow(AppIcons.Description, R.string.share_markdown) { onShare(ShareTarget.MARKDOWN) }
    ExportRow(AppIcons.Image, R.string.share_image) { onShare(ShareTarget.IMAGE) }
    ExportRow(AppIcons.PictureAsPdf, R.string.share_pdf) { onShare(ShareTarget.PDF) }
    ExportRow(AppIcons.Tune, R.string.share_options) { onShare(ShareTarget.OPTIONS) }
}

@Composable
private fun ExportRow(icon: ImageVector, label: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
    }
}
