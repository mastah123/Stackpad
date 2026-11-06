package com.stackpad.app.audio

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/** Which audio file is loaded, whether it is playing, and the playhead position. */
data class PlaybackState(
    val fileName: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Int = 0,
)

/** Plays one audio note at a time. Must be used from the main thread. */
class AudioPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var player: MediaPlayer? = null
    private var ticker: Job? = null

    /** Plays, pauses or resumes [fileName]. Returns false if the file could not be played. */
    fun toggle(fileName: String, file: File): Boolean {
        val current = player
        if (current != null && _state.value.fileName == fileName) {
            if (current.isPlaying) {
                current.pause()
                ticker?.cancel()
                _state.update { it.copy(isPlaying = false) }
            } else {
                current.start()
                _state.update { it.copy(isPlaying = true) }
                startTicker()
            }
            return true
        }

        stop()
        if (!file.exists()) return false

        val next = MediaPlayer()
        return try {
            next.setDataSource(file.absolutePath)
            next.prepare()
            next.setOnCompletionListener { stop() }
            next.start()
            player = next
            _state.value = PlaybackState(fileName = fileName, isPlaying = true, positionMs = 0)
            startTicker()
            true
        } catch (e: Exception) {
            runCatching { next.release() }
            false
        }
    }

    /** Stops playback and resets state. */
    fun stop() {
        ticker?.cancel()
        ticker = null
        player?.let {
            runCatching { it.stop() }
            it.release()
        }
        player = null
        _state.value = PlaybackState()
    }

    /** Stops playback only if [fileName] is the one currently loaded. */
    fun stopIfPlaying(fileName: String) {
        if (_state.value.fileName == fileName) stop()
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val p = player
                if (p != null && p.isPlaying) {
                    val position = p.currentPosition
                    _state.update { it.copy(positionMs = position) }
                }
                delay(TICK_MS)
            }
        }
    }

    private companion object {
        const val TICK_MS = 50L
    }
}
