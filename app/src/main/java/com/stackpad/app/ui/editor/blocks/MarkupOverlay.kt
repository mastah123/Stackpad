package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.stackpad.app.data.media.AnnotationPainter
import com.stackpad.app.domain.image.Annotation
import com.stackpad.app.domain.image.CropRect
import com.stackpad.app.domain.image.MarkStyle
import com.stackpad.app.domain.image.NormPoint
import kotlin.math.max
import kotlin.math.min

/** What a finger does on the picture in the Mark up tab. */
enum class MarkupTool { DRAW, HIGHLIGHT, BLUR, PIXELATE, TEXT }

/**
 * Draws the marks over the picture (which is shown with ContentScale.Fit in the same box) and turns
 * finger movement into new marks: a line for pen and highlighter, a box for blur and pixelate, a
 * tap for text. Positions are handed on as fractions of the picture.
 */
@Composable
fun MarkupOverlay(
    pictureWidth: Int,
    pictureHeight: Int,
    marks: List<Annotation>,
    tool: MarkupTool,
    color: Int,
    size: MarkStyle.Size,
    onAdd: (Annotation) -> Unit,
    onTextAt: (NormPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    var box by remember { mutableStateOf(IntSize.Zero) }
    var draft by remember { mutableStateOf<List<NormPoint>>(emptyList()) }
    val pen = tool == MarkupTool.DRAW || tool == MarkupTool.HIGHLIGHT
    val highlight = tool == MarkupTool.HIGHLIGHT

    val scale = if (box.width == 0 || box.height == 0) 1f else min(box.width / pictureWidth.toFloat(), box.height / pictureHeight.toFloat())
    val left = (box.width - pictureWidth * scale) / 2f
    val top = (box.height - pictureHeight * scale) / 2f

    fun toNorm(point: Offset) = NormPoint(
        ((point.x - left) / (pictureWidth * scale)).coerceIn(0f, 1f),
        ((point.y - top) / (pictureHeight * scale)).coerceIn(0f, 1f),
    )

    Canvas(
        modifier = modifier
            .onSizeChanged { box = it }
            .pointerInput(tool, color, size, box, pictureWidth, pictureHeight) {
                fun finish() {
                    val points = draft
                    draft = emptyList()
                    if (points.isEmpty()) return
                    if (pen) {
                        onAdd(Annotation.Stroke(points, color, size.strokeWidth(highlight), highlight))
                    } else if (points.size >= 2) {
                        val a = points.first()
                        val b = points.last()
                        val rect = CropRect(min(a.x, b.x), min(a.y, b.y), max(a.x, b.x), max(a.y, b.y))
                        if (rect.width > MIN_REGION && rect.height > MIN_REGION) {
                            onAdd(Annotation.Obscure(rect, pixelate = tool == MarkupTool.PIXELATE))
                        }
                    }
                }
                if (tool == MarkupTool.TEXT) {
                    detectTapGestures { onTextAt(toNorm(it)) }
                } else {
                    detectDragGestures(
                        onDragStart = { draft = listOf(toNorm(it)) },
                        onDrag = { change, _ ->
                            change.consume()
                            val point = toNorm(change.position)
                            draft = if (pen) draft + point else listOf(draft.first(), point)
                        },
                        onDragEnd = { finish() },
                        onDragCancel = { draft = emptyList() },
                    )
                }
            },
    ) {
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            native.save()
            native.translate(left, top)
            native.scale(scale, scale)
            val live = if (pen && draft.isNotEmpty()) {
                listOf(Annotation.Stroke(draft, color, size.strokeWidth(highlight), highlight))
            } else {
                emptyList()
            }
            AnnotationPainter.drawMarks(native, pictureWidth, pictureHeight, marks + live)
            native.restore()
        }
        if (!pen && draft.size == 2) {
            val a = draft.first()
            val b = draft.last()
            val pw = pictureWidth * scale
            val ph = pictureHeight * scale
            drawRect(
                color = Color.White,
                topLeft = Offset(left + min(a.x, b.x) * pw, top + min(a.y, b.y) * ph),
                size = Size(kotlin.math.abs(a.x - b.x) * pw, kotlin.math.abs(a.y - b.y) * ph),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}

private const val MIN_REGION = 0.02f
