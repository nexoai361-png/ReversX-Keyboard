package com.example.keyboard

enum class ShiftState {
    OFF,
    ON,
    CAPS_LOCK
}

enum class KeyboardMode {
    ALPHA,
    SYMBOLS,
    SNIPPETS,
    EMOJI
}

enum class TerminalThemePreset(val id: String, val title: String, val primaryColorHex: Long, val surfaceColorHex: Long) {
    CYAN_DEFAULT("cyan", "Cyan Terminal", 0xFFA2C9FF, 0xFF121414),
    MATRIX_GREEN("green", "Matrix Hacker", 0xFF79FF5B, 0xFF0A0F0D),
    AMBER_CRT("amber", "Amber CRT", 0xFFFFB800, 0xFF14100A),
    DRACULA_PURPLE("dracula", "Dracula Dark", 0xFFBD93F9, 0xFF1E1E2E)
}

data class KeyboardSettings(
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val themePreset: TerminalThemePreset = TerminalThemePreset.CYAN_DEFAULT,
    val showSnippetBar: Boolean = true
)
