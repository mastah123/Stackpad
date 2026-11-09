package com.stackpad.app.domain.reminders

import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.Note
import java.time.ZoneId

/** One notification to schedule for a checklist item. */
data class ReminderSpec(
    val key: String,
    val noteId: String,
    val noteTitle: String,
    val itemText: String,
    val triggerAt: Long,
)

/** Schedules and cancels reminders. The Android implementation uses AlarmManager. */
interface ReminderScheduler {
    fun schedule(spec: ReminderSpec)
    fun cancel(key: String)
}

/** What to do with reminders after a note changed. */
data class ReminderChanges(val cancel: Set<String>, val schedule: List<ReminderSpec>)

/** Works out which reminders a note needs. Pure logic, so it is unit tested. */
object ReminderPlanner {

    fun key(noteId: String, itemId: String) = "$noteId:$itemId"

    /** Reminders for unchecked items with a due date whose reminder time is still ahead. */
    fun plan(note: Note, now: Long, zone: ZoneId): List<ReminderSpec> =
        note.blocks.filterIsInstance<ChecklistBlock>().flatMap { block ->
            block.items.mapNotNull { item ->
                if (item.checked) return@mapNotNull null
                val date = DueDates.parse(item.dueDate) ?: return@mapNotNull null
                val trigger = DueDates.reminderTime(date, zone)
                if (trigger <= now) return@mapNotNull null
                // A locked note's text must not appear in a notification.
                val title = if (note.locked) "" else note.title
                val text = if (note.locked) "" else item.text
                ReminderSpec(key(note.id, item.id), note.id, title, text, trigger)
            }
        }

    /** Keys of every checklist item that has a due date, whether or not it is still pending. */
    fun dueKeys(note: Note): Set<String> =
        note.blocks.filterIsInstance<ChecklistBlock>()
            .flatMap { it.items }
            .filter { it.dueDate != null }
            .map { key(note.id, it.id) }
            .toSet()

    /** Cancels reminders that no longer apply and (re)schedules the current ones. */
    fun reconcile(old: Note?, new: Note, now: Long, zone: ZoneId): ReminderChanges {
        val schedule = plan(new, now, zone)
        val stale = (old?.let(::dueKeys).orEmpty() + dueKeys(new)) - schedule.map { it.key }.toSet()
        return ReminderChanges(cancel = stale, schedule = schedule)
    }
}
