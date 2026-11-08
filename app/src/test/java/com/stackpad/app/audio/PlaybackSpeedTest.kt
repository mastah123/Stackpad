package com.stackpad.app.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackSpeedTest {
    @Test
    fun cyclesThroughSpeeds() {
        assertEquals(1.5f, PlaybackSpeed.next(1f))
        assertEquals(2f, PlaybackSpeed.next(1.5f))
        assertEquals(1f, PlaybackSpeed.next(2f))
        assertEquals(1f, PlaybackSpeed.next(3.3f))
    }

    @Test
    fun labels() {
        assertEquals("1x", PlaybackSpeed.label(1f))
        assertEquals("1.5x", PlaybackSpeed.label(1.5f))
        assertEquals("2x", PlaybackSpeed.label(2f))
    }

    @Test
    fun positionIsClampedToDuration() {
        assertEquals(0, PlaybackSpeed.positionFor(-0.5f, 4000))
        assertEquals(2000, PlaybackSpeed.positionFor(0.5f, 4000))
        assertEquals(4000, PlaybackSpeed.positionFor(1.7f, 4000))
        assertEquals(0, PlaybackSpeed.positionFor(0.5f, 0))
    }
}
