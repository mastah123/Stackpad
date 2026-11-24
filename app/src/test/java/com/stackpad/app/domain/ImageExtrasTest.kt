package com.stackpad.app.domain

import com.stackpad.app.domain.image.Annotation
import com.stackpad.app.domain.image.CropRect
import com.stackpad.app.domain.image.ImageEdit
import com.stackpad.app.domain.image.ImageFilter
import com.stackpad.app.domain.image.ImageSize
import com.stackpad.app.domain.image.MarkStyle
import com.stackpad.app.domain.image.NormPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageExtrasTest {
    private fun apply(m: FloatArray, r: Float, g: Float, b: Float): Triple<Float, Float, Float> = Triple(
        m[0] * r + m[1] * g + m[2] * b + m[4],
        m[5] * r + m[6] * g + m[7] * b + m[9],
        m[10] * r + m[11] * g + m[12] * b + m[14],
    )

    @Test fun everyFilterIsAFourByFiveMatrix() {
        ImageFilter.entries.forEach { assertEquals(20, it.matrix.size) }
    }

    @Test fun noFilterLeavesColoursAlone() {
        assertEquals(Triple(0.2f, 0.5f, 0.9f), apply(ImageFilter.NONE.matrix, 0.2f, 0.5f, 0.9f))
    }

    @Test fun monoGivesEqualChannels() {
        val (r, g, b) = apply(ImageFilter.MONO.matrix, 0.9f, 0.2f, 0.4f)
        assertTrue(kotlin.math.abs(r - g) < 0.001f)
        assertTrue(kotlin.math.abs(g - b) < 0.001f)
    }

    @Test fun warmAddsRedAndCoolAddsBlue() {
        val warm = apply(ImageFilter.WARM.matrix, 0.5f, 0.5f, 0.5f)
        val cool = apply(ImageFilter.COOL.matrix, 0.5f, 0.5f, 0.5f)
        assertTrue(warm.first > warm.third)
        assertTrue(cool.third > cool.first)
    }

    @Test fun sizeLimitsTheLongerSideAndNeverEnlarges() {
        assertEquals(1280 to 960, ImageSize.MEDIUM.fit(4000, 3000).let { (w, h) -> w to h })
        assertEquals(960 to 1280, ImageSize.MEDIUM.fit(3000, 4000))
        assertEquals(500 to 300, ImageSize.MEDIUM.fit(500, 300))
        assertEquals(4000 to 3000, ImageSize.ORIGINAL.fit(4000, 3000))
        assertEquals(1 to 800, ImageSize.SMALL.fit(1, 100000))
    }

    @Test fun smallerSizesCompressMore() {
        assertTrue(ImageSize.SMALL.jpegQuality < ImageSize.MEDIUM.jpegQuality)
        assertTrue(ImageSize.MEDIUM.jpegQuality < ImageSize.ORIGINAL.jpegQuality)
    }

    @Test fun anyExtraCountsAsAChange() {
        assertTrue(ImageEdit().isUnchanged)
        assertFalse(ImageEdit().withFilter(ImageFilter.SEPIA).isUnchanged)
        assertFalse(ImageEdit().withSize(ImageSize.SMALL).isUnchanged)
        val mark = Annotation.Stroke(listOf(NormPoint(0.1f, 0.1f)), MarkStyle.colors[0], 0.01f, false)
        val marked = ImageEdit().withMark(mark)
        assertFalse(marked.isUnchanged)
        assertTrue(marked.withoutMarks().isUnchanged)
    }

    @Test fun geometryComparisonIgnoresFiltersAndMarks() {
        val a = ImageEdit()
        assertTrue(a.sameGeometry(a.withFilter(ImageFilter.MONO).withSize(ImageSize.LARGE)))
        assertFalse(a.sameGeometry(a.rotateRight()))
        assertFalse(a.sameGeometry(a.cropped(CropRect(0f, 0f, 0.5f, 0.5f))))
    }

    @Test fun rotatingKeepsTheFilterAndSize() {
        val edit = ImageEdit().withFilter(ImageFilter.VIVID).withSize(ImageSize.MEDIUM).rotateRight().flipHorizontal()
        assertEquals(ImageFilter.VIVID, edit.filter)
        assertEquals(ImageSize.MEDIUM, edit.size)
    }

    @Test fun highlighterIsThickerThanPen() {
        MarkStyle.Size.entries.forEach { assertTrue(it.strokeWidth(true) > it.strokeWidth(false)) }
    }
}
