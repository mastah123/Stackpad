package com.stackpad.app.domain

import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.mediaFileNames
import com.stackpad.app.domain.image.AspectChoice
import com.stackpad.app.domain.image.CropHandle
import com.stackpad.app.domain.image.CropMath
import com.stackpad.app.domain.image.CropRect
import com.stackpad.app.domain.image.EditGeometry
import com.stackpad.app.domain.image.EditHistory
import com.stackpad.app.domain.image.ImageEdit
import com.stackpad.app.domain.image.PixelSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ImageEditTest {
    private fun near(a: Float, b: Float) = abs(a - b) < 0.001f
    private fun assertRect(e: CropRect, a: CropRect) =
        assertTrue("expected $e but was $a", near(e.left, a.left) && near(e.top, a.top) && near(e.right, a.right) && near(e.bottom, a.bottom))

    /** Where a point of the source (fractions) ends up when shown: mirror, then turn clockwise by quarters. */
    private fun shown(p: Pair<Float, Float>, edit: ImageEdit): Pair<Float, Float> {
        var (x, y) = p
        if (edit.mirrored) x = 1f - x
        repeat(edit.quarterTurns) { val nx = 1f - y; y = x; x = nx }
        return x to y
    }

    // region orientation

    @Test fun four_right_turns_are_no_change() {
        val edit = ImageEdit().rotateRight().rotateRight().rotateRight().rotateRight()
        assertTrue(edit.isUnchanged)
    }

    @Test fun left_undoes_right() {
        assertTrue(ImageEdit().rotateRight().rotateLeft().isUnchanged)
    }

    @Test fun flip_twice_is_no_change_in_any_orientation() {
        for (turns in 0..3) {
            var e = ImageEdit()
            repeat(turns) { e = e.rotateRight() }
            assertEquals(e, e.flipHorizontal().flipHorizontal())
            assertEquals(e, e.flipVertical().flipVertical())
        }
    }

    @Test fun flips_match_what_is_shown() {
        val point = 0.2f to 0.1f
        val sequences: List<List<(ImageEdit) -> ImageEdit>> = listOf(
            listOf({ e -> e.rotateRight() }, { e -> e.flipHorizontal() }),
            listOf({ e -> e.rotateRight() }, { e -> e.flipVertical() }),
            listOf({ e -> e.flipVertical() }, { e -> e.rotateLeft() }, { e -> e.flipHorizontal() }),
            listOf({ e -> e.rotateLeft() }, { e -> e.flipHorizontal() }, { e -> e.rotateRight() }, { e -> e.flipVertical() }),
        )
        // The same actions done on the shown point: rotate right (x,y)->(1-y,x), left (x,y)->(y,1-x), flips mirror.
        val onShown: List<List<(Pair<Float, Float>) -> Pair<Float, Float>>> = listOf(
            listOf({ p -> 1f - p.second to p.first }, { p -> 1f - p.first to p.second }),
            listOf({ p -> 1f - p.second to p.first }, { p -> p.first to 1f - p.second }),
            listOf({ p -> p.first to 1f - p.second }, { p -> p.second to 1f - p.first }, { p -> 1f - p.first to p.second }),
            listOf(
                { p -> p.second to 1f - p.first }, { p -> 1f - p.first to p.second },
                { p -> 1f - p.second to p.first }, { p -> p.first to 1f - p.second },
            ),
        )
        sequences.indices.forEach { i ->
            var edit = ImageEdit()
            sequences[i].forEach { edit = it(edit) }
            var expected = point
            onShown[i].forEach { expected = it(expected) }
            val actual = shown(point, edit)
            assertTrue("sequence $i: expected $expected but was $actual", near(expected.first, actual.first) && near(expected.second, actual.second))
        }
    }

    @Test fun flipping_reverses_the_straighten_angle() {
        val e = ImageEdit().straightened(10f)
        assertEquals(-10f, e.flipHorizontal().straighten)
        assertEquals(-10f, e.flipVertical().straighten)
        assertEquals(10f, e.rotateRight().straighten)
    }

    @Test fun straighten_is_limited_and_clears_the_crop() {
        val e = ImageEdit(crop = CropRect(0.1f, 0.1f, 0.5f, 0.5f)).straightened(90f)
        assertEquals(45f, e.straighten)
        assertTrue(e.crop.isFull)
        assertEquals(-45f, ImageEdit().straightened(-80f).straighten)
    }

    // endregion

    // region crop mapping

    @Test fun crop_follows_the_picture_when_it_turns() {
        val c = CropRect(0.1f, 0.2f, 0.5f, 0.9f)
        assertRect(CropRect(0.1f, 0.1f, 0.8f, 0.5f), c.rotatedRight())
        assertRect(CropRect(0.2f, 0.5f, 0.9f, 0.9f), c.rotatedLeft())
        assertRect(CropRect(0.5f, 0.2f, 0.9f, 0.9f), c.mirrored())
        assertRect(c, c.rotatedRight().rotatedLeft())
        assertRect(c, c.mirrored().mirrored())
        var r = c
        repeat(4) { r = r.rotatedRight() }
        assertRect(c, r)
    }

    @Test fun crop_corner_moves_with_the_picture() {
        // The top-left corner of the picture goes to the top-right when turned right.
        val corner = CropRect(0f, 0f, 0.25f, 0.25f).rotatedRight()
        assertRect(CropRect(0.75f, 0f, 1f, 0.25f), corner)
    }

    // endregion

    // region geometry

    @Test fun straighten_scale_known_values() {
        assertTrue(near(1f, EditGeometry.straightenScale(400, 300, 0f)))
        assertTrue(near(0.7071f, EditGeometry.straightenScale(500, 500, 45f)))
        // A wide picture loses more when straightened than a square one.
        assertTrue(EditGeometry.straightenScale(400, 100, 10f) < EditGeometry.straightenScale(400, 400, 10f))
        // The sign of the angle does not matter.
        assertTrue(near(EditGeometry.straightenScale(400, 300, 7f), EditGeometry.straightenScale(400, 300, -7f)))
    }

    @Test fun straightened_picture_has_no_empty_corners() {
        val w = 400
        val h = 300
        val deg = 12f
        val k = EditGeometry.straightenScale(w, h, deg)
        val a = Math.toRadians(deg.toDouble())
        // Corners of the kept area, turned back into the source picture, are inside it.
        for (sx in listOf(-1, 1)) for (sy in listOf(-1, 1)) {
            val x = sx * k * w / 2.0
            val y = sy * k * h / 2.0
            val rx = x * Math.cos(a) + y * Math.sin(a)
            val ry = -x * Math.sin(a) + y * Math.cos(a)
            assertTrue(Math.abs(rx) <= w / 2.0 + 0.01 && Math.abs(ry) <= h / 2.0 + 0.01)
        }
    }

    @Test fun sizes_swap_on_odd_turns() {
        assertEquals(PixelSize(400, 300), EditGeometry.baseSize(400, 300, ImageEdit()))
        assertEquals(PixelSize(300, 400), EditGeometry.baseSize(400, 300, ImageEdit().rotateRight()))
        assertEquals(PixelSize(400, 300), EditGeometry.baseSize(400, 300, ImageEdit().rotateRight().rotateRight()))
    }

    @Test fun output_size_is_the_crop_of_the_base() {
        val edit = ImageEdit(crop = CropRect(0.25f, 0f, 0.75f, 0.5f))
        assertEquals(PixelSize(200, 150), EditGeometry.outputSize(400, 300, edit))
        val rect = EditGeometry.cropPixels(PixelSize(400, 300), CropRect(0.25f, 0f, 0.75f, 0.5f))
        assertEquals(100, rect.left)
        assertEquals(0, rect.top)
    }

    @Test fun crop_pixels_stay_inside_and_not_empty() {
        val rect = EditGeometry.cropPixels(PixelSize(100, 100), CropRect(0.999f, 0.999f, 1f, 1f))
        assertTrue(rect.width >= 1 && rect.height >= 1 && rect.left + rect.width <= 100 && rect.top + rect.height <= 100)
    }

    // endregion

    // region crop frame

    @Test fun move_stays_inside_the_picture() {
        val r = CropRect(0.2f, 0.2f, 0.6f, 0.6f)
        assertRect(CropRect(0f, 0.2f, 0.4f, 0.6f), CropMath.move(r, -0.5f, 0f))
        assertRect(CropRect(0.6f, 0.6f, 1f, 1f), CropMath.move(r, 0.9f, 0.9f))
    }

    @Test fun free_drag_moves_only_the_grabbed_edges() {
        val r = CropRect(0.2f, 0.2f, 0.6f, 0.6f)
        assertRect(CropRect(0.1f, 0.2f, 0.6f, 0.6f), CropMath.drag(r, CropHandle.LEFT, -0.1f, 0.3f, null))
        assertRect(CropRect(0.2f, 0.2f, 0.7f, 0.7f), CropMath.drag(r, CropHandle.BOTTOM_RIGHT, 0.1f, 0.1f, null))
    }

    @Test fun free_drag_cannot_cross_or_leave() {
        val r = CropRect(0.2f, 0.2f, 0.6f, 0.6f)
        val a = CropMath.drag(r, CropHandle.RIGHT, -1f, 0f, null)
        assertTrue(near(0.2f + CropMath.MIN_SIZE, a.right))
        val b = CropMath.drag(r, CropHandle.TOP_LEFT, -5f, -5f, null)
        assertTrue(b.left == 0f && b.top == 0f)
    }

    @Test fun fixed_drag_keeps_the_shape_and_the_opposite_corner() {
        val r = CropRect(0.1f, 0.1f, 0.5f, 0.5f)
        val a = CropMath.drag(r, CropHandle.BOTTOM_RIGHT, 0.2f, 0.0f, 1f)
        assertTrue(near(0.1f, a.left) && near(0.1f, a.top))
        assertTrue(near(a.width, a.height))
        assertTrue(a.right > 0.5f)
        // Edges do nothing when the shape is fixed.
        assertRect(r, CropMath.drag(r, CropHandle.RIGHT, 0.2f, 0f, 1f))
    }

    @Test fun fixed_drag_cannot_leave_the_picture() {
        val r = CropRect(0.1f, 0.1f, 0.5f, 0.5f)
        val a = CropMath.drag(r, CropHandle.BOTTOM_RIGHT, 2f, 2f, 2f)
        assertTrue(a.right <= 1.0001f && a.bottom <= 1.0001f)
        assertTrue(near(a.width / a.height, 2f))
    }

    @Test fun fit_gives_the_largest_centred_shape() {
        val full = CropMath.fit(CropRect.FULL, 1f)
        assertRect(CropRect(0f, 0f, 1f, 1f), full) // fractions: square in fraction space
        val wide = CropMath.fit(CropRect.FULL, 2f)
        assertRect(CropRect(0f, 0.25f, 1f, 0.75f), wide)
        val tall = CropMath.fit(CropRect.FULL, 0.5f)
        assertRect(CropRect(0.25f, 0f, 0.75f, 1f), tall)
    }

    @Test fun pixel_ratio_becomes_a_fraction_ratio() {
        val base = PixelSize(400, 200)
        // A square in pixels is half as wide as tall in fractions of a 2:1 picture.
        assertTrue(near(0.5f, CropMath.fractionAspect(1f, base)))
        val square = CropMath.fit(CropRect.FULL, CropMath.fractionAspect(1f, base))
        assertRect(CropRect(0.25f, 0f, 0.75f, 1f), square)
        assertTrue(near(2f, AspectChoice.ORIGINAL.pixelRatio(base)!!))
        assertEquals(null, AspectChoice.FREE.pixelRatio(base))
        assertFalse(AspectChoice.FREE.isFixed)
    }

    // endregion

    // region history

    @Test fun history_undo_and_reset() {
        var h = EditHistory()
        assertFalse(h.canUndo)
        h = h.commit(h.current.rotateRight())
        h = h.commit(h.current.flipHorizontal())
        assertTrue(h.isChanged && h.canUndo)
        h = h.undo()
        assertEquals(ImageEdit().rotateRight(), h.current)
        h = h.reset()
        assertFalse(h.isChanged)
        assertTrue(h.canUndo)
        h = h.undo()
        assertEquals(ImageEdit().rotateRight(), h.current)
    }

    @Test fun committing_the_same_edit_adds_no_step() {
        val h = EditHistory()
        assertEquals(h, h.commit(ImageEdit()))
        assertFalse(h.undo().canUndo)
    }

    @Test fun history_is_limited() {
        var h = EditHistory()
        repeat(80) { i -> h = h.commit(ImageEdit().straightened((i % 40).toFloat() + 1f)) }
        var steps = 0
        while (h.canUndo) { h = h.undo(); steps++ }
        assertEquals(50, steps)
    }

    // endregion

    // region media names

    @Test fun edited_pictures_keep_both_files_on_disk() {
        val plain = ImageBlock(id = "1", fileName = "a.jpg")
        assertEquals(listOf("a.jpg"), plain.mediaFileNames())
        val edited = plain.copy(fileName = "b.jpg", originalFileName = "a.jpg")
        assertEquals(listOf("b.jpg", "a.jpg"), edited.mediaFileNames())
        assertEquals(emptyList<String>(), TextBlock(id = "2").mediaFileNames())
    }

    // endregion
}
