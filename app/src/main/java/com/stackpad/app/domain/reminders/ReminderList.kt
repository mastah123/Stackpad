package com.stackpad.app.domain.reminders

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ChecklistBlock
import java.time.LocalDate

/** One pending checklist item that has a due date, with the note it belongs to. */
data class ReminderEntry(
    val noteId: String,
    val noteTitle: String,
    val itemId: String,
    val itemText: String,
    val due: LocalDate,
)

/** Builds the Reminders list: every unchecked checklist item with a due date, earliest (overdue) first. */
object ReminderList {
    fun entriesOf(noteId: String, noteTitle: String, blocks: List<Block>): List<ReminderEntry> =
        blocks.filterIsInstance<ChecklistBlock>().flatMap { block ->
            block.items.mapNotNull { item ->
                if (item.checked) return@mapNotNull null
                val due = DueDates.parse(item.dueDate) ?: return@mapNotNull null
                ReminderEntry(noteId, noteTitle, item.id, item.text, due)
            }
        }

    fun sorted(entries: List<ReminderEntry>): List<ReminderEntry> =
        entries.sortedWith(compareBy<ReminderEntry> { it.due }.thenBy { it.noteTitle.lowercase() }.thenBy { it.itemText.lowercase() })
}
