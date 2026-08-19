package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.keyboard.KeyboardSettings
import com.example.keyboard.TerminalThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SnippetRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("terminal_keyboard_prefs", Context.MODE_PRIVATE)

    private val defaultSnippets = listOf(
        "git status",
        "cd ..",
        "ls -la",
        "npm run dev",
        "python3 ",
        "docker ps",
        "grep -rn ",
        "curl -I ",
        "sudo ",
        "clear"
    )

    private val _snippets = MutableStateFlow<List<String>>(emptyList())
    val snippets: StateFlow<List<String>> = _snippets.asStateFlow()

    private val _settings = MutableStateFlow(KeyboardSettings())
    val settings: StateFlow<KeyboardSettings> = _settings.asStateFlow()

    init {
        loadSnippets()
        loadSettings()
    }

    private fun loadSnippets() {
        val savedSet = prefs.getStringSet("saved_snippets", null)
        if (savedSet == null) {
            _snippets.value = defaultSnippets
            saveSnippetsInternal(defaultSnippets)
        } else {
            _snippets.value = savedSet.toList().sorted()
        }
    }

    private fun saveSnippetsInternal(list: List<String>) {
        prefs.edit().putStringSet("saved_snippets", list.toSet()).apply()
    }

    fun addSnippet(snippet: String) {
        val trimmed = snippet.trim()
        if (trimmed.isNotEmpty() && !_snippets.value.contains(trimmed)) {
            val updated = _snippets.value + trimmed
            _snippets.value = updated
            saveSnippetsInternal(updated)
        }
    }

    fun removeSnippet(snippet: String) {
        val updated = _snippets.value.filter { it != snippet }
        _snippets.value = updated
        saveSnippetsInternal(updated)
    }

    fun resetSnippets() {
        _snippets.value = defaultSnippets
        saveSnippetsInternal(defaultSnippets)
    }

    private fun loadSettings() {
        val haptic = prefs.getBoolean("haptic_feedback", true)
        val sound = prefs.getBoolean("sound_feedback", false)
        val themeId = prefs.getString("theme_preset", TerminalThemePreset.CYAN_DEFAULT.id)
        val theme = TerminalThemePreset.entries.find { it.id == themeId } ?: TerminalThemePreset.CYAN_DEFAULT
        val snippetBar = prefs.getBoolean("show_snippet_bar", true)

        _settings.value = KeyboardSettings(
            hapticFeedback = haptic,
            soundFeedback = sound,
            themePreset = theme,
            showSnippetBar = snippetBar
        )
    }

    fun updateHaptic(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
        _settings.value = _settings.value.copy(hapticFeedback = enabled)
    }

    fun updateSound(enabled: Boolean) {
        prefs.edit().putBoolean("sound_feedback", enabled).apply()
        _settings.value = _settings.value.copy(soundFeedback = enabled)
    }

    fun updateTheme(preset: TerminalThemePreset) {
        prefs.edit().putString("theme_preset", preset.id).apply()
        _settings.value = _settings.value.copy(themePreset = preset)
    }

    fun updateShowSnippetBar(enabled: Boolean) {
        prefs.edit().putBoolean("show_snippet_bar", enabled).apply()
        _settings.value = _settings.value.copy(showSnippetBar = enabled)
    }
}
