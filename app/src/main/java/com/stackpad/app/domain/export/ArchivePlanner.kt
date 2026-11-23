package com.stackpad.app.domain.export

import com.stackpad.app.data.model.Note

/** Decides which notes go into a "all notes as Markdown" archive and what each file is called. */
object ArchivePlanner {
    /** [mediaPrefix] is the relative path from the note's file to the shared `media/` folder. */
    data class Item(val note: Note, val path: String, val mediaPrefix: String)

    data class Plan(val items: List<Item>, val skippedLocked: Int)

    private const val MAX_NAME = 80
    private val forbidden = Regex("[\\\\/:*?\"<>|\\u0000-\\u001F]")

    /** Trashed and locked notes are left out (a locked note's text would be readable in the archive). */
    fun plan(notes: List<Note>, folderNames: Map<String, String>): Plan {
        val active = notes.filter { it.trashedAt == null }
        val exportable = active.filterNot { it.locked }.sortedWith(compareBy({ it.createdAt }, { it.id }))
        val taken = HashSet<String>()
        val items = exportable.map { note ->
            val folder = note.folderId?.let { folderNames[it] }?.let { safeName(it, "Folder") }
            val base = safeName(note.title, "Untitled")
            var name = base
            var n = 2
            while (!taken.add((folder.orEmpty() + "/" + name).lowercase())) name = "$base (${n++})"
            val path = if (folder == null) "$name.md" else "$folder/$name.md"
            Item(note, path, if (folder == null) "media/" else "../media/")
        }
        return Plan(items, active.count { it.locked })
    }

    /** A file or folder name that is safe on every system; [fallback] when nothing usable is left. */
    fun safeName(raw: String, fallback: String): String {
        val cleaned = raw.replace(forbidden, " ").replace(Regex("\\s+"), " ").trim().trim('.').trim()
        return cleaned.take(MAX_NAME).trim().trimEnd('.').ifEmpty { fallback }
    }
}
