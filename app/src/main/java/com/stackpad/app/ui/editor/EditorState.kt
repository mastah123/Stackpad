package com.stackpad.app.ui.editor

import androidx.annotation.StringRes
import com.stackpad.app.R
import com.stackpad.app.data.model.Block

/** Everything the editor screen renders for the current note. */
data class EditorUiState(
    val loaded: Boolean = false,
    val title: String = "",
    val blocks: List<Block> = emptyList(),
    val pinned: Boolean = false,
    /** Block that should grab keyboard focus once it is on screen. */
    val focusBlockId: String? = null,
)

/** The text block the cursor is in, with its selection (start == end for a caret). */
data class ActiveText(val blockId: String, val start: Int, val end: Int)

/** Live state while an audio note is being recorded. */
data class RecordingUiState(
    val elapsedMs: Long = 0L,
    /** The most recent waveform bars (0..100) for the live display. */
    val levels: List<Int> = emptyList(),
)

/** One-off messages the screen shows in a snackbar. */
enum class EditorMessage(@StringRes val resId: Int) {
    RECORD_FAILED(R.string.msg_record_failed),
    RECORDING_TOO_SHORT(R.string.msg_recording_too_short),
    IMAGE_FAILED(R.string.msg_image_failed),
    CAMERA_UNAVAILABLE(R.string.msg_camera_unavailable),
    AUDIO_UNAVAILABLE(R.string.msg_audio_unavailable),
}
