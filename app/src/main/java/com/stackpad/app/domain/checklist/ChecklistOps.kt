package com.stackpad.app.domain.checklist

import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.domain.reminders.DueDates

/** Pure checklist operations. */
object ChecklistOps {
    /** Items that are not done first, then done items; the order inside each group is kept. */
    fun uncheckedFirst(items: List<ChecklistItem>): List<ChecklistItem> =
        items.filterNot { it.checked } + items.filter { it.checked }

    /** True when sorting would change something (a done item sits above an open one). */
    fun canSort(items: List<ChecklistItem>): Boolean = uncheckedFirst(items) != items

    /** Open items by due date (soonest first, undated last), then done items the same way. Stable. */
    fun sortedByDueDate(items: List<ChecklistItem>): List<ChecklistItem> =
        items.sortedWith(
            compareBy<ChecklistItem>(
                { it.checked },
                { DueDates.parse(it.dueDate)?.toEpochDay() ?: Long.MAX_VALUE },
            ),
        )

    fun canSortByDueDate(items: List<ChecklistItem>): Boolean = sortedByDueDate(items) != items

    fun doneCount(items: List<ChecklistItem>): Int = items.count { it.checked }

    /** Whole-number percentage of done items; 0 for an empty list. */
    fun percent(done: Int, total: Int): Int = if (total <= 0) 0 else done * 100 / total

    fun uncheckAll(items: List<ChecklistItem>): List<ChecklistItem> =
        items.map { if (it.checked) it.copy(checked = false) else it }

    fun removeCompleted(items: List<ChecklistItem>): List<ChecklistItem> = items.filterNot { it.checked }

    /** Sets one item's checked state; with [autoSort] the done items then move to the bottom. */
    fun withChecked(items: List<ChecklistItem>, id: String, checked: Boolean, autoSort: Boolean): List<ChecklistItem> {
        val updated = items.map { if (it.id == id) it.copy(checked = checked) else it }
        return if (autoSort) uncheckedFirst(updated) else updated
    }

    /** Appends [item]; with [autoSort] it goes above the done items instead of below them. */
    fun withNewItem(items: List<ChecklistItem>, item: ChecklistItem, autoSort: Boolean): List<ChecklistItem> {
        val firstDone = items.indexOfFirst { it.checked }
        return if (!autoSort || firstDone < 0) {
            items + item
        } else {
            items.take(firstDone) + item + items.drop(firstDone)
        }
    }
}
