package com.stackpad.app.domain.image

/** A point on the picture as fractions of its width and height (0 to 1). */
data class NormPoint(val x: Float, val y: Float)

/** Something drawn on the picture. All positions are fractions of the finished (cropped) picture. */
sealed interface Annotation {
    /** A freehand line; [width] is a fraction of the picture's shorter side. */
    data class Stroke(val points: List<NormPoint>, val color: Int, val width: Float, val highlight: Boolean) : Annotation

    /** A region that is blurred or pixelated to hide what is in it. */
    data class Obscure(val rect: CropRect, val pixelate: Boolean) : Annotation

    /** Text with its top-left corner at ([x], [y]); [size] is a fraction of the picture's shorter side. */
    data class Label(val text: String, val x: Float, val y: Float, val size: Float, val color: Int) : Annotation
}

/** Colours and sizes offered for marking up. */
object MarkStyle {
    val colors: List<Int> = listOf(
        0xFFE53935.toInt(), // red
        0xFFFB8C00.toInt(), // orange
        0xFFFDD835.toInt(), // yellow
        0xFF43A047.toInt(), // green
        0xFF1E88E5.toInt(), // blue
        0xFFFFFFFF.toInt(), // white
        0xFF000000.toInt(), // black
    )

    enum class Size(private val line: Float, val text: Float) {
        SMALL(0.006f, 0.04f),
        MEDIUM(0.012f, 0.07f),
        LARGE(0.024f, 0.11f),
        ;

        /** Highlighter lines are three times as thick as pen lines. */
        fun strokeWidth(highlight: Boolean): Float = if (highlight) line * 3 else line
    }
}
