package com.stackpad.app.domain

import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.reminders.DueDates
import com.stackpad.app.domain.reminders.ReminderPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReminderPlannerTest {
    private val zone = ZoneId.of("UTC")
    private val day10 = DueDates.reminderTime(LocalDate.of(2026, 11, 10), zone)
    private val before = day10 - 3_600_000L // 08:00 on the 10th

    private fun note(vararg items: ChecklistItem, title: String = "Shopping") = Note(
        id = "n1",
        title = title,
        blocks = listOf(TextBlock("t", text = "hi"), ChecklistBlock("c", "List", items.toList())),
        pinned = false,
        createdAt = 0,
        updatedAt = 0,
    )

    @Test
    fun plansPendingFutureItemsOnly() {
        val n = note(
            ChecklistItem("a", "Milk", false, "2026-11-10"),
            ChecklistItem("b", "Done", true, "2026-11-10"),
            ChecklistItem("c", "No date", false, null),
            ChecklistItem("d", "Past", false, "2026-11-01"),
        )
        val plan = ReminderPlanner.plan(n, before, zone)
        assertEquals(1, plan.size)
        val spec = plan.single()
        assertEquals("n1:a", spec.key)
        assertEquals("Milk", spec.itemText)
        assertEquals("Shopping", spec.noteTitle)
        assertEquals(day10, spec.triggerAt)
    }

    @Test
    fun reminderTimeAlreadyPassedIsNotPlanned() {
        val n = note(ChecklistItem("a", "Milk", false, "2026-11-10"))
        assertTrue(ReminderPlanner.plan(n, day10 + 1, zone).isEmpty())
        assertTrue(ReminderPlanner.plan(n, day10, zone).isEmpty())
    }

    @Test
    fun invalidDatesAreIgnored() {
        val n = note(ChecklistItem("a", "Milk", false, "tomorrow"))
        assertTrue(ReminderPlanner.plan(n, before, zone).isEmpty())
    }

    @Test
    fun dueKeysIncludeCheckedItems() {
        val n = note(
            ChecklistItem("a", "x", true, "2026-11-10"),
            ChecklistItem("b", "y", false, null),
        )
        assertEquals(setOf("n1:a"), ReminderPlanner.dueKeys(n))
    }

    @Test
    fun checkingAnItemCancelsItsReminder() {
        val old = note(ChecklistItem("a", "Milk", false, "2026-11-10"))
        val new = note(ChecklistItem("a", "Milk", true, "2026-11-10"))
        val changes = ReminderPlanner.reconcile(old, new, before, zone)
        assertEquals(setOf("n1:a"), changes.cancel)
        assertTrue(changes.schedule.isEmpty())
    }

    @Test
    fun removingItemOrDateCancelsReminder() {
        val old = note(
            ChecklistItem("a", "Milk", false, "2026-11-10"),
            ChecklistItem("b", "Eggs", false, "2026-11-10"),
        )
        val new = note(ChecklistItem("a", "Milk", false, null))
        val changes = ReminderPlanner.reconcile(old, new, before, zone)
        assertEquals(setOf("n1:a", "n1:b"), changes.cancel)
        assertTrue(changes.schedule.isEmpty())
    }

    @Test
    fun editedItemIsRescheduledWithNewText() {
        val old = note(ChecklistItem("a", "Milk", false, "2026-11-10"))
        val new = note(ChecklistItem("a", "Oat milk", false, "2026-11-10"))
        val changes = ReminderPlanner.reconcile(old, new, before, zone)
        assertTrue(changes.cancel.isEmpty())
        assertEquals("Oat milk", changes.schedule.single().itemText)
    }

    @Test
    fun brandNewNoteSchedulesWithoutCancelling() {
        val new = note(ChecklistItem("a", "Milk", false, "2026-11-10"))
        val changes = ReminderPlanner.reconcile(null, new, before, zone)
        assertTrue(changes.cancel.isEmpty())
        assertEquals(1, changes.schedule.size)
    }

    @Test
    fun lockedNoteRemindersHideItsText() {
        val n = note(ChecklistItem("a", "Secret item", false, "2026-11-10")).copy(locked = true)
        val spec = ReminderPlanner.plan(n, before, zone).single()
        assertEquals("", spec.itemText)
        assertEquals("", spec.noteTitle)
        assertEquals("n1:a", spec.key)
    }
}
