package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TerminalDarkColorScheme = darkColorScheme(
    primary = TerminalCyanPrimary,
    onPrimary = TerminalBlack,
    primaryContainer = Color(0xFF1E2F40),
    onPrimaryContainer = TerminalCyanPrimary,
    secondary = TerminalGreenAccent,
    onSecondary = TerminalBlack,
    background = TerminalBackground,
    onBackground = TerminalOnSurface,
    surface = TerminalBackground,
    onSurface = TerminalOnSurface,
    surfaceVariant = TerminalKeyBackground,
    onSurfaceVariant = TerminalSecondaryText,
    outline = TerminalOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force dark terminal aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TerminalDarkColorScheme,
        typography = Typography,
        content = content
    )
}

