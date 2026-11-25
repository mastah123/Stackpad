package com.stackpad.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warm off-white with navy text and controls. Highlights (selected items) are a soft pink pill; the coral is only a tint.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1C2340),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFDECEA),
    onPrimaryContainer = Color(0xFF1C2340),
    secondary = Color(0xFF6B54C9),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDECEA),
    onSecondaryContainer = Color(0xFF1C2340),
    tertiary = Color(0xFF4C9150),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8F3E6),
    onTertiaryContainer = Color(0xFF1F4A21),
    background = Color(0xFFFDFCFA),
    onBackground = Color(0xFF1C2340),
    surface = Color(0xFFFDFCFA),
    onSurface = Color(0xFF1C2340),
    surfaceVariant = Color(0xFFF4F0EB),
    onSurfaceVariant = Color(0xFF6B7592),
    surfaceTint = Color(0xFF1C2340),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF9F6),
    surfaceContainer = Color(0xFFF8F5F1),
    surfaceContainerHigh = Color(0xFFF4F0EB),
    surfaceContainerHighest = Color(0xFFEFEAE4),
    inverseSurface = Color(0xFF1C2340),
    inverseOnSurface = Color(0xFFF4F0EB),
    inversePrimary = Color(0xFFFF9A90),
    outline = Color(0xFF9AA0B5),
    outlineVariant = Color(0xFFEAE3DB),
    error = Color(0xFFD64545),
    onError = Color.White,
    errorContainer = Color(0xFFFDE8E8),
    onErrorContainer = Color(0xFF5C1212),
)

// The same hues on deep navy.
private val DarkColors = darkColorScheme(
    primary = Color(0xFFEDEFF8),
    onPrimary = Color(0xFF11152A),
    primaryContainer = Color(0xFF4A2F3A),
    onPrimaryContainer = Color(0xFFFFE5E2),
    secondary = Color(0xFFB7A8F5),
    onSecondary = Color(0xFF2A1F6B),
    secondaryContainer = Color(0xFF4A2F3A),
    onSecondaryContainer = Color(0xFFFFE5E2),
    tertiary = Color(0xFF9ED39B),
    onTertiary = Color(0xFF0F3A12),
    tertiaryContainer = Color(0xFF2A4D2C),
    onTertiaryContainer = Color(0xFFD3F0D0),
    background = Color(0xFF11152A),
    onBackground = Color(0xFFEDEFF8),
    surface = Color(0xFF11152A),
    onSurface = Color(0xFFEDEFF8),
    surfaceVariant = Color(0xFF1D2340),
    onSurfaceVariant = Color(0xFFA9B1CC),
    surfaceTint = Color(0xFFEDEFF8),
    surfaceContainerLowest = Color(0xFF0D1122),
    surfaceContainerLow = Color(0xFF151A32),
    surfaceContainer = Color(0xFF1A2038),
    surfaceContainerHigh = Color(0xFF1F2543),
    surfaceContainerHighest = Color(0xFF262D4E),
    inverseSurface = Color(0xFFEDEFF8),
    inverseOnSurface = Color(0xFF1C2340),
    inversePrimary = Color(0xFF1C2340),
    outline = Color(0xFF6F7799),
    outlineVariant = Color(0xFF2E3558),
    error = Color(0xFFFF9C9C),
    onError = Color(0xFF560909),
    errorContainer = Color(0xFF6B2020),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun StackpadTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = StackpadTypography,
        content = content,
    )
}
