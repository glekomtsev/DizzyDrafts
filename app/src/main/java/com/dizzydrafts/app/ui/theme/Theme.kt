package com.dizzydrafts.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFF90A4AE),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF0D0D0D),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF252525),
    onSurfaceVariant = Color(0xFFB0B0B0),
    primaryContainer = Color(0xFF1A3A5C),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondaryContainer = Color(0xFF263238),
    onSecondaryContainer = Color(0xFFB0BEC5),
    outline = Color(0xFF424242)
)

@Composable
fun DizzyDraftsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
