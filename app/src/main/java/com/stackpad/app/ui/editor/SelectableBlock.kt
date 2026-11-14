package com.stackpad.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.stackpad.app.R

/**
 * Shows a block normally, or, while [selecting], puts a checkbox of one fixed size in a column before it
 * (whatever the block's size) and makes the whole block a tap target that toggles the selection, so taps
 * never reach the block underneath.
 */
@Composable
fun SelectableBlock(
    selecting: Boolean,
    selected: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (!selecting) {
        content()
        return
    }
    val colors = MaterialTheme.colorScheme
    val description = stringResource(if (selected) R.string.cd_block_selected else R.string.cd_block_not_selected)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.primary.copy(alpha = 0.14f) else Color.Transparent)
            .clickable(role = Role.Checkbox, onClick = onToggle)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.Top,
    ) {
        // Fixed 40 dp column with a 24 dp icon, aligned to the first line of the block.
        Box(modifier = Modifier.size(CHECKBOX_COLUMN), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (selected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                contentDescription = null,
                tint = if (selected) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(CHECKBOX_ICON),
            )
        }
        // The block is shown for reference only: a transparent layer above it takes every touch.
        Box(modifier = Modifier.weight(1f)) {
            content()
            Box(modifier = Modifier.matchParentSize().clickable(onClick = onToggle))
        }
    }
}

private val CHECKBOX_COLUMN = 40.dp
private val CHECKBOX_ICON = 24.dp
