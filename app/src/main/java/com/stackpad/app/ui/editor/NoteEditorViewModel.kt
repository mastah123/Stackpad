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
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.isBlank
import com.stackpad.app.data.model.mediaFileName
import com.stackpad.app.domain.text.SpanEditing
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
                    blocks = note.blocks.ifEmpty { listOf(BlockFactory.text()) },
                    pinned = note.pinned,
                )
            } else {
                _state.value = EditorUiState(loaded = true, blocks = listOf(BlockFactory.text()))
            }
        }
    }

    // region Editing

    fun setTitle(title: String) = mutate { it.copy(title = title) }

    fun togglePin() = mutate { it.copy(pinned = !it.pinned) }

    fun updateBlock(id: String, transform: (Block) -> Block) = mutate { s ->
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
            s.copy(blocks = list, focusBlockId = block.id)
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

        blocks[index].mediaFileName()?.let { name ->
            player.stopIfPlaying(name)
            media.delete(name)
        }
        mutate { s ->
            val remaining = s.blocks.filterNot { it.id == id }.ifEmpty { listOf(BlockFactory.text()) }
            val previousId = remaining.getOrNull((index - 1).coerceAtLeast(0))?.id
            s.copy(blocks = remaining, focusBlockId = if (viaBackspace) previousId else null)
        }
        if (lastFocusedId == id) lastFocusedId = null
        if (_activeText.value?.blockId == id) _activeText.value = null
    }

    fun consumeFocus(id: String) {
        _state.update { if (it.focusBlockId == id) it.copy(focusBlockId = null) else it }
    }

    // endregion

    // region Formatting

    fun onBlockFocused(id: String) {
        lastFocusedId = id
        if (_activeText.value?.blockId != id) _activeText.value = null
    }

    fun onTextSelection(id: String, start: Int, end: Int) {
        lastFocusedId = id
        _activeText.value = ActiveText(id, minOf(start, end), maxOf(start, end))
    }

    fun toggleSpan(type: SpanType) {
        val active = _activeText.value ?: return
        if (active.start == active.end) return
        edit<TextBlock>(active.blockId) {
            it.copy(spans = SpanEditing.toggle(it.spans, active.start, active.end, type))
        }
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

    /** Deletes the note and all of its media, then calls [onDeleted] on the main thread. */
    fun deleteNote(onDeleted: () -> Unit) {
        discarded = true
        saveJob?.cancel()
        _state.value.blocks.forEach { block -> block.mediaFileName()?.let(media::delete) }
        player.stop()
        viewModelScope.launch {
            repository.delete(noteId)
            onDeleted()
        }
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

    private fun mutate(change: (EditorUiState) -> EditorUiState) {
        if (!_state.value.loaded) return
        _state.update(change)
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
    }

    companion object {
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
