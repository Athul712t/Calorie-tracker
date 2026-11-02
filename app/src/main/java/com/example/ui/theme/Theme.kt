package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = DarkPill,
    onPrimary = DarkPillText,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextBlack,
    secondary = NothingRed,
    onSecondary = DarkPillText,
    secondaryContainer = NothingRedLight,
    onSecondaryContainer = NothingRed,
    background = CanvasBackground,
    surface = SurfaceWhite,
    surfaceVariant = SurfaceElevated,
    onBackground = TextBlack,
    onSurface = TextBlack,
    onSurfaceVariant = TextMuted,
    outline = BorderLight,
    outlineVariant = BorderSubtle,
    error = NothingRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
