package com.stackpad.app.ui.editor

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.stackpad.app.R
import com.stackpad.app.util.formatDateTime

/** Small, faded line under the title: how many words the note has and when it was created. */
@Composable
fun NoteInfoLine(wordCount: Int, createdAt: Long, modifier: Modifier = Modifier) {
    val words = LocalContext.current.resources.getQuantityString(R.plurals.editor_word_count, wordCount, wordCount)
    val text = if (createdAt > 0L) {
        stringResource(R.string.editor_info_separator, words, stringResource(R.string.editor_created, formatDateTime(createdAt)))
    } else {
        words
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = modifier,
    )
}
