package com.stackpad.app.data.share

import android.net.Uri
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.Note
import com.stackpad.app.domain.share.SharedContentPlanner
import com.stackpad.app.domain.share.SharedContentPlanner.Part

/** Saves content shared into the app (text, links, pictures) as a new note. */
class SharedNoteCreator(
    private val repository: NoteRepository,
    private val media: MediaStorage,
) {
    /** Returns the new note's id, or null when there was nothing usable to save. */
    suspend fun create(subject: String?, text: String?, pictures: List<Uri>): String? {
        val plan = SharedContentPlanner.plan(subject, text)
        val blocks = ArrayList<Block>()
        for (part in plan.parts) {
            blocks += when (part) {
                is Part.Text -> BlockFactory.text().copy(text = part.text)
                is Part.Link -> LinkBlock(id = BlockFactory.newId(), url = part.url)
            }
        }
        for (uri in pictures) {
            media.importImage(uri)?.let { blocks += BlockFactory.image(it) }
        }
        if (blocks.isEmpty() && plan.title.isBlank()) return null
        if (blocks.lastOrNull() !is com.stackpad.app.data.model.TextBlock) blocks += BlockFactory.text()

        val now = System.currentTimeMillis()
        val note = Note(
            id = BlockFactory.newId(),
            title = plan.title,
            blocks = blocks,
            pinned = false,
            createdAt = now,
            updatedAt = now,
        )
        repository.save(note)
        return note.id
    }
}
