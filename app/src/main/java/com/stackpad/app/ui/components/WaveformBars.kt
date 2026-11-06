package com.stackpad.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Vertical bars forming a voice-message style waveform.
 * Bars before [progress] (0..1) use [activeColor]; while [playing], the bars around the
 * playhead pulse so the wave visibly moves.
 */
@Composable
fun WaveformBars(
    levels: List<Int>,
    progress: Float,
    playing: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
) {
    val pulse = if (playing) {
        rememberInfiniteTransition(label = "wave").animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(tween(260), RepeatMode.Reverse),
            label = "pulse",
        ).value
    } else {
        1f
    }

    Canvas(modifier = modifier) {
        val count = levels.size
        if (count == 0) return@Canvas
        val slot = size.width / count
        val barWidth = slot * 0.55f
        levels.forEachIndexed { index, level ->
            var height = size.height * (level.coerceIn(8, 100) / 100f)
            val playhead = progress * count
            if (playing && abs(index + 0.5f - playhead) < 1.5f) {
                height = (height * pulse).coerceAtMost(size.height)
            }
            val played = (index + 0.5f) / count <= progress
            drawRoundRect(
                color = if (played) activeColor else inactiveColor,
                topLeft = Offset(index * slot + (slot - barWidth) / 2f, (size.height - height) / 2f),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth / 2f),
            )
        }
    }
}
