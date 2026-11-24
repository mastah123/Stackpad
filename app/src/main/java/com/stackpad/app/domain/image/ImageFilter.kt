package com.stackpad.app.domain.image

/** Colour looks for a picture. [matrix] is a 4x5 colour matrix (red, green, blue, alpha rows) as Android and Compose use. */
enum class ImageFilter(val matrix: FloatArray) {
    NONE(IDENTITY()),
    MONO(saturation(0f)),
    SEPIA(
        floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        ),
    ),
    VIVID(saturation(1.45f)),
    COOL(channels(red = 0.92f, blue = 1.08f)),
    WARM(channels(red = 1.08f, blue = 0.92f)),
}

private fun IDENTITY(): FloatArray = channels(1f, 1f)

private fun channels(red: Float, blue: Float): FloatArray = floatArrayOf(
    red, 0f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f, 0f,
    0f, 0f, blue, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)

/** The usual saturation matrix: 0 is grey, 1 is unchanged, above 1 is more colourful. */
private fun saturation(s: Float): FloatArray {
    val r = 0.213f
    val g = 0.715f
    val b = 0.072f
    return floatArrayOf(
        r * (1 - s) + s, g * (1 - s), b * (1 - s), 0f, 0f,
        r * (1 - s), g * (1 - s) + s, b * (1 - s), 0f, 0f,
        r * (1 - s), g * (1 - s), b * (1 - s) + s, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )
}
