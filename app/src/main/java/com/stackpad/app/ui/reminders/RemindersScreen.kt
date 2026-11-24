package com.stackpad.app.ui.reminders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackpad.app.R
import com.stackpad.app.domain.reminders.DueDates
import com.stackpad.app.domain.reminders.DueStatus
import com.stackpad.app.domain.reminders.ReminderEntry
import java.time.LocalDate

/** Every pending checklist reminder, earliest first. Tapping one opens its note. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminders_title), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        val current = reminders
        when {
            current == null -> Box(Modifier.fillMaxSize().padding(padding))
            current.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.reminders_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(current, key = { "${it.noteId}:${it.itemId}" }) { entry ->
                    ReminderCard(entry, today, onClick = { onOpenNote(entry.noteId) }, modifier = Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(entry: ReminderEntry, today: LocalDate, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val status = DueDates.status(entry.due, today)
    val label = DueDates.shortLabel(entry.due, today)
    val overdue = status == DueStatus.OVERDUE
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = if (overdue) stringResource(R.string.reminder_overdue, label) else label,
                style = MaterialTheme.typography.labelMedium,
                color = if (overdue) colors.error else colors.primary,
            )
            Text(
                text = entry.itemText.ifBlank { stringResource(R.string.reminder_untitled_item) },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = entry.noteTitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
