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
}
