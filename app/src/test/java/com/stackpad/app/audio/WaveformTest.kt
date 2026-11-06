package com.stackpad.app.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WaveformTest {
    @Test
    fun normalizeStaysInRange() {
        assertEquals(6, Waveform.normalize(0))
        assertEquals(100, Waveform.normalize(32767))
        assertEquals(100, Waveform.normalize(99999))
        assertEquals(6, Waveform.normalize(-5))
        assertTrue(Waveform.normalize(1000) in 6..100)
    }

    @Test
    fun downsampleAlwaysReturnsRequestedBars() {
        assertEquals(List(Waveform.BARS) { 6 }, Waveform.downsample(emptyList()))
        assertEquals(Waveform.BARS, Waveform.downsample(List(5) { 50 }).size)
        assertEquals(Waveform.BARS, Waveform.downsample(List(1000) { it % 100 }).size)
    }

    @Test
    fun downsampleKeepsLoudestValueOfEachBucket() {
        val result = Waveform.downsample(List(100) { it }, bars = 10)
        assertEquals(99, result.last())
        assertEquals(9, result.first())
        assertEquals(listOf(10, 10, 90, 90), Waveform.downsample(listOf(10, 90), bars = 4))
    }
}
