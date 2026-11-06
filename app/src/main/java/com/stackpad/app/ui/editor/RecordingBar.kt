package com.stackpad.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
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
import com.stackpad.app.ui.components.SmallIconButton
import com.stackpad.app.ui.components.WaveformBars
import com.stackpad.app.util.formatDuration

/** Shown while recording: cancel, live waveform, timer, and a stop button that saves the note. */
@Composable
fun RecordingBar(
    state: RecordingUiState,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(start = 2.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallIconButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_cancel_recording),
                onClick = onCancel,
                tint = colors.onSurface,
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.error),
            )
            WaveformBars(
                levels = state.levels,
                progress = 1f,
                playing = false,
                activeColor = colors.error,
                inactiveColor = colors.outlineVariant,
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .padding(horizontal = 8.dp),
            )
            Text(
                text = formatDuration(state.elapsedMs),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface,
                modifier = Modifier.padding(end = 6.dp),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.error)
                    .clickable(role = Role.Button, onClick = onStop),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = stringResource(R.string.cd_stop_recording),
                    modifier = Modifier.size(18.dp),
                    tint = colors.onError,
                )
            }
        }
    }
}
