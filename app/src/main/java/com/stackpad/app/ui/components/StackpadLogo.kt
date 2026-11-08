package com.stackpad.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The Stackpad mark: three stacked bars, the middle one with a check. Decorative. */
@Composable
fun StackpadLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    checkColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val barHeight = w * 0.22f
        val gap = w * 0.085f
        val top = (w - (barHeight * 3 + gap * 2)) / 2f
        val radius = CornerRadius(barHeight / 2f)
        repeat(3) { index ->
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, top + index * (barHeight + gap)),
                size = Size(w, barHeight),
                cornerRadius = radius,
            )
        }
        // Check mark on the middle bar.
        val midY = top + barHeight + gap + barHeight / 2f
        val check = Path().apply {
            moveTo(w * 0.12f, midY)
            lineTo(w * 0.19f, midY + barHeight * 0.28f)
            lineTo(w * 0.31f, midY - barHeight * 0.28f)
        }
        drawPath(
            path = check,
            color = checkColor,
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
