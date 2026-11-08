package com.stackpad.app.ui.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The note's tags in one scrollable row above the title. Tapping a tag opens the options popup to edit them. */
@Composable
fun NoteTagsRow(tags: List<String>, onClick: () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tags.forEach { tag ->
            SuggestionChip(onClick = onClick, label = { Text("#$tag") })
        }
    }
}
