package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.audio.PlaybackSpeed
import com.stackpad.app.audio.PlaybackState
import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.ui.components.SmallIconButton
import com.stackpad.app.ui.components.WaveformBars
import com.stackpad.app.util.formatDuration

/**
 * Horizontal voice-note row: play/pause, waveform, duration, and an "x" to remove.
 * The waveform fills in and pulses while playing. Tap or drag the waveform to seek, and tap the
 * speed chip to cycle 1x / 1.5x / 2x.
 */
@Composable
fun AudioBlockView(
    block: AudioBlock,
    playback: PlaybackState,
    available: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onCycleSpeed: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val isCurrent = playback.fileName == block.fileName
    val playing = isCurrent && playback.isPlaying
    val progress = if (isCurrent && block.durationMs > 0) {
        (playback.positionMs / block.durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val speedText = PlaybackSpeed.label(playback.speed)
    val speedLabel = stringResource(R.string.cd_audio_speed, speedText)
    val seekLabel = stringResource(R.string.cd_audio_seek)
    val shownMs = if (isCurrent) playback.positionMs.toLong() else block.durationMs

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 2.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (available) colors.primary else colors.outline)
                    .clickable(enabled = available, role = Role.Button, onClick = onTogglePlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(
                        if (playing) R.string.cd_pause_audio else R.string.cd_play_audio,
                    ),
                    modifier = Modifier.size(20.dp),
                    tint = colors.onPrimary,
                )
            }
            WaveformBars(
                levels = block.waveform,
                progress = progress,
                playing = playing,
                activeColor = colors.primary,
                inactiveColor = colors.outlineVariant,
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .padding(horizontal = 8.dp)
                    .seekable(enabled = available, progress = progress, onSeek = onSeek, label = seekLabel),
            )
            if (available) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surface,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(role = Role.Button, onClick = onCycleSpeed)
                        .semantics {
                            contentDescription = speedLabel
                        },
                ) {
                    Text(
                        text = PlaybackSpeed.label(playback.speed),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Text(
                text = if (available) formatDuration(shownMs) else stringResource(R.string.audio_unavailable),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            SmallIconButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_remove_audio),
                onClick = onRemove,
                size = 28.dp,
                iconSize = 16.dp,
            )
        }
    }
}

/** Tap or drag to pick a position (0..1 across the width). Also exposes a slider to TalkBack. */
private fun Modifier.seekable(
    enabled: Boolean,
    progress: Float,
    onSeek: (Float) -> Unit,
    label: String,
): Modifier {
    if (!enabled) return this
    return this
        .pointerInput(Unit) {
            detectTapGestures { offset -> onSeek(offset.x / size.width) }
        }
        .pointerInput(Unit) {
            detectHorizontalDragGestures { change, _ ->
                change.consume()
                onSeek(change.position.x / size.width)
            }
        }
        .semantics {
            contentDescription = label
            progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
            setProgress { value ->
                onSeek(value)
                true
            }
        }
}
