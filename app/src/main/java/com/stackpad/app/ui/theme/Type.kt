package com.stackpad.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.stackpad.app.data.model.TextLevel

/**
 * Compact type scale: small but readable sizes with tight line heights.
 * Body text is 14sp on an 18sp line.
 */
private val BaseTypography = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
)

/**
 * Inter for body text and labels; Fraunces (serif) for note titles, headings and screen titles
 * (headlineSmall, titleLarge, titleMedium).
 */
val StackpadTypography: Typography = BaseTypography.let { t ->
    fun TextStyle.sans() = copy(fontFamily = InterFamily)
    fun TextStyle.serif() = copy(fontFamily = FrauncesFamily)
    t.copy(
        displayLarge = t.displayLarge.sans(),
        displayMedium = t.displayMedium.sans(),
        displaySmall = t.displaySmall.sans(),
        headlineLarge = t.headlineLarge.sans(),
        headlineMedium = t.headlineMedium.sans(),
        headlineSmall = t.headlineSmall.serif(),
        titleLarge = t.titleLarge.serif(),
        titleMedium = t.titleMedium.serif(),
        titleSmall = t.titleSmall.sans(),
        bodyLarge = t.bodyLarge.sans(),
        bodyMedium = t.bodyMedium.sans(),
        bodySmall = t.bodySmall.sans(),
        labelLarge = t.labelLarge.sans(),
        labelMedium = t.labelMedium.sans(),
        labelSmall = t.labelSmall.sans(),
    )
}

/** Text style for a block's heading level. */
@Composable
fun TextLevel.textStyle(): TextStyle = textStyle(MaterialTheme.typography)

fun TextLevel.textStyle(typography: Typography): TextStyle = when (this) {
    TextLevel.PARAGRAPH -> typography.bodyMedium
    TextLevel.H1 -> typography.headlineSmall
    TextLevel.H2 -> typography.titleLarge
    TextLevel.H3 -> typography.titleMedium
}
