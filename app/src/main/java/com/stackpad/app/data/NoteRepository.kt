package com.stackpad.app.data

import com.stackpad.app.data.backup.BackupFile
import com.stackpad.app.data.backup.BackupFolder
import com.stackpad.app.data.backup.BackupNote
import com.stackpad.app.data.backup.RestoreSummary
import com.stackpad.app.data.db.FolderDao
import com.stackpad.app.data.db.FolderEntity
import com.stackpad.app.data.db.NoteDao
import com.stackpad.app.data.db.NoteEntity
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.Folder
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.data.model.mediaFileName
import com.stackpad.app.domain.backup.BackupRules
import com.stackpad.app.domain.backup.MergeDecision
import com.stackpad.app.domain.notes.FolderNames
import com.stackpad.app.domain.search.SearchQuery
import com.stackpad.app.domain.notes.TagCount
import com.stackpad.app.domain.notes.Tags
import com.stackpad.app.domain.notes.TrashPolicy
import com.stackpad.app.domain.reminders.ReminderPlanner
import com.stackpad.app.domain.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.ZoneId

/** Single entry point for reading and writing notes, folders and their media files. */
class NoteRepository(
    private val dao: NoteDao,
    private val folderDao: FolderDao,
    private val media: MediaStorage,
    private val reminders: ReminderScheduler,
    private val scope: CoroutineScope,
) {
    private val writeLock = Mutex()

    private companion object {
        /** Fresh files may belong to a note that is still being edited, so they are never swept. */
        const val ORPHAN_MIN_AGE_MS = 10 * 60 * 1000L

        /** SQLite allows only so many variables per statement, so id lists are split. */
        const val SQL_CHUNK = 500
    }

    // region Reading

    /** Notes outside the trash that match [query]. Filtering and sorting happen in the caller. */
    fun observeSummaries(query: String): Flow<List<NoteSummary>> {
        val words = SearchQuery.words(query)
        return dao.observeActive("").map { list ->
            if (words.isEmpty()) {
                list.map { it.toSummary() }
            } else {
                // Every word must match somewhere; the preview becomes a snippet around the first hit.
                list
                    .filter { SearchQuery.matches(words, it.title, it.searchText, Tags.parse(it.tags)) }
                    .map { it.toSummary().copy(preview = SearchQuery.snippet(it.searchText, words)) }
            }
        }
    }

    fun observeTrashed(): Flow<List<NoteSummary>> =
        dao.observeTrashed().map { list -> list.map { it.toSummary() } }

    fun observeFolders(): Flow<List<Folder>> =
        folderDao.observeAll().map { list -> list.map { it.toFolder() } }

    /** Every tag in use (outside the trash) with its note count. */
    fun observeTagCounts(): Flow<List<TagCount>> =
        dao.observeTagStrings().map { rows -> Tags.count(rows.map(Tags::parse)) }

    suspend fun load(id: String): Note? = dao.get(id)?.toNote()

    /** The most recently edited note outside the trash. Waits for pending saves so it is current. */
    suspend fun latestSummary(): NoteSummary? = writeLock.withLock { dao.latestActive()?.toSummary() }

    // endregion

    // region Writing notes

    suspend fun save(note: Note) {
        writeLock.withLock {
            val previous = dao.get(note.id)?.toNote()
            dao.upsert(note.toEntity(updatedAt = System.currentTimeMillis()))
            val changes = ReminderPlanner.reconcile(previous, note, System.currentTimeMillis(), ZoneId.systemDefault())
            changes.cancel.forEach(reminders::cancel)
            changes.schedule.forEach(reminders::schedule)
        }
    }

    /** Saves on the app-wide scope so the write survives the caller being destroyed. */
    fun saveAsync(note: Note) {
        scope.launch { save(note) }
    }

    suspend fun setPinned(id: String, pinned: Boolean) {
        writeLock.withLock { dao.setPinned(id, pinned) }
    }

    /** Pins or unpins several notes at once. */
    suspend fun setPinned(ids: Collection<String>, pinned: Boolean) {
        writeLock.withLock { ids.chunked(SQL_CHUNK).forEach { dao.setPinnedIn(it, pinned) } }
    }

    /** Files several notes in [folderId] (null = no folder). Their edit time is not changed. */
    suspend fun setFolder(ids: Collection<String>, folderId: String?) {
        writeLock.withLock { ids.chunked(SQL_CHUNK).forEach { dao.setFolderIn(it, folderId) } }
    }

    /** Adds a tag to several notes (ignored where it is invalid, a duplicate, or the note has too many). */
    suspend fun addTag(ids: Collection<String>, rawTag: String) {
        writeLock.withLock {
            ids.forEach { id ->
                val entity = dao.get(id) ?: return@forEach
                val current = Tags.parse(entity.tags)
                val updated = Tags.add(current, rawTag)
                if (updated != current) dao.setTags(id, Tags.serialize(updated))
            }
        }
    }

    /** Moves several notes to the trash. */
    suspend fun moveToTrash(ids: Collection<String>) {
        ids.forEach { moveToTrash(it) }
    }

    /** Restores several notes from the trash. */
    suspend fun restore(ids: Collection<String>) {
        ids.forEach { restore(it) }
    }

    // endregion

    // region Trash

    /** Saves [note] (latest edits included) and moves it to the trash. Its media is kept. */
    suspend fun saveAndTrash(note: Note) {
        writeLock.withLock {
            val now = System.currentTimeMillis()
            dao.upsert(note.copy(trashedAt = now).toEntity(updatedAt = now))
            ReminderPlanner.dueKeys(note).forEach(reminders::cancel)
        }
    }

    suspend fun moveToTrash(id: String) {
        writeLock.withLock {
            dao.setTrashedAt(id, System.currentTimeMillis())
            dao.get(id)?.toNote()?.let { ReminderPlanner.dueKeys(it).forEach(reminders::cancel) }
        }
    }

    suspend fun restore(id: String) {
        writeLock.withLock {
            dao.setTrashedAt(id, null)
            dao.get(id)?.toNote()?.let(::scheduleReminders)
        }
    }

    /** Re-creates every pending reminder, e.g. after a reboot or an app update cleared the alarms. */
    suspend fun rescheduleAllReminders() {
        writeLock.withLock { dao.allActive().forEach { scheduleReminders(it.toNote()) } }
    }

    private fun scheduleReminders(note: Note) {
        ReminderPlanner.plan(note, System.currentTimeMillis(), ZoneId.systemDefault()).forEach(reminders::schedule)
    }

    /** Permanently deletes the note and every image/audio file it owns. */
    suspend fun deleteForever(id: String) {
        writeLock.withLock { deleteNotes(listOfNotNull(dao.get(id))) }
    }

    suspend fun emptyTrash() {
        writeLock.withLock { deleteNotes(dao.allTrashed()) }
    }

    /** Erases notes that have been in the trash longer than the retention period. */
    suspend fun purgeExpiredTrash(now: Long = System.currentTimeMillis()) {
        writeLock.withLock { deleteNotes(dao.trashedAtOrBefore(TrashPolicy.cutoff(now))) }
    }

    private suspend fun deleteNotes(notes: List<NoteEntity>) {
        notes.forEach { entity ->
            val note = entity.toNote()
            ReminderPlanner.dueKeys(note).forEach(reminders::cancel)
            note.blocks.forEach { block -> block.mediaFileName()?.let(media::delete) }
            dao.delete(entity.id)
        }
    }

    /** Removes media files that no note refers to (left behind by undo history or a crash). */
    suspend fun sweepOrphanMedia() {
        writeLock.withLock {
            val referenced = dao.allBlocksJson()
                .flatMap { BlockJsonCodec.decode(it) }
                .mapNotNull { it.mediaFileName() }
                .toSet()
            media.listNames(minAgeMs = ORPHAN_MIN_AGE_MS).filterNot { it in referenced }.forEach(media::delete)
        }
    }

    // endregion

    // region Folders

    /** Creates a folder. Returns null when [name] has no usable characters. */
    suspend fun createFolder(name: String): Folder? {
        val clean = FolderNames.normalize(name) ?: return null
        val folder = FolderEntity(BlockFactory.newId(), clean, System.currentTimeMillis())
        folderDao.upsert(folder)
        return folder.toFolder()
    }

    suspend fun renameFolder(id: String, name: String) {
        val clean = FolderNames.normalize(name) ?: return
        val existing = folderDao.get(id) ?: return
        folderDao.upsert(existing.copy(name = clean))
    }

    /** Deletes the folder; its notes stay, and become unfiled. */
    suspend fun deleteFolder(id: String) {
        writeLock.withLock {
            dao.clearFolder(id)
            folderDao.delete(id)
        }
    }

    // endregion

    // region Backup

    /** Every note (trash included) and folder, as written to a backup. */
    suspend fun snapshotForBackup(appVersion: String): BackupFile = writeLock.withLock {
        BackupFile(
            formatVersion = BackupRules.FORMAT_VERSION,
            appVersion = appVersion,
            createdAt = System.currentTimeMillis(),
            folders = folderDao.getAll().map { BackupFolder(it.id, it.name, it.createdAt) },
            notes = (dao.allActive() + dao.allTrashed()).map { it.toBackupNote() },
        )
    }

    /**
     * Merges [backup] into the database. [installMedia] is asked to bring in a media file by name
     * (returns false when the backup did not contain it); it is only called for notes that are
     * actually added or replaced. Existing notes are never replaced by older copies.
     */
    suspend fun mergeBackup(backup: BackupFile, installMedia: (String) -> Unit): RestoreSummary =
        writeLock.withLock {
            val knownFolders = folderDao.getAll().map { it.id }.toMutableSet()
            backup.folders.forEach { folder ->
                val name = FolderNames.normalize(folder.name)
                if (name != null && knownFolders.add(folder.id)) {
                    folderDao.upsert(FolderEntity(folder.id, name, folder.createdAt))
                }
            }

            var added = 0
            var updated = 0
            var skipped = 0
            for (incoming in backup.notes) {
                val decision = BackupRules.decide(dao.get(incoming.id)?.updatedAt, incoming.updatedAt)
                if (decision == MergeDecision.SKIP) {
                    skipped++
                    continue
                }
                val note = incoming.toNote(folderKnown = incoming.folderId in knownFolders)
                note.blocks.mapNotNull { it.mediaFileName() }.distinct().forEach(installMedia)
                dao.upsert(note.toEntity(updatedAt = incoming.updatedAt))
                if (decision == MergeDecision.ADD) added++ else updated++
            }

            dao.allActive().forEach { scheduleReminders(it.toNote()) }
            RestoreSummary(added, updated, skipped)
        }

    // endregion
}
