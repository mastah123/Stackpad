package com.stackpad.app.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.ListKind

/** Scrollable row of chips; each one inserts a block of that type into the note. */
@Composable
fun AddBlockBar(
    recordingActive: Boolean,
    onAddBlock: (Block) -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordAudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var imageMenuOpen by remember { mutableStateOf(false) }
    var listMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AddChip(Icons.Filled.TextFields, stringResource(R.string.add_text)) { onAddBlock(BlockFactory.text()) }
        Box {
            AddChip(Icons.Filled.FormatListBulleted, stringResource(R.string.add_list)) { listMenuOpen = true }
            DropdownMenu(expanded = listMenuOpen, onDismissRequest = { listMenuOpen = false }) {
                ListMenuItem(Icons.Filled.FormatListBulleted, stringResource(R.string.add_bullets)) {
                    listMenuOpen = false
                    onAddBlock(BlockFactory.list(ListKind.BULLET))
                }
                ListMenuItem(Icons.Filled.FormatListNumbered, stringResource(R.string.add_numbers)) {
                    listMenuOpen = false
                    onAddBlock(BlockFactory.list(ListKind.NUMBERED))
                }
                ListMenuItem(Icons.Filled.SortByAlpha, stringResource(R.string.add_letters)) {
                    listMenuOpen = false
                    onAddBlock(BlockFactory.list(ListKind.LETTERED))
                }
                ListMenuItem(Icons.Filled.CheckBox, stringResource(R.string.add_checklist)) {
                    listMenuOpen = false
                    onAddBlock(BlockFactory.checklist())
                }
            }
        }
        Box {
            AddChip(Icons.Filled.Image, stringResource(R.string.add_image)) { imageMenuOpen = true }
            DropdownMenu(expanded = imageMenuOpen, onDismissRequest = { imageMenuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.image_from_gallery)) },
                    leadingIcon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                    onClick = {
                        imageMenuOpen = false
                        onPickFromGallery()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.image_from_camera)) },
                    leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                    onClick = {
                        imageMenuOpen = false
                        onTakePhoto()
                    },
                )
            }
        }
        AddChip(Icons.Filled.TableChart, stringResource(R.string.add_table)) { onAddBlock(BlockFactory.sheet()) }
        AddChip(Icons.Filled.Mic, stringResource(R.string.add_audio), enabled = !recordingActive, onClick = onRecordAudio)
        AddChip(Icons.Filled.Link, stringResource(R.string.add_link)) { onAddBlock(BlockFactory.link()) }
        AddChip(Icons.Filled.AutoAwesome, stringResource(R.string.add_prompt)) { onAddBlock(BlockFactory.prompt()) }
    }
}

@Composable
private fun ListMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
    )
}

@Composable
private fun AddChip(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceVariant,
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (enabled) colors.primary else contentColor,
            )
            Spacer(Modifier.width(5.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = contentColor)
        }
    }
}
