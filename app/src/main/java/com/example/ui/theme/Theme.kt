package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Ink,
    onPrimary = AppColors.Surface,
    primaryContainer = AppColors.Chip,
    onPrimaryContainer = AppColors.Ink,
    secondary = AppColors.Green,
    onSecondary = AppColors.Surface,
    secondaryContainer = AppColors.GreenTint,
    onSecondaryContainer = AppColors.GreenDeep,
    background = AppColors.Background,
    surface = AppColors.Surface,
    surfaceVariant = AppColors.Chip,
    onBackground = AppColors.Ink,
    onSurface = AppColors.Ink,
    onSurfaceVariant = AppColors.Muted,
    outline = AppColors.BorderStrong,
    outlineVariant = AppColors.Border,
    error = AppColors.AmberDeep
)

/** The app is light only for now. */
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
