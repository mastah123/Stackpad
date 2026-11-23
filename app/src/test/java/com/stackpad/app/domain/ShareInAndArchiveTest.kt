package com.stackpad.app.domain

import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.Note
import com.stackpad.app.domain.backup.AutoBackupRules
import com.stackpad.app.domain.export.ArchivePlanner
import com.stackpad.app.domain.export.ExportFormat
import com.stackpad.app.domain.export.NoteExporter
import com.stackpad.app.domain.share.SharedContentPlanner
import com.stackpad.app.domain.share.SharedContentPlanner.Part
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ShareInAndArchiveTest {
    private fun note(id: String, title: String, folder: String? = null, locked: Boolean = false, trashed: Long? = null, created: Long = 0) =
        Note(id, title, emptyList(), false, created, created, folderId = folder, trashedAt = trashed, locked = locked)

    @Test fun pageShareBecomesTitleAndLink() {
        val plan = SharedContentPlanner.plan(null, "Great article\nhttps://example.com/a")
        assertEquals("Great article", plan.title)
        assertEquals(listOf<Part>(Part.Link("https://example.com/a")), plan.parts)
    }

    @Test fun subjectIsTheTitleAndTextIsKept() {
        val plan = SharedContentPlanner.plan("From Chrome", "See this https://x.org now\n\nhttps://x.org")
        assertEquals("From Chrome", plan.title)
        assertEquals(listOf(Part.Text("See this https://x.org now"), Part.Link("https://x.org")), plan.parts)
    }

    @Test fun shortPlainTextKeepsBodyAndGetsTitle() {
        val plan = SharedContentPlanner.plan(null, "Buy milk")
        assertEquals("Buy milk", plan.title)
        assertEquals(listOf<Part>(Part.Text("Buy milk")), plan.parts)
    }

    @Test fun longTextTitleIsCutButBodyIsWhole() {
        val long = "word ".repeat(30).trim()
        val plan = SharedContentPlanner.plan(null, long)
        assertEquals(true, plan.title.isNotEmpty() && plan.title.length <= 60 && long.startsWith(plan.title))
        assertEquals(listOf<Part>(Part.Text(long)), plan.parts)
    }

    @Test fun emptyShareHasNoParts() {
        val plan = SharedContentPlanner.plan(null, null)
        assertEquals("", plan.title)
        assertEquals(emptyList<Part>(), plan.parts)
    }

    @Test fun archiveSkipsTrashedAndLockedAndCountsLocked() {
        val plan = ArchivePlanner.plan(
            listOf(note("1", "A"), note("2", "B", locked = true), note("3", "C", trashed = 5L)),
            emptyMap(),
        )
        assertEquals(listOf("A.md"), plan.items.map { it.path })
        assertEquals(1, plan.skippedLocked)
    }

    @Test fun archiveNamesAreSafeUniqueAndFoldered() {
        val plan = ArchivePlanner.plan(
            listOf(
                note("1", "Plan: a/b?", created = 1),
                note("2", "plan: a/b?", created = 2),
                note("3", "", folder = "f", created = 3),
            ),
            mapOf("f" to "Work"),
        )
        assertEquals(listOf("Plan a b.md", "plan a b (2).md", "Work/Untitled.md"), plan.items.map { it.path })
        assertEquals(listOf("media/", "media/", "../media/"), plan.items.map { it.mediaPrefix })
    }

    @Test fun safeNameFallsBackAndLimitsLength() {
        assertEquals("Untitled", ArchivePlanner.safeName(" ... ", "Untitled"))
        assertEquals(80, ArchivePlanner.safeName("x".repeat(200), "Untitled").length)
    }

    @Test fun markdownLinksPicturesOnlyWhenAPathIsGiven() {
        val n = Note("1", "T", listOf(BlockFactory.image("pic.jpg")), false, 0, 0)
        assertEquals("# T\n\n*[Image]*", NoteExporter.export(n, ExportFormat.MARKDOWN))
        assertEquals("# T\n\n![Image](media/pic.jpg)", NoteExporter.export(n, ExportFormat.MARKDOWN) { "media/${it.fileName}" })
        assertEquals("T\n\n[Image]", NoteExporter.export(n, ExportFormat.PLAIN) { "media/${it.fileName}" })
    }

    @Test fun autoBackupNamesAndRetention() {
        val d = LocalDate.of(2026, 11, 3)
        assertEquals("stackpad-auto-2026-11-03.zip", AutoBackupRules.fileName(d))
        assertEquals(d, AutoBackupRules.dateOf("stackpad-auto-2026-11-03.zip"))
        assertNull(AutoBackupRules.dateOf("stackpad-backup-2026-11-03.zip"))
        assertNull(AutoBackupRules.dateOf("stackpad-auto-2026-13-40.zip"))
        val names = (1..9).map { AutoBackupRules.fileName(LocalDate.of(2026, 11, it)) } + "notes.zip"
        assertEquals(
            listOf("stackpad-auto-2026-11-02.zip", "stackpad-auto-2026-11-01.zip"),
            AutoBackupRules.toDelete(names.shuffled(), keep = 7),
        )
        assertEquals(emptyList<String>(), AutoBackupRules.toDelete(names, keep = 20))
    }
}
