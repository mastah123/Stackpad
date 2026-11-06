package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.audio.PlaybackState
import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.ui.components.SmallIconButton
import com.stackpad.app.ui.components.WaveformBars
import com.stackpad.app.util.formatDuration

/**
 * Horizontal voice-note row: play/pause, waveform, duration, and an "x" to remove.
 * The waveform fills in and pulses while playing.
 */
@Composable
fun AudioBlockView(
    block: AudioBlock,
    playback: PlaybackState,
    available: Boolean,
    onTogglePlay: () -> Unit,
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
                    .padding(horizontal = 8.dp),
            )
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
