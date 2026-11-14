package com.stackpad.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.ui.theme.NoteColors

/** The note color swatches in one scrollable row. Index 0 means no color. */
@Composable
fun NoteColorPicker(selected: Int, onSelect: (Int) -> Unit) {
    val names = stringArrayResource(R.array.note_color_names)
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (index in 0 until NoteColors.count) {
            val accent = NoteColors.accent(index)
            val isSelected = index == selected
            val outline = MaterialTheme.colorScheme.outline
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent ?: MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(role = Role.RadioButton) { onSelect(index) }
                    .semantics {
                        contentDescription = names.getOrElse(index) { "" }
                        this.selected = isSelected
                    },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isSelected -> Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (accent == null) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black.copy(alpha = 0.75f),
                    )
                    accent == null -> Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = outline,
                    )
                }
            }
        }
    }
}
