package com.example.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ArrowDirection {
    UP, DOWN, LEFT, RIGHT
}

@Composable
fun TerminalKeyboardView(
    modifier: Modifier = Modifier,
    settings: KeyboardSettings = KeyboardSettings(),
    snippets: List<String> = emptyList(),
    onTextCommit: (String) -> Unit = {},
    onBackspace: () -> Unit = {},
    onEnter: () -> Unit = {},
    onTab: () -> Unit = {},
    onEsc: () -> Unit = {},
    onArrow: (ArrowDirection) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSwitchKeyboard: () -> Unit = {}
) {
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var isCtrlActive by remember { mutableStateOf(false) }
    var isAltActive by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(KeyboardMode.ALPHA) }

    val primaryColor = Color(settings.themePreset.primaryColorHex)
    val view = LocalView.current

    val triggerHaptic = {
        if (settings.hapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    val handleKeyClick: (String) -> Unit = { char ->
        triggerHaptic()
        if (isCtrlActive && char.length == 1 && char[0].isLetter()) {
            val combo = char.lowercase()
            when (combo) {
                "c" -> onTextCommit("\u0003") // Ctrl+C
                "v" -> onTextCommit("\u0016") // Ctrl+V
                "a" -> onTextCommit("\u0001") // Ctrl+A
                "z" -> onTextCommit("\u001A") // Ctrl+Z
                "x" -> onTextCommit("\u0018") // Ctrl+X
                "l" -> onTextCommit("clear\n") // Ctrl+L
                else -> onTextCommit(char)
            }
            isCtrlActive = false
        } else {
            val textToCommit = when (shiftState) {
                ShiftState.OFF -> char.lowercase()
                ShiftState.ON -> {
                    shiftState = ShiftState.OFF
                    char.uppercase()
                }
                ShiftState.CAPS_LOCK -> char.uppercase()
            }
            onTextCommit(textToCommit)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF121414))
            .border(width = 1.dp, color = Color(0xFF3C4B35), shape = RectangleShape)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .testTag("terminal_keyboard_chassis")
    ) {
        // Snippets and quick macros bar
        if (settings.showSnippetBar && snippets.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(bottom = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .background(Color(0xFF1E2020))
                        .border(1.dp, primaryColor, RectangleShape)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Terminal Snippets",
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CMD",
                            color = primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                snippets.forEach { snippet ->
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .background(Color(0xFF212121))
                            .border(1.dp, Color(0xFF3C4B35), RectangleShape)
                            .clickable {
                                triggerHaptic()
                                onTextCommit(snippet + " ")
                            }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = snippet,
                            color = Color(0xFFE2E2E2),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        if (mode == KeyboardMode.EMOJI) {
            EmojiAndSymbolsPanel(
                primaryColor = primaryColor,
                onSelectSymbol = {
                    triggerHaptic()
                    onTextCommit(it)
                },
                onBack = {
                    triggerHaptic()
                    mode = KeyboardMode.ALPHA
                }
            )
        } else {
            // Row 1: ESC, Numbers 1-0, Backspace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                KeyButton(
                    text = "ESC",
                    isFunctionKey = true,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f),
                    testTag = "key_esc",
                    onClick = {
                        triggerHaptic()
                        onEsc()
                    }
                )
                val numberKeys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                numberKeys.forEach { num ->
                    KeyButton(
                        text = num,
                        modifier = Modifier.weight(1f),
                        testTag = "key_$num",
                        onClick = { handleKeyClick(num) }
                    )
                }
                RepeatableKeyButton(
                    modifier = Modifier.weight(1.2f),
                    testTag = "key_backspace",
                    onClick = {
                        triggerHaptic()
                        onBackspace()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = Color(0xFFE2E2E2),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Row 2: TAB, QWERTY Top, \
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                KeyButton(
                    text = "TAB",
                    isFunctionKey = true,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1.2f),
                    testTag = "key_tab",
                    onClick = {
                        triggerHaptic()
                        onTab()
                    }
                )
                val row2Keys = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
                row2Keys.forEach { key ->
                    val displayKey = when (shiftState) {
                        ShiftState.OFF -> key
                        ShiftState.ON, ShiftState.CAPS_LOCK -> key.uppercase()
                    }
                    KeyButton(
                        text = displayKey,
                        modifier = Modifier.weight(1f),
                        testTag = "key_$key",
                        onClick = { handleKeyClick(key) }
                    )
                }
                KeyButton(
                    text = "\\",
                    modifier = Modifier.weight(1f),
                    testTag = "key_backslash",
                    onClick = { handleKeyClick("\\") }
                )
            }

            // Row 3: CTRL, QWERTY Home, RETURN/ENTER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                KeyButton(
                    text = "CTRL",
                    isFunctionKey = true,
                    isActive = isCtrlActive,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1.5f),
                    testTag = "key_ctrl",
                    onClick = {
                        triggerHaptic()
                        isCtrlActive = !isCtrlActive
                    }
                )
                val row3Keys = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
                row3Keys.forEach { key ->
                    val displayKey = when (shiftState) {
                        ShiftState.OFF -> key
                        ShiftState.ON, ShiftState.CAPS_LOCK -> key.uppercase()
                    }
                    KeyButton(
                        text = displayKey,
                        modifier = Modifier.weight(1f),
                        testTag = "key_$key",
                        onClick = { handleKeyClick(key) }
                    )
                }
                KeyButton(
                    text = "",
                    isPrimaryAction = true,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1.5f),
                    testTag = "key_enter",
                    onClick = {
                        triggerHaptic()
                        onEnter()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = "Enter",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Row 4: SHIFT, QWERTY Bottom, , . ?
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val shiftLabel = when (shiftState) {
                    ShiftState.OFF -> "SHIFT"
                    ShiftState.ON -> "SHIFT▲"
                    ShiftState.CAPS_LOCK -> "CAPS🔒"
                }
                KeyButton(
                    text = shiftLabel,
                    isFunctionKey = true,
                    isActive = shiftState != ShiftState.OFF,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1.5f),
                    testTag = "key_shift",
                    onClick = {
                        triggerHaptic()
                        shiftState = when (shiftState) {
                            ShiftState.OFF -> ShiftState.ON
                            ShiftState.ON -> ShiftState.CAPS_LOCK
                            ShiftState.CAPS_LOCK -> ShiftState.OFF
                        }
                    }
                )
                val row4Keys = listOf("z", "x", "c", "v", "b", "n", "m")
                row4Keys.forEach { key ->
                    val displayKey = when (shiftState) {
                        ShiftState.OFF -> key
                        ShiftState.ON, ShiftState.CAPS_LOCK -> key.uppercase()
                    }
                    KeyButton(
                        text = displayKey,
                        modifier = Modifier.weight(1f),
                        testTag = "key_$key",
                        onClick = { handleKeyClick(key) }
                    )
                }
                KeyButton(
                    text = ",",
                    modifier = Modifier.weight(1f),
                    testTag = "key_comma",
                    onClick = { handleKeyClick(",") }
                )
                KeyButton(
                    text = ".",
                    modifier = Modifier.weight(1f),
                    testTag = "key_dot",
                    onClick = { handleKeyClick(".") }
                )
                KeyButton(
                    text = "?",
                    modifier = Modifier.weight(1.5f),
                    testTag = "key_question",
                    onClick = { handleKeyClick("?") }
                )
            }

            // Row 5: ALT, Emoji, -, SPACE, _, Settings, Navigation Cluster
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                KeyButton(
                    text = "ALT",
                    isFunctionKey = true,
                    isActive = isAltActive,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1.2f),
                    testTag = "key_alt",
                    onClick = {
                        triggerHaptic()
                        isAltActive = !isAltActive
                    }
                )
                KeyButton(
                    text = "",
                    isFunctionKey = true,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f),
                    testTag = "key_emoji",
                    onClick = {
                        triggerHaptic()
                        mode = KeyboardMode.EMOJI
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mood,
                        contentDescription = "Emoji & Symbols",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                KeyButton(
                    text = "-",
                    modifier = Modifier.weight(1f),
                    testTag = "key_dash",
                    onClick = { handleKeyClick("-") }
                )
                KeyButton(
                    text = "",
                    modifier = Modifier.weight(5f),
                    testTag = "key_space",
                    onClick = {
                        triggerHaptic()
                        onTextCommit(" ")
                    }
                )
                KeyButton(
                    text = "_",
                    modifier = Modifier.weight(1f),
                    testTag = "key_underscore",
                    onClick = { handleKeyClick("_") }
                )
                KeyButton(
                    text = "",
                    isFunctionKey = true,
                    primaryColor = primaryColor,
                    modifier = Modifier.weight(1f),
                    testTag = "key_settings",
                    onClick = {
                        triggerHaptic()
                        onOpenSettings()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Arrow Navigation Cluster
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            ArrowMiniButton(
                                icon = Icons.Default.ArrowDropUp,
                                contentDescription = "Arrow Up",
                                modifier = Modifier.weight(1f),
                                testTag = "arrow_up",
                                onClick = {
                                    triggerHaptic()
                                    onArrow(ArrowDirection.UP)
                                }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            ArrowMiniButton(
                                icon = Icons.Default.ArrowLeft,
                                contentDescription = "Arrow Left",
                                modifier = Modifier.weight(1f),
                                testTag = "arrow_left",
                                onClick = {
                                    triggerHaptic()
                                    onArrow(ArrowDirection.LEFT)
                                }
                            )
                            ArrowMiniButton(
                                icon = Icons.Default.ArrowDropDown,
                                contentDescription = "Arrow Down",
                                modifier = Modifier.weight(1f),
                                testTag = "arrow_down",
                                onClick = {
                                    triggerHaptic()
                                    onArrow(ArrowDirection.DOWN)
                                }
                            )
                            ArrowMiniButton(
                                icon = Icons.Default.ArrowRight,
                                contentDescription = "Arrow Right",
                                modifier = Modifier.weight(1f),
                                testTag = "arrow_right",
                                onClick = {
                                    triggerHaptic()
                                    onArrow(ArrowDirection.RIGHT)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    isFunctionKey: Boolean = false,
    isPrimaryAction: Boolean = false,
    isActive: Boolean = false,
    primaryColor: Color = Color(0xFFA2C9FF),
    testTag: String = "",
    onClick: () -> Unit = {},
    customContent: (@Composable () -> Unit)? = null
) {
    val bgColor = when {
        isPrimaryAction -> primaryColor
        isActive -> primaryColor
        isFunctionKey -> Color(0xFF000000)
        else -> Color(0xFF212121)
    }

    val textColor = when {
        isPrimaryAction -> Color.Black
        isActive -> Color.Black
        isFunctionKey -> primaryColor
        else -> Color.White
    }

    val borderModifier = if (isFunctionKey && !isActive) {
        Modifier.border(1.dp, primaryColor, RectangleShape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(bgColor)
            .then(borderModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (customContent != null) {
            customContent()
        } else {
            Text(
                text = text,
                color = textColor,
                fontSize = if (text.length > 3) 11.sp else 16.sp,
                fontWeight = if (isFunctionKey || isPrimaryAction) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RepeatableKeyButton(
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var repeatJob by remember { mutableStateOf<Job?>(null) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF212121))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onClick()
                        repeatJob = scope.launch {
                            delay(400)
                            while (true) {
                                onClick()
                                delay(60)
                            }
                        }
                        tryAwaitRelease()
                        repeatJob?.cancel()
                    }
                )
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ArrowMiniButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF212121))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFFE2E2E2),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun EmojiAndSymbolsPanel(
    primaryColor: Color,
    onSelectSymbol: (String) -> Unit,
    onBack: () -> Unit
) {
    val symbolsAndEmoji = listOf(
        "~", "`", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+",
        "{", "}", "[", "]", "|", ":", ";", "\"", "'", "<", ">", "?", "/", "=",
        "👍", "🚀", "⚡", "🔥", "💻", "🐧", "🤖", "✅", "❌", "💡", "📦", "🔒"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(214.dp)
            .background(Color(0xFF121414))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYMBOLS & EMOJI",
                color = primaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF212121))
                    .border(1.dp, primaryColor, RectangleShape)
                    .clickable { onBack() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to keyboard",
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ABC",
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val rows = symbolsAndEmoji.chunked(10)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rowItems.forEach { sym ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(Color(0xFF212121))
                                .border(1.dp, Color(0xFF3C4B35), RectangleShape)
                                .clickable { onSelectSymbol(sym) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
