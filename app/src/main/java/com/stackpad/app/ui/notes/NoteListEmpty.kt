package com.stackpad.app.ui.notes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import kotlin.random.Random

private val TIPS = listOf(
    R.string.tip_pin,
    R.string.tip_tags,
    R.string.tip_folders,
    R.string.tip_checklist,
    R.string.tip_table,
    R.string.tip_search,
)

/**
 * Shown when the list has nothing to show. With no notes at all it is a tip (a different one each
 * time the screen is opened) and a "Create a new note" button; search and filter results get a short message.
 */
@Composable
fun NoteListEmpty(query: String, filtered: Boolean, onCreate: () -> Unit) {
    if (query.isBlank() && !filtered) {
        EmptyNotesTip(onCreate)
        return
    }
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = when {
                query.isNotBlank() && filtered -> stringResource(R.string.empty_search_filtered, query)
                query.isNotBlank() -> stringResource(R.string.empty_search, query)
                else -> stringResource(R.string.empty_filtered)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyNotesTip(onCreate: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tip = rememberSaveable { Random.nextInt(TIPS.size) }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.onSurfaceVariant)
            Text(
                text = stringResource(R.string.tip_label).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(TIPS[tip]),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
        )
        OutlinedButton(onClick = onCreate, shape = RoundedCornerShape(10.dp), modifier = Modifier.align(Alignment.Start)) {
            Text(stringResource(R.string.empty_create_note))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp).size(18.dp),
            )
        }
    }
}
