package com.stackpad.app.domain

import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.domain.notes.FolderFilter
import com.stackpad.app.domain.notes.NoteFilter
import com.stackpad.app.domain.notes.NoteOrganizer
import com.stackpad.app.domain.notes.NoteSort
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteOrganizerTest {
    private fun note(
        id: String,
        title: String = id,
        pinned: Boolean = false,
        created: Long = 0,
        updated: Long = 0,
        folder: String? = null,
        tags: List<String> = emptyList(),
    ) = NoteSummary(id, title, "", pinned, created, updated, folder, tags)

    private fun ids(list: List<NoteSummary>) = list.map { it.id }

    @Test
    fun lastEditedIsNewestFirstWithPinnedOnTop() {
        val notes = listOf(note("a", updated = 1), note("b", updated = 3), note("c", updated = 2, pinned = true))
        assertEquals(listOf("c", "b", "a"), ids(NoteOrganizer.organize(notes, NoteSort.LAST_EDITED, NoteFilter())))
    }

    @Test
    fun createdSortUsesCreationTime() {
        val notes = listOf(note("a", created = 5, updated = 9), note("b", created = 8, updated = 1))
        assertEquals(listOf("b", "a"), ids(NoteOrganizer.organize(notes, NoteSort.CREATED, NoteFilter())))
    }

    @Test
    fun titleSortIsCaseInsensitiveAndUntitledLast() {
        val notes = listOf(note("1", "banana"), note("2", ""), note("3", "Apple"), note("4", "cherry"))
        assertEquals(listOf("3", "1", "4", "2"), ids(NoteOrganizer.organize(notes, NoteSort.TITLE, NoteFilter())))
    }

    @Test
    fun pinnedStaysFirstInEverySort() {
        val notes = listOf(note("a", "A"), note("z", "Z", pinned = true))
        assertEquals("z", NoteOrganizer.organize(notes, NoteSort.TITLE, NoteFilter()).first().id)
    }

    @Test
    fun folderFilters() {
        val notes = listOf(note("a", folder = "f1"), note("b", folder = "f2"), note("c"))
        assertEquals(listOf("a"), ids(NoteOrganizer.organize(notes, NoteSort.TITLE, NoteFilter(FolderFilter.InFolder("f1")))))
        assertEquals(listOf("c"), ids(NoteOrganizer.organize(notes, NoteSort.TITLE, NoteFilter(FolderFilter.Unfiled))))
        assertEquals(3, NoteOrganizer.organize(notes, NoteSort.TITLE, NoteFilter(FolderFilter.All)).size)
    }

    @Test
    fun tagAndFolderFiltersCombine() {
        val notes = listOf(
            note("a", folder = "f1", tags = listOf("work")),
            note("b", folder = "f1", tags = listOf("home")),
            note("c", folder = "f2", tags = listOf("work")),
        )
        val result = NoteOrganizer.organize(notes, NoteSort.TITLE, NoteFilter(FolderFilter.InFolder("f1"), "work"))
        assertEquals(listOf("a"), ids(result))
    }
}
