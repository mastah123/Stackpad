package com.stackpad.app.audio

import kotlin.math.sqrt

/** Helpers that turn raw microphone amplitudes into waveform bars (0..100). */
object Waveform {
    const val BARS = 40
    private const val MAX_AMPLITUDE = 32767f
    private const val MIN_LEVEL = 6

    /** Maps a raw MediaRecorder amplitude (0..32767) to a bar height; sqrt makes quiet sounds visible. */
    fun normalize(amplitude: Int): Int {
        val ratio = (amplitude / MAX_AMPLITUDE).coerceIn(0f, 1f)
        return (sqrt(ratio) * 100).toInt().coerceIn(MIN_LEVEL, 100)
    }

    /** Reduces [levels] to exactly [bars] bars, keeping the loudest value of each bucket. */
    fun downsample(levels: List<Int>, bars: Int = BARS): List<Int> {
        if (levels.isEmpty()) return List(bars) { MIN_LEVEL }
        return List(bars) { i ->
            val from = i * levels.size / bars
            val to = maxOf(from + 1, (i + 1) * levels.size / bars).coerceAtMost(levels.size)
            levels.subList(minOf(from, levels.size - 1), to).max()
        }
    }
}
