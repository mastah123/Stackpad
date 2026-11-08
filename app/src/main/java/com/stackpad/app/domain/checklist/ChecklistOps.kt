package com.stackpad.app.domain.checklist

import com.stackpad.app.data.model.ChecklistItem

/** Pure checklist operations. */
object ChecklistOps {
    /** Items that are not done first, then done items; the order inside each group is kept. */
    fun uncheckedFirst(items: List<ChecklistItem>): List<ChecklistItem> =
        items.filterNot { it.checked } + items.filter { it.checked }

    /** True when sorting would change something (a done item sits above an open one). */
    fun canSort(items: List<ChecklistItem>): Boolean = uncheckedFirst(items) != items
}
