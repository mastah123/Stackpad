package com.stackpad.app.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

private enum class OptionRow { PLUS, LIST, CARDS, TAGS }

/**
 * Scrollable row of chips; each one inserts a block of that type into the note.
 * "+", List, Cards and Tag open an option row above the chips, styled like the text format toolbar.
 * [onOptionRowChange] reports whether such a row is open, so the editor can hide the text toolbar.
 */
@Composable
fun AddBlockBar(
    recordingActive: Boolean,
    onAddBlock: (Block) -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordAudio: () -> Unit,
    /** Tags on this note, every tag in use, and what tapping them or "+" does. */
    noteTags: List<String>,
    knownTags: List<String>,
    onToggleTag: (String) -> Unit,
    onNewTag: () -> Unit,
    onOptionRowChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var openRow by remember { mutableStateOf<OptionRow?>(null) }
    fun toggle(row: OptionRow) {
        openRow = if (openRow == row) null else row
    }
    LaunchedEffect(openRow) { onOptionRowChange(openRow != null) }
    // Picks an option and closes the row.
    fun choose(action: () -> Unit) {
        openRow = null
        action()
    }

    Column(modifier = modifier) {
        when (openRow) {
            OptionRow.LIST -> OptionButtons {
                OptionButton(Icons.Filled.FormatListBulleted, stringResource(R.string.add_bullets)) {
                    choose { onAddBlock(BlockFactory.list(ListKind.BULLET)) }
                }
                OptionButton(Icons.Filled.FormatListNumbered, stringResource(R.string.add_numbers)) {
                    choose { onAddBlock(BlockFactory.list(ListKind.NUMBERED)) }
                }
                OptionButton(Icons.Filled.SortByAlpha, stringResource(R.string.add_letters)) {
                    choose { onAddBlock(BlockFactory.list(ListKind.LETTERED)) }
                }
                OptionButton(Icons.Filled.CheckBox, stringResource(R.string.add_checklist)) {
                    choose { onAddBlock(BlockFactory.checklist()) }
                }
            }
            OptionRow.PLUS -> OptionButtons {
                OptionButton(Icons.Filled.PhotoLibrary, stringResource(R.string.image_from_gallery)) {
                    choose(onPickFromGallery)
                }
                OptionButton(Icons.Filled.CameraAlt, stringResource(R.string.image_from_camera)) {
                    choose(onTakePhoto)
                }
                if (!recordingActive) {
                    OptionButton(Icons.Filled.Mic, stringResource(R.string.add_audio)) { choose(onRecordAudio) }
                }
                OptionButton(Icons.Filled.Link, stringResource(R.string.add_link)) {
                    choose { onAddBlock(BlockFactory.link()) }
                }
                OptionButton(Icons.Filled.TableChart, stringResource(R.string.add_table)) {
                    choose { onAddBlock(BlockFactory.sheet()) }
                }
                OptionButton(Icons.Filled.HorizontalRule, stringResource(R.string.add_divider)) {
                    choose { onAddBlock(BlockFactory.divider()) }
                }
            }
            OptionRow.CARDS -> OptionButtons {
                OptionButton(Icons.Filled.AutoAwesome, stringResource(R.string.add_prompt)) {
                    choose { onAddBlock(BlockFactory.prompt()) }
                }
                OptionButton(Icons.Filled.FormatQuote, stringResource(R.string.add_quote)) {
                    choose { onAddBlock(BlockFactory.quote()) }
                }
                OptionButton(Icons.Filled.Code, stringResource(R.string.add_code)) {
                    choose { onAddBlock(BlockFactory.code()) }
                }
            }
            OptionRow.TAGS -> OptionButtons {
                OptionButton(Icons.Filled.Add, stringResource(R.string.tag_new), onClick = onNewTag)
                (noteTags + knownTags.filterNot { it in noteTags }.sortedBy { it }).let { all ->
                    // The note's own tags come first so they are always in view.
                    all.forEach { tag ->
                        TagToggle(tag = tag, selected = tag in noteTags, onClick = { onToggleTag(tag) })
                    }
                }
            }
            null -> Unit
        }
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AddChip(Icons.Filled.Add, stringResource(R.string.add_plus), selected = openRow == OptionRow.PLUS) {
                toggle(OptionRow.PLUS)
            }
            AddChip(Icons.Filled.TextFields, stringResource(R.string.add_text)) {
                openRow = null
                onAddBlock(BlockFactory.text())
            }
            AddChip(Icons.Filled.FormatListBulleted, stringResource(R.string.add_list), selected = openRow == OptionRow.LIST) {
                toggle(OptionRow.LIST)
            }
            AddChip(Icons.Filled.Style, stringResource(R.string.add_cards), selected = openRow == OptionRow.CARDS) {
                toggle(OptionRow.CARDS)
            }
            AddChip(Icons.Filled.Sell, stringResource(R.string.add_tag), selected = openRow == OptionRow.TAGS) {
                toggle(OptionRow.TAGS)
            }
        }
    }
}

@Composable
private fun OptionButtons(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

@Composable
private fun TagToggle(tag: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) colors.primaryContainer else colors.surface,
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Checkbox, onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "#$tag", style = MaterialTheme.typography.labelLarge, color = colors.onSurface)
        }
    }
}

@Composable
private fun OptionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    FormatButton(isSelected = false, description = label, onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun AddChip(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primaryContainer else colors.surfaceVariant,
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
