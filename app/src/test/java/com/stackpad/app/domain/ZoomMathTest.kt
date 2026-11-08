package com.stackpad.app.domain

import com.stackpad.app.domain.image.ZoomMath
import org.junit.Assert.assertEquals
import org.junit.Test

class ZoomMathTest {
    @Test
    fun scaleIsClamped() {
        assertEquals(1f, ZoomMath.clampScale(0.3f))
        assertEquals(3f, ZoomMath.clampScale(3f))
        assertEquals(5f, ZoomMath.clampScale(9f))
    }

    @Test
    fun noPanAtNormalSize() {
        assertEquals(0f, ZoomMath.maxPan(1000f, 1f))
        assertEquals(0f, ZoomMath.clampPan(120f, 1000f, 1f))
    }

    @Test
    fun panStaysInsideZoomedImage() {
        assertEquals(500f, ZoomMath.maxPan(1000f, 2f))
        assertEquals(500f, ZoomMath.clampPan(900f, 1000f, 2f))
        assertEquals(-500f, ZoomMath.clampPan(-900f, 1000f, 2f))
        assertEquals(120f, ZoomMath.clampPan(120f, 1000f, 2f))
    }

    @Test
    fun doubleTapTogglesZoom() {
        assertEquals(2.5f, ZoomMath.doubleTapScale(1f))
        assertEquals(1f, ZoomMath.doubleTapScale(2.5f))
        assertEquals(1f, ZoomMath.doubleTapScale(4f))
    }
}
