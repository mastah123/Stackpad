package com.stackpad.app.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R

/** Everything the Share submenu can do with the open note. */
enum class ShareTarget { COPY, TEXT, MARKDOWN, IMAGE, PDF }

private enum class MenuPage { MAIN, SHARE, COLOR }

/**
 * Overflow menu of the editor top bar. The main page lists the note actions; Share and Color
 * open in place of it (with a back row), so everything stays directly below the 3 dots.
 */
@Composable
fun NoteActionsMenu(
    color: Int,
    onSelectColor: (Int) -> Unit,
    onShare: (ShareTarget) -> Unit,
    locked: Boolean,
    onToggleLock: () -> Unit,
    onInsertTemplate: () -> Unit,
    onSaveTemplate: () -> Unit,
    onSelectBlocks: () -> Unit,
    onTrash: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(MenuPage.MAIN) }

    fun close() {
        open = false
        page = MenuPage.MAIN
    }

    Box {
        IconButton(onClick = { page = MenuPage.MAIN; open = true }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.cd_more_actions),
                tint = if (color != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = ::close) {
            when (page) {
                MenuPage.MAIN -> {
                    item(R.string.action_share, Icons.Filled.Share) { page = MenuPage.SHARE }
                    item(R.string.action_color, Icons.Filled.Palette) { page = MenuPage.COLOR }
                    item(R.string.action_select_blocks, Icons.Filled.Checklist) { close(); onSelectBlocks() }
                    item(R.string.action_templates, Icons.Filled.Dashboard) { close(); onInsertTemplate() }
                    item(R.string.action_save_template, Icons.Filled.BookmarkAdd) { close(); onSaveTemplate() }
                    item(
                        if (locked) R.string.action_unlock_note else R.string.action_lock_note,
                        if (locked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    ) { close(); onToggleLock() }
                    item(R.string.action_move_to_trash, Icons.Filled.Delete) { close(); onTrash() }
                }
                MenuPage.SHARE -> {
                    item(R.string.action_back, Icons.AutoMirrored.Filled.ArrowBack) { page = MenuPage.MAIN }
                    item(R.string.share_copy, Icons.Filled.ContentCopy) { close(); onShare(ShareTarget.COPY) }
                    item(R.string.share_text, Icons.Filled.TextFields) { close(); onShare(ShareTarget.TEXT) }
                    item(R.string.share_markdown, Icons.Filled.Description) { close(); onShare(ShareTarget.MARKDOWN) }
                    item(R.string.share_image, Icons.Filled.Image) { close(); onShare(ShareTarget.IMAGE) }
                    item(R.string.share_pdf, Icons.Filled.PictureAsPdf) { close(); onShare(ShareTarget.PDF) }
                }
                MenuPage.COLOR -> {
                    item(R.string.action_back, Icons.AutoMirrored.Filled.ArrowBack) { page = MenuPage.MAIN }
                    Box(modifier = Modifier.width(260.dp).padding(horizontal = 16.dp, vertical = 8.dp)) {
                        NoteColorPicker(selected = color, onSelect = { onSelectColor(it); close() })
                    }
                }
            }
        }
    }
}

@Composable
private fun item(labelRes: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(labelRes)) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
    )
}
