package com.stackpad.app.ui.editor

import com.stackpad.app.ui.icons.AppIcons
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
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
import com.stackpad.app.ui.editor.blocks.BulletMark
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.domain.text.ListMarkers
import com.stackpad.app.domain.text.isBullet

private enum class OptionRow { PLUS, LIST, CARDS }

/** The third row that opens above the List row (Bullets, Numbers, Letters) or the + row (Image). */
private enum class StyleRow { BULLETS, NUMBERS, LETTERS, CHECKLIST, IMAGE }

/**
 * Scrollable row of chips; each one inserts a block of that type into the note.
 * "+", List and Cards open an option row above the chips, styled like the text format toolbar;
 * in the List row, Bullets, Numbers, Letters and Checklist open a third row (styles, or Checkbox list and Task list), and Image in the + row opens Gallery and Photo.
 * [onOptionRowChange] reports whether such a row is open, so the editor can hide the text toolbar.
 */
@Composable
fun AddBlockBar(
    recordingActive: Boolean,
    onAddBlock: (Block) -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordAudio: () -> Unit,
    onOptionRowChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var openRow by remember { mutableStateOf<OptionRow?>(null) }
    var styleRow by remember { mutableStateOf<StyleRow?>(null) }
    fun toggle(row: OptionRow) {
        styleRow = null
        openRow = if (openRow == row) null else row
    }
    LaunchedEffect(openRow) { onOptionRowChange(openRow != null) }
    // Picks an option and closes the row.
    fun choose(action: () -> Unit) {
        openRow = null
        styleRow = null
        action()
    }

    Column(modifier = modifier) {
        // The third row (styles, or Gallery and Photo) sits above the option row that opened it.
        styleRow?.let { group ->
            OptionButtons {
                when (group) {
                    StyleRow.IMAGE -> {
                        OptionButton(AppIcons.PhotoLibrary, stringResource(R.string.image_from_gallery)) {
                            choose(onPickFromGallery)
                        }
                        OptionButton(AppIcons.CameraAlt, stringResource(R.string.image_from_camera)) {
                            choose(onTakePhoto)
                        }
                    }
                    StyleRow.CHECKLIST -> {
                        OptionButton(AppIcons.CheckBoxOutlineBlank, stringResource(R.string.add_checkbox_list)) {
                            choose { onAddBlock(BlockFactory.list(ListKind.CHECKBOX)) }
                        }
                        OptionButton(AppIcons.Checklist, stringResource(R.string.add_task_list)) {
                            choose { onAddBlock(BlockFactory.checklist()) }
                        }
                    }
                    else -> {
                        val kinds = when (group) {
                            StyleRow.BULLETS -> ListMarkers.BULLET_KINDS
                            StyleRow.NUMBERS -> ListMarkers.NUMBER_KINDS
                            else -> ListMarkers.LETTER_KINDS
                        }
                        kinds.forEach { kind ->
                            StyleButton(kind) { choose { onAddBlock(BlockFactory.list(kind)) } }
                        }
                    }
                }
            }
        }
        when (openRow) {
            OptionRow.LIST -> OptionButtons {
                OptionButton(
                    AppIcons.FormatListBulleted,
                    stringResource(R.string.add_bullets),
                    expanded = styleRow == StyleRow.BULLETS,
                ) { styleRow = if (styleRow == StyleRow.BULLETS) null else StyleRow.BULLETS }
                OptionButton(
                    AppIcons.FormatListNumbered,
                    stringResource(R.string.add_numbers),
                    expanded = styleRow == StyleRow.NUMBERS,
                ) { styleRow = if (styleRow == StyleRow.NUMBERS) null else StyleRow.NUMBERS }
                OptionButton(
                    AppIcons.SortByAlpha,
                    stringResource(R.string.add_letters),
                    expanded = styleRow == StyleRow.LETTERS,
                ) { styleRow = if (styleRow == StyleRow.LETTERS) null else StyleRow.LETTERS }
                OptionButton(
                    AppIcons.CheckBox,
                    stringResource(R.string.add_checklist),
                    expanded = styleRow == StyleRow.CHECKLIST,
                ) { styleRow = if (styleRow == StyleRow.CHECKLIST) null else StyleRow.CHECKLIST }
            }
            OptionRow.PLUS -> OptionButtons {
                OptionButton(
                    AppIcons.PhotoLibrary,
                    stringResource(R.string.add_image),
                    expanded = styleRow == StyleRow.IMAGE,
                ) { styleRow = if (styleRow == StyleRow.IMAGE) null else StyleRow.IMAGE }
                if (!recordingActive) {
                    OptionButton(AppIcons.Mic, stringResource(R.string.add_audio)) { choose(onRecordAudio) }
                }
                OptionButton(AppIcons.Link, stringResource(R.string.add_link)) {
                    choose { onAddBlock(BlockFactory.link()) }
                }
                OptionButton(AppIcons.TableChart, stringResource(R.string.add_table)) {
                    choose { onAddBlock(BlockFactory.sheet()) }
                }
                OptionButton(AppIcons.HorizontalRule, stringResource(R.string.add_divider)) {
                    choose { onAddBlock(BlockFactory.divider()) }
                }
            }
            OptionRow.CARDS -> OptionButtons {
                OptionButton(AppIcons.AutoAwesome, stringResource(R.string.add_prompt)) {
                    choose { onAddBlock(BlockFactory.prompt()) }
                }
                OptionButton(AppIcons.FormatQuote, stringResource(R.string.add_quote)) {
                    choose { onAddBlock(BlockFactory.quote()) }
                }
                OptionButton(AppIcons.Code, stringResource(R.string.add_code)) {
                    choose { onAddBlock(BlockFactory.code()) }
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
            AddChip(AppIcons.Add, stringResource(R.string.add_plus), selected = openRow == OptionRow.PLUS) {
                toggle(OptionRow.PLUS)
            }
            AddChip(AppIcons.TextFields, stringResource(R.string.add_text)) {
                openRow = null
                onAddBlock(BlockFactory.text())
            }
            AddChip(AppIcons.FormatListBulleted, stringResource(R.string.add_list), selected = openRow == OptionRow.LIST) {
                toggle(OptionRow.LIST)
            }
            AddChip(AppIcons.Style, stringResource(R.string.add_cards), selected = openRow == OptionRow.CARDS) {
                toggle(OptionRow.CARDS)
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

/** One list style in the third row, shown by its marker (`1. 2. 3.`, `A. B. C.`, `•`, ...). */
@Composable
private fun StyleButton(kind: ListKind, onClick: () -> Unit) {
    FormatButton(isSelected = false, description = stringResource(kind.labelRes()), onClick = onClick) {
        if (kind.isBullet) {
            BulletMark(kind = kind, color = LocalContentColor.current, lineHeight = 20.dp, modifier = Modifier.width(20.dp))
        } else {
            Text(text = (0..2).joinToString("  ") { ListMarkers.label(kind, it) }, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun ListKind.labelRes(): Int = when (this) {
    ListKind.BULLET -> R.string.list_style_bullet
    ListKind.BULLET_HOLLOW -> R.string.list_style_hollow
    ListKind.BULLET_SQUARE -> R.string.list_style_square
    ListKind.NUMBERED -> R.string.list_style_numbers
    ListKind.UPPER_ALPHA -> R.string.list_style_upper_letters
    ListKind.LETTERED -> R.string.list_style_lower_letters
    ListKind.UPPER_ROMAN -> R.string.list_style_upper_roman
    ListKind.LOWER_ROMAN -> R.string.list_style_lower_roman
    ListKind.GREEK -> R.string.list_style_greek
    ListKind.CHECKBOX -> R.string.add_checkbox_list
}

@Composable
private fun OptionButton(icon: ImageVector, label: String, expanded: Boolean? = null, onClick: () -> Unit) {
    FormatButton(isSelected = expanded == true, description = label, onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        // A small arrow shows that Bullets and Numbers open their own style row.
        if (expanded != null) {
            Icon(
                imageVector = if (expanded) AppIcons.KeyboardArrowDown else AppIcons.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
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
    val contentColor = when {
        !enabled -> colors.onSurface.copy(alpha = 0.38f)
        selected -> colors.onPrimary
        else -> colors.onSurface
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primary else colors.surfaceVariant,
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
                tint = if (selected || !enabled) contentColor else colors.primary,
            )
            Spacer(Modifier.width(5.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = contentColor)
        }
    }
}
