package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stackpad.app.R
import com.stackpad.app.domain.image.ImageEdit
import java.io.File

/**
 * Full-width image. Tap it to view full screen with zoom. The crop button opens the picture editor
 * (crop, rotate, flip, straighten); after an edit it opens a small menu with Edit picture and
 * Restore original instead. The "x" removes the image (and its file).
 */
@Composable
fun ImageBlockView(
    file: File,
    hasOriginal: Boolean,
    onSaveEdit: suspend (ImageEdit) -> Boolean,
    onRestoreOriginal: () -> Unit,
    onReadText: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val available = remember(file) { file.exists() && file.length() > 0 }
    var viewing by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var editingPicture by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().clip(shape)) {
        if (available) {
            AsyncImage(
                model = file,
                contentDescription = stringResource(R.string.cd_image),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(
                        onClickLabel = stringResource(R.string.cd_view_image),
                        role = Role.Image,
                    ) { viewing = true },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.image_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (available) {
            Box(modifier = Modifier.align(Alignment.TopStart)) {
                OverlayButton(
                    icon = Icons.Filled.Crop,
                    description = stringResource(R.string.cd_image_options),
                    onClick = { menuOpen = true },
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.image_edit_menu_edit)) },
                        leadingIcon = { Icon(Icons.Filled.Crop, contentDescription = null) },
                        onClick = { menuOpen = false; editingPicture = true },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.image_menu_read_text)) },
                        leadingIcon = { Icon(Icons.Filled.TextFields, contentDescription = null) },
                        onClick = { menuOpen = false; onReadText() },
                    )
                    if (hasOriginal) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.image_edit_restore_original)) },
                            leadingIcon = { Icon(Icons.Filled.Restore, contentDescription = null) },
                            onClick = { menuOpen = false; onRestoreOriginal() },
                        )
                    }
                }
            }
        }
        OverlayButton(
            icon = Icons.Filled.Close,
            description = stringResource(R.string.cd_remove_image),
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }

    if (viewing) {
        ImageViewer(
            file = file,
            onDismiss = { viewing = false },
            onEdit = { viewing = false; editingPicture = true },
        )
    }
    if (editingPicture) {
        ImageEditorDialog(file = file, onDismiss = { editingPicture = false }, onSave = onSaveEdit)
    }
}

@Composable
private fun OverlayButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(6.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(16.dp),
            tint = Color.White,
        )
    }
}
