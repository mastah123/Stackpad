package com.stackpad.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.stackpad.app.R

private val WEIGHTS = listOf(
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
)

/** One entry per weight, all drawn from a single variable font file (needs Android 8.0+, which the app requires). */
@OptIn(ExperimentalTextApi::class)
private fun variableFonts(resId: Int, style: FontStyle): List<Font> = WEIGHTS.map { weight ->
    Font(resId, weight, style, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))
}

/** Plus Jakarta Sans, upright and italic: the one font used for all text in the app. */
val AppFontFamily: FontFamily = FontFamily(
    variableFonts(R.font.plus_jakarta_sans, FontStyle.Normal) +
        variableFonts(R.font.plus_jakarta_sans_italic, FontStyle.Italic),
)
