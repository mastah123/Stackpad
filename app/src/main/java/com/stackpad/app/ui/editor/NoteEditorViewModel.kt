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
import com.stackpad.app.data.model.Folder
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.isBlank
import com.stackpad.app.data.model.mediaFileName
import com.stackpad.app.domain.blocks.BlockOrder
import com.stackpad.app.domain.export.ExportFormat
import com.stackpad.app.domain.templates.BuiltInTemplates
import com.stackpad.app.domain.templates.NoteTemplate
import com.stackpad.app.domain.templates.TemplateRules
import com.stackpad.app.domain.export.NoteExporter
import com.stackpad.app.domain.history.UndoHistory
import com.stackpad.app.domain.notes.Tags
import com.stackpad.app.domain.text.SpanEditing
import com.stackpad.app.domain.text.TextSplitting
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
    private val noteId: String,
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

    /** Folders the note can be filed in. */
    val folders: StateFlow<List<Folder>> = repository.observeFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /** Tags used by other notes, offered as suggestions. */
    val knownTags: StateFlow<List<String>> = repository.observeTagCounts()
        .map { counts -> counts.map { it.tag } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /** Built-in templates followed by the ones the user saved. */
    val templates: StateFlow<List<NoteTemplate>> = container.templatePreferences.templates
        .map { saved -> BuiltInTemplates.all() + saved }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), BuiltInTemplates.all())

    private val history = UndoHistory<EditorSnapshot>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _recording = MutableStateFlow<RecordingUiState?>(null)
    val recording: StateFlow<RecordingUiState?> = _recording.asStateFlow()

    private val _messages = MutableSharedFlow<EditorMessage>(extraBufferCapacity = 8)
    val messages: SharedFlow<EditorMessage> = _messages.asSharedFlow()

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
                    blocks = TextSplitting.splitMultilineBlocks(note.blocks).ifEmpty { listOf(BlockFactory.text()) },
                    pinned = note.pinned,
                    folderId = note.folderId,
                    tags = note.tags,
                    color = note.color,
                    locked = note.locked,
                )
            } else {
                _state.value = EditorUiState(loaded = true, blocks = listOf(BlockFactory.text()))
            }
        }
    }

    // region Editing

    fun setTitle(title: String) = mutate(coalesceKey = "title") { it.copy(title = title) }

    fun togglePin() = mutate(record = false) { it.copy(pinned = !it.pinned) }

    // region Organizing (not part of undo)

    fun setFolder(folderId: String?) = mutate(record = false) { it.copy(folderId = folderId) }

    fun setLocked(locked: Boolean) = mutate(record = false) { it.copy(locked = locked) }

    fun setColor(color: Int) = mutate(record = false) { it.copy(color = color) }

    fun addTag(raw: String) = mutate(record = false) { it.copy(tags = Tags.add(it.tags, raw)) }

    fun removeTag(tag: String) = mutate(record = false) { it.copy(tags = it.tags - tag) }

    /** Creates a folder and files this note in it. */
    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name)?.let { setFolder(it.id) }
        }
    }

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

    /**
     * Inserts [block] after the block that was last focused (or at the end). An empty text block
     * at that spot is replaced instead. A text block always stays at the very end.
     */
    fun addBlock(block: Block) {
        val anchor = lastFocusedId
        mutate { s ->
            val list = s.blocks.toMutableList()
            val anchorIndex = list.indexOfFirst { it.id == anchor }
            val target = list.getOrNull(if (anchorIndex >= 0) anchorIndex else list.lastIndex)
            if (target is TextBlock && target.isBlank()) {
                list[list.indexOf(target)] = block
            } else {
                list.add(if (anchorIndex >= 0) anchorIndex + 1 else list.size, block)
            }
            if (list.last() !is TextBlock) list.add(BlockFactory.text())
            s.copy(blocks = list, focusBlockId = block.id, focusCaret = null)
        }
        lastFocusedId = block.id
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
     * Handles typing in a text block. Newly typed text gets the current typing styles, and
     * pressing Enter (or pasting several lines) splits the text into one block per line so
     * every line can have its own heading level.
     */
    fun onTextChanged(id: String, newText: String, caret: Int) {
        val typing = _typingStyles.value
        _typingStyles.value = null
        var focusId: String? = null
        mutate(coalesceKey = if ('\n' in newText) null else "text:$id") { s ->
            val index = s.blocks.indexOfFirst { it.id == id }
            val block = s.blocks.getOrNull(index) as? TextBlock ?: return@mutate s
            val spans = SpanEditing.applyEdit(block.spans, block.text, newText, typing)
            val list = s.blocks.toMutableList()
            if ('\n' !in newText) {
                list[index] = block.copy(text = newText, spans = spans)
                s.copy(blocks = list)
            } else {
                val split = TextSplitting.splitLines(newText, spans, caret)
                val lines = split.lines.mapIndexed { i, line ->
                    if (i == 0) {
                        block.copy(text = line.text, spans = line.spans)
                    } else {
                        TextBlock(BlockFactory.newId(), TextLevel.PARAGRAPH, line.text, line.spans)
                    }
                }
                list.removeAt(index)
                list.addAll(index, lines)
                focusId = lines[split.caretLine].id
                s.copy(blocks = list, focusBlockId = focusId, focusCaret = split.caretOffset)
            }
        }
        focusId?.let { lastFocusedId = it }
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

    /** Applies [level] to the active text block; tapping the current level reverts to a paragraph. */
    fun toggleLevel(level: TextLevel) {
        val active = _activeText.value ?: return
        edit<TextBlock>(active.blockId) {
            it.copy(level = if (it.level == level) TextLevel.PARAGRAPH else level)
        }
    }

    // endregion

    // region Images

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

    /** Adds [template] to the note (it replaces the note while that is still empty). */
    fun applyTemplate(template: NoteTemplate) = mutate { s ->
        s.copy(
            blocks = TemplateRules.apply(s.blocks, template),
            title = s.title.ifBlank { template.title },
        )
    }

    /** Saves the note's content (without images and audio) as a template called [name]. */
    fun saveAsTemplate(name: String) {
        val clean = TemplateRules.normalizeName(name) ?: return
        val blocks = TemplateRules.storableBlocks(_state.value.blocks)
        if (blocks.isEmpty()) {
            emit(EditorMessage.TEMPLATE_EMPTY)
            return
        }
        viewModelScope.launch {
            if (container.templatePreferences.count() >= TemplateRules.MAX_USER_TEMPLATES) {
                emit(EditorMessage.TEMPLATE_LIMIT)
                return@launch
            }
            container.templatePreferences.add(
                NoteTemplate(
                    id = BlockFactory.newId(),
                    name = clean,
                    title = _state.value.title,
                    blocks = blocks,
                ),
            )
            emit(EditorMessage.TEMPLATE_SAVED)
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch { container.templatePreferences.remove(id) }
    }

    /** Locks or unlocks the note. The caller has already verified the user. */
    fun toggleLocked() {
        mutate(record = false) { it.copy(locked = !it.locked) }
        flush()
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
        val after = change(before)
        if (after == before) return
        if (record && (after.title != before.title || after.blocks != before.blocks)) {
            history.record(before.toSnapshot(), coalesceKey, SystemClock.elapsedRealtime())
            publishHistory()
        }
        _state.value = after
        markChanged()
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
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis(),
        )
    }

    // endregion

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
        fun names(blocks: List<Block>) = blocks.mapNotNull { it.mediaFileName() }.toSet()
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

        fun factory(container: AppContainer, noteId: String) = viewModelFactory {
            initializer { NoteEditorViewModel(container, noteId) }
        }
    }
}
