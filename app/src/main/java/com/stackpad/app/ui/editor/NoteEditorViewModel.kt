package com.stackpad.app.ui.editor

import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stackpad.app.AppContainer
import com.stackpad.app.audio.Waveform
import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.NoteVersion
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLook
import com.stackpad.app.data.model.withLook
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.data.model.isBlank
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.mediaFileName
import com.stackpad.app.data.model.mediaFileNames
import com.stackpad.app.domain.image.ImageEdit
import com.stackpad.app.domain.share.ShareOptions
import com.stackpad.app.domain.blocks.BlockOrder
import com.stackpad.app.domain.export.ExportFormat
import com.stackpad.app.domain.export.NoteExporter
import com.stackpad.app.domain.history.UndoHistory
import com.stackpad.app.domain.notes.Tags
import com.stackpad.app.domain.text.SpanEditing
import com.stackpad.app.domain.text.HeadingSpans
import com.stackpad.app.domain.text.TextRuns
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Holds and autosaves one note while it is open in the editor, and owns audio recording.
 * A brand-new note is only written to the database once it has content.
 */
class NoteEditorViewModel(
    private val container: AppContainer,
    val noteId: String,
    /** Folder a brand-new note starts in (the one selected in the note list); ignored for existing notes. */
    private val initialFolderId: String? = null,
) : ViewModel() {

    private val repository = container.repository
    private val media = container.mediaStorage
    private val player = container.audioPlayer
    private val recorder = container.createAudioRecorder()

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private val _activeText = MutableStateFlow<ActiveText?>(null)
    val activeText: StateFlow<ActiveText?> = _activeText.asStateFlow()

    /**
     * Styles chosen from the toolbar for the next typed text when nothing is selected.
     * Null means "continue the style of the character before the caret".
     */
    private val _typingStyles = MutableStateFlow<Set<SpanType>?>(null)
    val typingStyles: StateFlow<Set<SpanType>?> = _typingStyles.asStateFlow()

    val knownTags: StateFlow<List<String>> = repository.observeTagCounts()
        .map { counts -> counts.map { it.tag } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /** The saved options for sharing as an image or PDF, and whether their sheet opens before every share. */
    val shareOptions: StateFlow<ShareOptions> = container.sharePreferences.options
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ShareOptions())
    val askBeforeSharing: StateFlow<Boolean> = container.sharePreferences.askEachTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    fun saveShareOptions(options: ShareOptions, askEachTime: Boolean) {
        viewModelScope.launch { container.sharePreferences.save(options, askEachTime) }
    }

    private val history = UndoHistory<EditorSnapshot>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _recording = MutableStateFlow<RecordingUiState?>(null)
    val recording: StateFlow<RecordingUiState?> = _recording.asStateFlow()

    private val _messages = MutableSharedFlow<EditorMessage>(extraBufferCapacity = 8)
    val messages: SharedFlow<EditorMessage> = _messages.asSharedFlow()

    private val _undoOffers = MutableSharedFlow<Int>(extraBufferCapacity = 4)
    val undoOffers: SharedFlow<Int> = _undoOffers.asSharedFlow()

    private var createdAt = System.currentTimeMillis()
    private var existsInDb = false
    private var dirty = false
    private var discarded = false
    private var saveJob: Job? = null

    private var lastFocusedId: String? = null
    private var pendingCameraName: String? = null

    private var recordingName: String? = null
    private var recordingJob: Job? = null
    private var recordingStartedAt = 0L
    private val samples = ArrayList<Int>()

    init {
        viewModelScope.launch {
            val note = repository.load(noteId)
            if (note != null) {
                existsInDb = true
                createdAt = note.createdAt
                _state.value = EditorUiState(
                    loaded = true,
                    title = note.title,
                    blocks = TextRuns.normalize(note.blocks).blocks.ifEmpty { listOf(BlockFactory.text()) },
                    pinned = note.pinned,
                    folderId = note.folderId,
                    tags = note.tags,
                    color = note.color,
                    locked = note.locked,
                    createdAt = note.createdAt,
                    updatedAt = note.updatedAt,
                    archived = note.archived,
                )
            } else {
                _state.value = EditorUiState(
                    loaded = true,
                    blocks = listOf(BlockFactory.text()),
                    folderId = initialFolderId,
                    createdAt = createdAt,
                )
            }
        }
    }

    // region Editing

    fun setTitle(title: String) = mutate(coalesceKey = "title") { it.copy(title = title) }

    fun togglePin() = mutate(record = false) { it.copy(pinned = !it.pinned) }

    // region Organizing (not part of undo)

    fun setLocked(locked: Boolean) = mutate(record = false) { it.copy(locked = locked) }

    fun setColor(color: Int) = mutate(record = false) { it.copy(color = color) }

    fun addTag(raw: String) = mutate(record = false) { it.copy(tags = Tags.add(it.tags, raw)) }

    fun removeTag(tag: String) = mutate(record = false) { it.copy(tags = it.tags - tag) }

    // endregion

    // region Undo and redo

    fun undo() {
        val current = _state.value
        if (!current.loaded) return
        history.undo(current.toSnapshot())?.let(::restore)
    }

    fun redo() {
        val current = _state.value
        if (!current.loaded) return
        history.redo(current.toSnapshot())?.let(::restore)
    }

    private fun restore(snapshot: EditorSnapshot) {
        _state.update {
            it.copy(title = snapshot.title, blocks = snapshot.blocks, focusBlockId = null, focusCaret = null)
        }
        _typingStyles.value = null
        if (snapshot.blocks.none { it.id == _activeText.value?.blockId }) _activeText.value = null
        if (snapshot.blocks.none { it.id == lastFocusedId }) lastFocusedId = null
        markChanged()
        publishHistory()
    }

    private fun publishHistory() {
        _canUndo.value = history.canUndo
        _canRedo.value = history.canRedo
    }

    // endregion

    fun updateBlock(id: String, transform: (Block) -> Block) = mutate(coalesceKey = "block:$id") { s ->
        s.copy(blocks = s.blocks.map { if (it.id == id) transform(it) else it })
    }

    /** Edits the block [id] if it is a [T]; other types are left untouched. */
    inline fun <reified T : Block> edit(id: String, crossinline transform: (T) -> T) =
        updateBlock(id) { block -> if (block is T) transform(block) else block }

    /** Like [edit], but always its own undo step (never merged with typing). */
    inline fun <reified T : Block> editStep(id: String, crossinline transform: (T) -> T) =
        replaceBlock(id) { block -> if (block is T) transform(block) else block }

    fun replaceBlock(id: String, transform: (Block) -> Block) = mutate { s ->
        s.copy(blocks = s.blocks.map { if (it.id == id) transform(it) else it })
    }

    /** Asks the screen to show [messageRes] with an Undo button. */
    fun offerUndo(@androidx.annotation.StringRes messageRes: Int) {
        _undoOffers.tryEmit(messageRes)
    }

    /**
     * Inserts [block] after the block that was last focused (or at the end). An empty text block
     * at that spot is replaced instead. A text block always stays at the very end.
     */
    fun addBlock(block: Block) {
        val anchor = lastFocusedId
        val caret = _activeText.value?.takeIf { it.blockId == anchor }
        mutate { s ->
            val list = s.blocks.toMutableList()
            val anchorIndex = list.indexOfFirst { it.id == anchor }
            val caretBlock = if (caret != null && anchorIndex >= 0) list[anchorIndex] as? TextBlock else null
            if (caret != null && caretBlock != null) {
                // Inserting in the middle of a run of text splits it: text before, the block, text after.
                val (before, after) = TextRuns.splitAround(caretBlock, caret.start)
                list.removeAt(anchorIndex)
                list.addAll(anchorIndex, listOfNotNull(before, block, after))
            } else {
                val target = list.getOrNull(if (anchorIndex >= 0) anchorIndex else list.lastIndex)
                if (target is TextBlock && target.isBlank()) {
                    list[list.indexOf(target)] = block
                } else {
                    list.add(if (anchorIndex >= 0) anchorIndex + 1 else list.size, block)
                }
            }
            if (list.last() !is TextBlock) list.add(BlockFactory.text())
            s.copy(blocks = list, focusBlockId = block.id, focusCaret = null)
        }
        lastFocusedId = block.id
    }

    /**
     * Tapping the space below a block: puts the cursor in the text block that follows it, or adds a
     * blank text block there when the next block is not text. A text block is just focused.
     */
    fun addTextBelow(id: String) {
        val blocks = _state.value.blocks
        val index = blocks.indexOfFirst { it.id == id }
        if (index < 0) return
        val target = blocks[index].takeIf { it is TextBlock } ?: blocks.getOrNull(index + 1)?.takeIf { it is TextBlock }
        if (target != null) {
            mutate(record = false) { it.copy(focusBlockId = target.id, focusCaret = null) }
            lastFocusedId = target.id
            return
        }
        val fresh = BlockFactory.text()
        mutate { s ->
            val list = s.blocks.toMutableList()
            list.add(index + 1, fresh)
            s.copy(blocks = list, focusBlockId = fresh.id, focusCaret = null)
        }
        lastFocusedId = fresh.id
    }

    /**
     * Removes a block and deletes its media file. With [viaBackspace] (empty text block), the
     * trailing text block is kept when it is the only thing after a non-text block, and focus
     * moves to the previous block.
     */
    fun removeBlock(id: String, viaBackspace: Boolean = false) {
        val blocks = _state.value.blocks
        val index = blocks.indexOfFirst { it.id == id }
        if (index < 0) return
        if (viaBackspace && index == blocks.lastIndex && blocks.getOrNull(index - 1) !is TextBlock) return

        // The file stays on disk so undo can bring the block back; it is cleaned up on exit.
        blocks[index].mediaFileName()?.let(player::stopIfPlaying)
        mutate { s ->
            val remaining = s.blocks.filterNot { it.id == id }.ifEmpty { listOf(BlockFactory.text()) }
            val previousId = remaining.getOrNull((index - 1).coerceAtLeast(0))?.id
            s.copy(blocks = remaining, focusBlockId = if (viaBackspace) previousId else null, focusCaret = null)
        }
        if (lastFocusedId == id) lastFocusedId = null
        if (_activeText.value?.blockId == id) _activeText.value = null
    }

    /** Earlier versions of this note, newest first, without the one that equals what is on screen. */
    suspend fun loadVersions(): List<NoteVersion> {
        val s = _state.value
        return repository.versions(noteId).filterNot { it.title == s.title && it.blocks == s.blocks }
    }

    /** Puts an earlier version into the editor. The current text is kept as a version first, and Undo brings it back. */
    fun restoreVersion(version: NoteVersion) {
        viewModelScope.launch {
            saveJob?.cancel()
            snapshot()?.let {
                repository.save(it)
                existsInDb = true
                dirty = false
            }
            repository.keepVersionNow(noteId)
            mutate { s ->
                s.copy(
                    title = version.title,
                    blocks = version.blocks.ifEmpty { listOf(BlockFactory.text()) },
                    focusBlockId = null,
                    focusCaret = null,
                )
            }
        }
    }

    fun consumeFocus(id: String) {
        _state.update { if (it.focusBlockId == id) it.copy(focusBlockId = null, focusCaret = null) else it }
    }

    // endregion

    // region Formatting

    fun onBlockFocused(id: String) {
        lastFocusedId = id
        if (_activeText.value?.blockId != id) _activeText.value = null
    }

    fun onTextSelection(id: String, start: Int, end: Int) {
        lastFocusedId = id
        val next = ActiveText(id, minOf(start, end), maxOf(start, end))
        if (_activeText.value != next) {
            // Moving the caret discards any style picked for the next typed text.
            _typingStyles.value = null
            _activeText.value = next
        }
    }

    /**
     * Bold/italic/underline. With a selection, toggles the style on the selected text.
     * With only a caret, toggles the style for the text typed next.
     */
    fun toggleSpan(type: SpanType) {
        val active = _activeText.value ?: return
        if (active.start != active.end) {
            edit<TextBlock>(active.blockId) {
                it.copy(spans = SpanEditing.toggle(it.spans, active.start, active.end, type))
            }
            return
        }
        val block = _state.value.blocks.firstOrNull { it.id == active.blockId } as? TextBlock ?: return
        val current = _typingStyles.value ?: SpanEditing.stylesAt(block.spans, active.start - 1)
        _typingStyles.value = if (type in current) current - type else current + type
    }

    /**
     * Handles typing in a text block. Newly typed text gets the current typing styles; a heading
     * on a line grows with what is typed on that line. Enter just adds a line to the same block.
     */
    fun onTextChanged(id: String, newText: String, caret: Int) {
        val typing = _typingStyles.value
        _typingStyles.value = null
        val old = _state.value.blocks.firstOrNull { it.id == id } as? TextBlock
        val lineBreakEdit = old != null && old.text.count { it == '\n' } != newText.count { it == '\n' }
        mutate(coalesceKey = if (lineBreakEdit) null else "text:$id") { s ->
            val index = s.blocks.indexOfFirst { it.id == id }
            val block = s.blocks.getOrNull(index) as? TextBlock ?: return@mutate s
            val (headings, others) = block.spans.partition { HeadingSpans.isHeading(it.type) }
            val styled = SpanEditing.applyEdit(others, block.text, newText, typing?.filterNot(HeadingSpans::isHeading)?.toSet())
            val grown = SpanEditing.adjustForEdit(headings, block.text, newText).toMutableList()
            typing?.firstOrNull(HeadingSpans::isHeading)?.let { type ->
                val change = SpanEditing.diff(block.text, newText)
                if (change.newEnd > change.prefix) grown += TextSpan(change.prefix, change.newEnd, type)
            }
            val spans = HeadingSpans.normalize(newText, styled + grown)
            val list = s.blocks.toMutableList()
            list[index] = block.copy(text = newText, spans = spans)
            s.copy(blocks = list)
        }
    }

    /**
     * Backspace with the caret at the very start of a text block: an empty block is removed,
     * a non-empty one is merged into the text block above it.
     */
    fun onBackspaceAtStart(id: String) {
        val blocks = _state.value.blocks
        val index = blocks.indexOfFirst { it.id == id }
        val block = blocks.getOrNull(index) as? TextBlock ?: return
        if (block.text.isEmpty()) {
            removeBlock(id, viaBackspace = true)
            return
        }
        val previous = blocks.getOrNull(index - 1) as? TextBlock ?: return
        mutate { s ->
            val at = s.blocks.indexOfFirst { it.id == id }
            val current = s.blocks.getOrNull(at) as? TextBlock ?: return@mutate s
            val above = s.blocks.getOrNull(at - 1) as? TextBlock ?: return@mutate s
            val offset = above.text.length
            val merged = above.copy(
                text = above.text + current.text,
                spans = above.spans + current.spans.map { it.copy(start = it.start + offset, end = it.end + offset) },
            )
            val list = s.blocks.toMutableList()
            list[at - 1] = merged
            list.removeAt(at)
            s.copy(blocks = list, focusBlockId = merged.id, focusCaret = offset)
        }
        lastFocusedId = previous.id
    }

    /**
     * Toggles a heading on the lines the selection touches (tapping the level they all have turns it
     * off). On an empty line the heading is picked for the text typed next.
     */
    /** Changes the font, size, highlight and line spacing of the text block [blockId]. */
    fun setTextLook(blockId: String, look: TextLook) {
        edit<TextBlock>(blockId) { it.withLook(look) }
    }

    fun toggleLevel(level: TextLevel) {
        val active = _activeText.value ?: return
        val block = _state.value.blocks.firstOrNull { it.id == active.blockId } as? TextBlock ?: return
        val toggled = HeadingSpans.toggleLevel(block.text, block.spans, active.start, active.end, level)
        if (toggled != block.spans) {
            edit<TextBlock>(active.blockId) { it.copy(spans = toggled) }
            return
        }
        val type = HeadingSpans.typeOf(level) ?: return
        val current = _typingStyles.value ?: SpanEditing.stylesAt(block.spans, active.start - 1)
        val withoutHeading = current.filterNot(HeadingSpans::isHeading).toSet()
        _typingStyles.value = if (type in current) withoutHeading else withoutHeading + type
    }

    // endregion

    // region Images

    /**
     * Saves [change] as a new picture file for the image block [id] and points the block at it.
     * Every edit starts from the untouched picture (kept after the first edit), so it can be re-cropped or restored. Returns false (and shows a message) on failure.
     */
    suspend fun saveImageEdit(id: String, change: ImageEdit): Boolean {
        val block = _state.value.blocks.firstOrNull { it.id == id } as? ImageBlock ?: return false
        val saved = container.imageEditSaver.save(block.originalFileName ?: block.fileName, change)
        if (saved == null) {
            emit(EditorMessage.IMAGE_EDIT_FAILED)
            return false
        }
        // The previous file stays until the editor closes, so undo can bring it back.
        replaceImageBlock(id) { it.copy(fileName = saved, originalFileName = it.originalFileName ?: it.fileName) }
        return true
    }

    /** Reads the text in the picture of image block [id] and adds it as text right below the picture. */
    fun readPictureText(id: String) {
        val block = _state.value.blocks.firstOrNull { it.id == id } as? ImageBlock ?: return
        viewModelScope.launch {
            emit(EditorMessage.PICTURE_TEXT_WORKING)
            val text = container.pictureTextReader.read(media.file(block.fileName))
            when {
                text == null -> emit(EditorMessage.PICTURE_TEXT_FAILED)
                text.isBlank() -> emit(EditorMessage.PICTURE_TEXT_NONE)
                else -> {
                    mutate { s ->
                        val index = s.blocks.indexOfFirst { it.id == id }
                        if (index < 0) return@mutate s
                        val list = s.blocks.toMutableList()
                        list.add(index + 1, BlockFactory.text().copy(text = text.trim()))
                        s.copy(blocks = list)
                    }
                    emit(EditorMessage.PICTURE_TEXT_ADDED)
                }
            }
        }
    }

    /** Puts the untouched picture back in the image block [id]. */
    fun restoreOriginalImage(id: String) {
        val block = _state.value.blocks.firstOrNull { it.id == id } as? ImageBlock ?: return
        val original = block.originalFileName
        if (original == null || !media.exists(original)) {
            emit(EditorMessage.IMAGE_EDIT_FAILED)
            return
        }
        replaceImageBlock(id) { it.copy(fileName = original, originalFileName = null) }
    }

    /** Changes an image block as its own undo step (not merged with other edits). */
    private fun replaceImageBlock(id: String, transform: (ImageBlock) -> ImageBlock) = mutate { s ->
        s.copy(blocks = s.blocks.map { if (it.id == id && it is ImageBlock) transform(it) else it })
    }

    fun importImage(source: Uri) {
        viewModelScope.launch {
            val name = media.importImage(source)
            if (name == null) emit(EditorMessage.IMAGE_FAILED) else addBlock(BlockFactory.image(name))
        }
    }

    /** Reserves a file for the camera app and returns the Uri it should write to. */
    fun prepareCameraUri(): Uri {
        val target = media.createCameraTarget()
        pendingCameraName = target.fileName
        return target.uri
    }

    fun onCameraResult(success: Boolean) {
        val name = pendingCameraName ?: return
        pendingCameraName = null
        when {
            success && media.exists(name) -> addBlock(BlockFactory.image(name))
            success -> {
                media.delete(name)
                emit(EditorMessage.IMAGE_FAILED)
            }
            else -> media.delete(name)
        }
    }

    fun reportCameraUnavailable() {
        pendingCameraName?.let(media::delete)
        pendingCameraName = null
        emit(EditorMessage.CAMERA_UNAVAILABLE)
    }

    // endregion

    // region Audio

    fun togglePlayback(block: AudioBlock) {
        if (!player.toggle(block.fileName, media.file(block.fileName))) {
            emit(EditorMessage.AUDIO_UNAVAILABLE)
        }
    }

    /** Moves the playhead to [fraction] (0..1) of the note, starting playback if it was idle. */
    fun seekAudio(block: AudioBlock, fraction: Float) {
        if (!player.seekTo(block.fileName, media.file(block.fileName), fraction)) {
            emit(EditorMessage.AUDIO_UNAVAILABLE)
        }
    }

    fun cycleAudioSpeed() = player.cycleSpeed()

    fun isAudioAvailable(block: AudioBlock): Boolean = media.exists(block.fileName)

    /** Starts recording. The caller must already hold the microphone permission. */
    fun startRecording() {
        if (recordingName != null) return
        player.stop()
        val name = media.newAudioName()
        if (!recorder.start(media.file(name))) {
            media.delete(name)
            emit(EditorMessage.RECORD_FAILED)
            return
        }
        recordingName = name
        samples.clear()
        recordingStartedAt = SystemClock.elapsedRealtime()
        _recording.value = RecordingUiState()
        recordingJob = viewModelScope.launch {
            while (isActive) {
                delay(SAMPLE_INTERVAL_MS)
                samples.add(Waveform.normalize(recorder.maxAmplitude()))
                val elapsed = SystemClock.elapsedRealtime() - recordingStartedAt
                _recording.value = RecordingUiState(elapsed, samples.takeLast(LIVE_BARS))
                if (elapsed >= MAX_RECORDING_MS) {
                    stopRecording()
                    break
                }
            }
        }
    }

    /** Stops recording and adds the result to the note as an audio block. */
    fun stopRecording() = finishRecording(save = true)

    fun cancelRecording() = finishRecording(save = false)

    private fun finishRecording(save: Boolean) {
        val name = recordingName ?: return
        recordingJob?.cancel()
        recordingJob = null
        val duration = SystemClock.elapsedRealtime() - recordingStartedAt
        val recorded = recorder.stop()
        recordingName = null
        _recording.value = null

        if (!save) {
            media.delete(name)
            return
        }
        if (!recorded || duration < MIN_RECORDING_MS) {
            media.delete(name)
            emit(EditorMessage.RECORDING_TOO_SHORT)
            return
        }
        addBlock(BlockFactory.audio(name, duration, Waveform.downsample(samples)))
    }

    // endregion

    // region Persistence

    /**
     * Moves the note to the trash (with its latest edits and media), then calls [onTrashed]
     * on the main thread. A note that was never saved is simply discarded.
     */
    fun trashNote(onTrashed: () -> Unit) {
        val note = snapshot()
        discarded = true
        saveJob?.cancel()
        player.stop()
        viewModelScope.launch {
            if (note != null) repository.saveAndTrash(note)
            onTrashed()
        }
    }

    /** Moves the selected blocks one step up or down (undoable). */
    fun moveBlocks(ids: Set<String>, up: Boolean) = mutate { s ->
        s.copy(blocks = if (up) BlockOrder.moveUp(s.blocks, ids) else BlockOrder.moveDown(s.blocks, ids))
    }

    /** Which directions the selected blocks can still move, for enabling the buttons. */
    fun canMove(ids: Set<String>, up: Boolean): Boolean {
        val blocks = _state.value.blocks
        return if (up) BlockOrder.canMoveUp(blocks, ids) else BlockOrder.canMoveDown(blocks, ids)
    }

    /** Locks or unlocks the note. The caller has already verified the user. */
    fun toggleLocked() {
        mutate(record = false) { it.copy(locked = !it.locked) }
        flush()
    }

    /** Moves the note to the archive (or back to the notes) with its latest edits, then calls [onDone]. */
    fun toggleArchived(onDone: () -> Unit) {
        mutate(record = false) { it.copy(archived = !it.archived) }
        flush()
        onDone()
    }

    /** Saves the note, then stores a copy of it. [onDone] gets false when there was nothing to copy. */
    fun duplicateNote(onDone: (Boolean) -> Unit) {
        val note = snapshot()
        viewModelScope.launch {
            if (note != null) repository.save(note)
            onDone(note != null && repository.duplicate(noteId) != null)
        }
    }

    /** The blocks with these ids (in note order) as plain text, or null when they hold nothing to copy. */
    fun exportBlocks(ids: Set<String>): String? {
        val chosen = _state.value.blocks.filter { it.id in ids }
        return NoteExporter.exportBlocks(chosen, ExportFormat.PLAIN).takeIf { it.isNotBlank() }
    }

    /** The note as shareable text in [format], or null when it has no content yet. */
    fun exportText(format: ExportFormat): String? {
        val note = snapshot() ?: return null
        val text = NoteExporter.export(note, format)
        return text.takeIf { it.isNotBlank() }
    }

    /** The open note as it would be saved now, or null while it is still empty. Used for image and PDF export. */
    fun exportNote(): Note? = snapshot()

    /** Writes pending changes immediately (on a scope that outlives this view model). */
    fun flush() {
        saveJob?.cancel()
        if (!dirty) return
        val note = snapshot() ?: return
        dirty = false
        existsInDb = true
        repository.saveAsync(note)
    }

    /**
     * Applies [change]. When it alters the title or blocks, the previous state is recorded for
     * undo ([record] = false skips that); changes sharing a [coalesceKey] within a second become
     * one undo step, so typing is undone word by word instead of letter by letter.
     */
    private fun mutate(
        coalesceKey: String? = null,
        record: Boolean = true,
        change: (EditorUiState) -> EditorUiState,
    ) {
        val before = _state.value
        if (!before.loaded) return
        val changed = change(before)
        val after = joinTextRuns(changed)
        if (after == before) return
        if (record && (after.title != before.title || after.blocks != before.blocks)) {
            history.record(before.toSnapshot(), coalesceKey, SystemClock.elapsedRealtime())
            publishHistory()
        }
        _state.value = after.copy(updatedAt = System.currentTimeMillis())
        markChanged()
    }

    /** Keeps each run of text in one block: neighbouring text blocks (e.g. after a table is removed) are joined. */
    private fun joinTextRuns(state: EditorUiState): EditorUiState {
        val joined = TextRuns.normalize(state.blocks)
        if (joined.moved.isEmpty() && joined.blocks == state.blocks) return state
        val focus = state.focusBlockId?.let { joined.moved[it] }
        lastFocusedId = lastFocusedId?.let { joined.moved[it]?.into ?: it }
        if (_activeText.value?.blockId?.let { it in joined.moved } == true) _activeText.value = null
        return state.copy(
            blocks = joined.blocks,
            focusBlockId = focus?.into ?: state.focusBlockId,
            focusCaret = if (focus != null) focus.offset + (state.focusCaret ?: 0) else state.focusCaret,
        )
    }

    private fun markChanged() {
        dirty = true
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(AUTOSAVE_DELAY_MS)
            val note = snapshot() ?: return@launch
            dirty = false
            repository.save(note)
            existsInDb = true
        }
    }

    /** The note to persist, or null when there is nothing worth saving. */
    private fun snapshot(): Note? {
        val s = _state.value
        if (!s.loaded || discarded) return null
        if (!existsInDb && s.title.isBlank() && s.blocks.all { it.isBlank() }) return null
        return Note(
            id = noteId,
            title = s.title,
            blocks = s.blocks,
            pinned = s.pinned,
            folderId = s.folderId,
            tags = s.tags,
            color = s.color,
            locked = s.locked,
            archived = s.archived,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis(),
        )
    }

    // endregion

    fun notify(message: EditorMessage) = emit(message)

    private fun emit(message: EditorMessage) {
        _messages.tryEmit(message)
    }

    override fun onCleared() {
        finishRecording(save = true)
        flush()
        player.stop()
        deleteUnusedMedia()
    }

    /** Deletes media of blocks that were removed (kept until now so undo could restore them). */
    private fun deleteUnusedMedia() {
        fun names(blocks: List<Block>) = blocks.flatMap { it.mediaFileNames() }.toSet()
        val inUse = names(_state.value.blocks)
        val everSeen = history.snapshots().flatMap { names(it.blocks) }.toSet()
        (everSeen - inUse).forEach(media::delete)
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val AUTOSAVE_DELAY_MS = 600L
        private const val SAMPLE_INTERVAL_MS = 80L
        private const val LIVE_BARS = 44
        private const val MIN_RECORDING_MS = 600L
        private const val MAX_RECORDING_MS = 10 * 60 * 1000L

        fun factory(container: AppContainer, noteId: String, folderId: String? = null) = viewModelFactory {
            initializer { NoteEditorViewModel(container, noteId, folderId) }
        }
    }
}
