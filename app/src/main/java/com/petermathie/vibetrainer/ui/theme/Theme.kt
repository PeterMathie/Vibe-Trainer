package com.petermathie.vibetrainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VibeDarkScheme = darkColorScheme(
    primary = VibeColors.Accent,
    onPrimary = VibeColors.InkOnAccent,
    primaryContainer = VibeColors.Accent,
    onPrimaryContainer = VibeColors.InkOnAccent,
    background = VibeColors.AppBackground,
    onBackground = VibeColors.TextPrimary,
    surface = VibeColors.CardBackground,
    onSurface = VibeColors.TextPrimary,
    surfaceVariant = VibeColors.CardRaised,
    onSurfaceVariant = VibeColors.TextMuted,
    outline = VibeColors.BorderSubtle,
)

@Composable
fun VibeTrainerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VibeDarkScheme,
        typography = VibeTypography,
        content = content,
    )
}
