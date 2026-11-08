package com.stackpad.app.audio

/** The playback speeds offered for voice notes, cycled with one button. */
object PlaybackSpeed {
    val steps: List<Float> = listOf(1f, 1.5f, 2f)

    /** The speed after [current]; unknown values restart at normal speed. */
    fun next(current: Float): Float {
        val index = steps.indexOf(current)
        return if (index < 0) steps.first() else steps[(index + 1) % steps.size]
    }

    /** Short label such as `1x` or `1.5x`. */
    fun label(speed: Float): String =
        if (speed == speed.toInt().toFloat()) "${speed.toInt()}x" else "${speed}x"

    /** Playhead time for a tap or drag at [fraction] (0..1) of the waveform. */
    fun positionFor(fraction: Float, durationMs: Int): Int =
        (fraction.coerceIn(0f, 1f) * durationMs).toInt().coerceIn(0, maxOf(durationMs, 0))
}
