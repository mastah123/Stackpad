package com.stackpad.app.domain.image

/**
 * The changes made to a picture in the editor. Order of operations: mirror, turn by quarters,
 * straighten by a small angle (the picture is cut to the largest area without empty corners),
 * then crop. The flip and rotate functions work on the picture as it is *shown*, and keep the
 * crop and the angle matching what the person sees.
 *
 * @property quarterTurns clockwise quarter turns, 0 to 3
 * @property mirrored mirrored left to right before turning
 * @property straighten clockwise angle in degrees, -45 to 45
 * @property filter colour look
 * @property size the biggest size the saved picture may have
 * @property annotations pen, highlighter, hidden regions and text, placed on the finished (cropped) picture
 */
data class ImageEdit(
    val quarterTurns: Int = 0,
    val mirrored: Boolean = false,
    val straighten: Float = 0f,
    val crop: CropRect = CropRect.FULL,
    val filter: ImageFilter = ImageFilter.NONE,
    val size: ImageSize = ImageSize.ORIGINAL,
    val annotations: List<Annotation> = emptyList(),
) {
    val isUnchanged: Boolean
        get() = quarterTurns == 0 && !mirrored && straighten == 0f && crop.isFull &&
            filter == ImageFilter.NONE && size == ImageSize.ORIGINAL && annotations.isEmpty()

    /** True when [other] shows the same part of the picture the same way (marks placed on it still fit). */
    fun sameGeometry(other: ImageEdit): Boolean =
        quarterTurns == other.quarterTurns && mirrored == other.mirrored &&
            straighten == other.straighten && crop == other.crop

    fun rotateRight() = copy(quarterTurns = (quarterTurns + 1) % 4, crop = crop.rotatedRight())

    fun rotateLeft() = copy(quarterTurns = (quarterTurns + 3) % 4, crop = crop.rotatedLeft())

    /** Mirrors the shown picture left to right. */
    fun flipHorizontal() = copy(
        quarterTurns = (4 - quarterTurns) % 4,
        mirrored = !mirrored,
        straighten = -straighten,
        crop = crop.mirrored(),
    )

    /** Mirrors the shown picture top to bottom. */
    fun flipVertical() = flipHorizontal().rotateRight().rotateRight()

    /** Straightening changes the cut-off area, so the crop starts again from the whole picture. */
    fun straightened(degrees: Float) = copy(straighten = degrees.coerceIn(-MAX_STRAIGHTEN, MAX_STRAIGHTEN), crop = CropRect.FULL)

    fun cropped(rect: CropRect) = copy(crop = rect)

    fun withFilter(value: ImageFilter) = copy(filter = value)

    fun withSize(value: ImageSize) = copy(size = value)

    fun withMark(mark: Annotation) = copy(annotations = annotations + mark)

    fun withoutMarks() = copy(annotations = emptyList())

    companion object {
        const val MAX_STRAIGHTEN = 45f
    }
}
