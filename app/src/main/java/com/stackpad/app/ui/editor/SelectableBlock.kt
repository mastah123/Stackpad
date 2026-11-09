package com.stackpad.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
 * Shows a block normally, or, while [selecting], covers it with a tap target that toggles whether it
 * is selected (so taps never reach the block underneath).
 */
@Composable
fun SelectableBlock(
    selecting: Boolean,
    selected: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box {
        content()
        if (selecting) {
            val colors = MaterialTheme.colorScheme
            val description = stringResource(if (selected) R.string.cd_block_selected else R.string.cd_block_not_selected)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) colors.primary.copy(alpha = 0.14f) else Color.Transparent)
                    .clickable(role = Role.Checkbox, onClick = onToggle)
                    .semantics { contentDescription = description },
            ) {
                Icon(
                    imageVector = if (selected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = null,
                    tint = if (selected) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(20.dp),
                )
            }
        }
    }
}
