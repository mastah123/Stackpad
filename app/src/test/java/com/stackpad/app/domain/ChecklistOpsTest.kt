package com.stackpad.app.domain

import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.domain.checklist.ChecklistOps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistOpsTest {
    private fun item(id: String, checked: Boolean) = ChecklistItem(id = id, text = id, checked = checked)

    @Test fun movesDoneItemsToTheBottomKeepingOrder() {
        val items = listOf(item("a", true), item("b", false), item("c", true), item("d", false))
        assertEquals(listOf("b", "d", "a", "c"), ChecklistOps.uncheckedFirst(items).map { it.id })
    }

    @Test fun alreadySortedCannotSort() {
        assertFalse(ChecklistOps.canSort(listOf(item("a", false), item("b", true))))
        assertFalse(ChecklistOps.canSort(emptyList()))
        assertFalse(ChecklistOps.canSort(listOf(item("a", true))))
    }

    @Test fun mixedCanSort() {
        assertTrue(ChecklistOps.canSort(listOf(item("a", true), item("b", false))))
    }

    @Test fun keepsAllItems() {
        val items = listOf(item("a", true), item("b", true))
        assertEquals(items, ChecklistOps.uncheckedFirst(items))
    }

    private fun due(id: String, date: String?, checked: Boolean = false) =
        ChecklistItem(id = id, text = id, checked = checked, dueDate = date)

    @Test fun sortsByDueDateWithUndatedAndDoneLast() {
        val items = listOf(
            due("none", null),
            due("late", "2026-12-01"),
            due("doneEarly", "2026-01-01", checked = true),
            due("soon", "2026-11-02"),
        )
        assertEquals(listOf("soon", "late", "none", "doneEarly"), ChecklistOps.sortedByDueDate(items).map { it.id })
        assertTrue(ChecklistOps.canSortByDueDate(items))
        assertFalse(ChecklistOps.canSortByDueDate(ChecklistOps.sortedByDueDate(items)))
    }

    @Test fun dueDateSortKeepsOrderOfEqualDates() {
        val items = listOf(due("a", "2026-11-02"), due("b", "2026-11-02"), due("c", null), due("d", null))
        assertEquals(listOf("a", "b", "c", "d"), ChecklistOps.sortedByDueDate(items).map { it.id })
    }

    @Test fun percentIsWholeNumberAndSafeWhenEmpty() {
        assertEquals(0, ChecklistOps.percent(0, 0))
        assertEquals(33, ChecklistOps.percent(1, 3))
        assertEquals(100, ChecklistOps.percent(4, 4))
    }

    @Test fun uncheckAllAndRemoveCompleted() {
        val items = listOf(item("a", true), item("b", false), item("c", true))
        assertEquals(listOf(false, false, false), ChecklistOps.uncheckAll(items).map { it.checked })
        assertEquals(listOf("b"), ChecklistOps.removeCompleted(items).map { it.id })
        assertEquals(2, ChecklistOps.doneCount(items))
    }

    @Test fun tickingWithAutoSortMovesItemDown() {
        val items = listOf(item("a", false), item("b", false), item("c", false))
        val ticked = ChecklistOps.withChecked(items, "a", true, autoSort = true)
        assertEquals(listOf("b", "c", "a"), ticked.map { it.id })
        val kept = ChecklistOps.withChecked(items, "a", true, autoSort = false)
        assertEquals(listOf("a", "b", "c"), kept.map { it.id })
    }

    @Test fun untickingWithAutoSortKeepsDoneAtBottom() {
        val items = listOf(item("a", false), item("b", true), item("c", true))
        val result = ChecklistOps.withChecked(items, "c", false, autoSort = true)
        assertEquals(listOf("a", "c", "b"), result.map { it.id })
    }

    @Test fun newItemGoesAboveDoneItemsOnlyWithAutoSort() {
        val items = listOf(item("a", false), item("b", true))
        val fresh = item("n", false)
        assertEquals(listOf("a", "n", "b"), ChecklistOps.withNewItem(items, fresh, true).map { it.id })
        assertEquals(listOf("a", "b", "n"), ChecklistOps.withNewItem(items, fresh, false).map { it.id })
        assertEquals(listOf("a", "n"), ChecklistOps.withNewItem(listOf(item("a", false)), fresh, true).map { it.id })
    }
}
