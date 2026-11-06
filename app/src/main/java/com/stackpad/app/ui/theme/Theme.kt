package com.stackpad.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E0FF),
    onPrimaryContainer = Color(0xFF16105E),
    secondary = Color(0xFF5C5B7A),
    onSecondary = Color.White,
    background = Color(0xFFFBFBFE),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBFBFE),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFEEEFF8),
    onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF777680),
    outlineVariant = Color(0xFFC9C8D3),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9EA3FF),
    onPrimary = Color(0xFF1B1A7A),
    primaryContainer = Color(0xFF34358F),
    onPrimaryContainer = Color(0xFFE2E0FF),
    secondary = Color(0xFFC4C3E4),
    onSecondary = Color(0xFF2D2D49),
    background = Color(0xFF121218),
    onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF121218),
    onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF23242E),
    onSurfaceVariant = Color(0xFFC9C8D3),
    outline = Color(0xFF918F9A),
    outlineVariant = Color(0xFF474853),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun StackpadTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = StackpadTypography,
        content = content,
    )
}
