package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SnippetRepository
import com.example.keyboard.ArrowDirection
import com.example.keyboard.KeyboardSettings
import com.example.keyboard.TerminalKeyboardView
import com.example.keyboard.TerminalThemePreset
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var repository: SnippetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = SnippetRepository(applicationContext)

        setContent {
            val settings by repository.settings.collectAsState()
            val snippets by repository.snippets.collectAsState()

            MyApplicationTheme {
                MainScreen(
                    repository = repository,
                    settings = settings,
                    snippets = snippets
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    repository: SnippetRepository,
    settings: KeyboardSettings,
    snippets: List<String>
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) }
    var isKeyboardEnabledInSystem by remember { mutableStateOf(false) }

    val checkKeyboardStatus = {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledMethods = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_INPUT_METHODS
        ) ?: ""
        isKeyboardEnabledInSystem = enabledMethods.contains(context.packageName)
    }

    LaunchedEffect(Unit) {
        checkKeyboardStatus()
    }

    val primaryColor = Color(settings.themePreset.primaryColorHex)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121414)),
        containerColor = Color(0xFF121414),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .background(Color(0xFF121414))
                    .border(width = 1.dp, color = Color(0xFF3C4B35), shape = RectangleShape)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (isKeyboardEnabledInSystem) Color(0xFF79FF5B) else Color(0xFFFFB800), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TERMINAL KEYBOARD",
                            color = primaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "v1.0",
                        color = Color(0xFFBACCB0),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Status Banner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2020)),
                    shape = RectangleShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isKeyboardEnabledInSystem) Color(0xFF3C4B35) else Color(0xFFFFB800), RectangleShape)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isKeyboardEnabledInSystem) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Status",
                                tint = if (isKeyboardEnabledInSystem) Color(0xFF79FF5B) else Color(0xFFFFB800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isKeyboardEnabledInSystem) "System Keyboard Enabled" else "Keyboard Not Enabled",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (isKeyboardEnabledInSystem) "Ready for system-wide use" else "Tap Enable to activate in Android Settings",
                                    color = Color(0xFFBACCB0),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!isKeyboardEnabledInSystem) {
                                Button(
                                    onClick = {
                                        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RectangleShape,
                                    modifier = Modifier.testTag("btn_enable_keyboard")
                                ) {
                                    Text("ENABLE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.showInputMethodPicker()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RectangleShape,
                                    modifier = Modifier.testTag("btn_switch_keyboard")
                                ) {
                                    Text("SWITCH", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color(0xFF121414),
                    contentColor = primaryColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = primaryColor
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = "Terminal", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("TERMINAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, contentDescription = "Snippets", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SNIPPETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .background(Color(0xFF121414))
        ) {
            when (activeTab) {
                0 -> LiveTerminalCanvasTab(settings = settings, snippets = snippets, primaryColor = primaryColor)
                1 -> SnippetManagerTab(repository = repository, snippets = snippets, primaryColor = primaryColor)
                2 -> SettingsTab(repository = repository, settings = settings, primaryColor = primaryColor)
            }
        }
    }
}

@Composable
fun LiveTerminalCanvasTab(
    settings: KeyboardSettings,
    snippets: List<String>,
    primaryColor: Color
) {
    var inputText by remember { mutableStateOf("") }
    val logs = remember { mutableStateListOf(
        "Welcome to Terminal Keyboard Interactive Tester!",
        "System: Android Linux 6.1.0-aistudio x86_64",
        "Type shell commands below or test ESC, Tab, Ctrl & Arrow keys."
    ) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Output Console Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0C0F0F))
                .border(1.dp, Color(0xFF3C4B35), RectangleShape)
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        color = if (log.startsWith("user@android")) Color(0xFF79FF5B) else Color(0xFFA2C9FF),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input prompt line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2020))
                .border(1.dp, primaryColor, RectangleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "user@android:~$ ",
                color = Color(0xFF79FF5B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_terminal_prompt"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        logs.add("user@android:~$ $inputText")
                        when (inputText.trim()) {
                            "clear" -> logs.clear()
                            "date" -> logs.add("Output: " + java.util.Date().toString())
                            "help" -> logs.add("Available test commands: clear, date, help, uname, whoami")
                            "uname" -> logs.add("Linux android 6.1.0-aistudio-arm64")
                            "whoami" -> logs.add("root")
                            else -> logs.add("Executed: $inputText (OK)")
                        }
                        inputText = ""
                    }
                },
                modifier = Modifier.testTag("btn_send_cmd")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Run Command", tint = primaryColor)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "LIVE IN-APP KEYBOARD PREVIEW",
            color = Color(0xFFBACCB0),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Embedded Interactive Keyboard
        TerminalKeyboardView(
            settings = settings,
            snippets = snippets,
            onTextCommit = { text ->
                inputText += text
            },
            onBackspace = {
                if (inputText.isNotEmpty()) {
                    inputText = inputText.dropLast(1)
                }
            },
            onEnter = {
                if (inputText.isNotBlank()) {
                    logs.add("user@android:~$ $inputText")
                    if (inputText.trim() == "clear") {
                        logs.clear()
                    } else {
                        logs.add("Executed: $inputText (OK)")
                    }
                    inputText = ""
                }
            },
            onTab = {
                inputText += "    "
            },
            onEsc = {
                logs.add("System: ESC key triggered!")
            },
            onArrow = { dir ->
                logs.add("Cursor Navigation: $dir")
            }
        )
    }
}

@Composable
fun SnippetManagerTab(
    repository: SnippetRepository,
    snippets: List<String>,
    primaryColor: Color
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newSnippetText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "QUICK TERMINAL MACROS",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Custom shell commands visible on top keyboard bar",
                    color = Color(0xFFBACCB0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RectangleShape,
                modifier = Modifier.testTag("btn_add_snippet")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(snippets) { snippet ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E2020))
                        .border(1.dp, Color(0xFF3C4B35), RectangleShape)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = "Snippet", tint = primaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = snippet,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = { repository.removeSnippet(snippet) },
                            modifier = Modifier.testTag("btn_delete_$snippet")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFFB4AB))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { repository.resetSnippets() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
            shape = RectangleShape,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF3C4B35), RectangleShape)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFFBACCB0))
            Spacer(modifier = Modifier.width(6.dp))
            Text("RESET DEFAULT MACROS", color = Color(0xFFBACCB0), fontFamily = FontFamily.Monospace)
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Add Quick Macro", fontFamily = FontFamily.Monospace, color = Color.White)
            },
            text = {
                Column {
                    Text("Enter command or text macro:", color = Color(0xFFBACCB0), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSnippetText,
                        onValueChange = { newSnippetText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFF3C4B35)
                        ),
                        placeholder = { Text("e.g. git commit -m \"\"", color = Color.Gray, fontFamily = FontFamily.Monospace) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSnippetText.isNotBlank()) {
                            repository.addSnippet(newSnippetText)
                            newSnippetText = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RectangleShape
                ) {
                    Text("SAVE", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = Color(0xFF1E2020)
        )
    }
}

@Composable
fun SettingsTab(
    repository: SnippetRepository,
    settings: KeyboardSettings,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "KEYBOARD CONFIGURATION",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Haptic Feedback Toggle
        SettingCard(
            title = "Haptic Vibration Feedback",
            subtitle = "Tactile vibration click on every key press",
            primaryColor = primaryColor
        ) {
            Switch(
                checked = settings.hapticFeedback,
                onCheckedChange = { repository.updateHaptic(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = primaryColor
                ),
                modifier = Modifier.testTag("switch_haptic")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Snippet Bar Toggle
        SettingCard(
            title = "Top Macro Snippet Bar",
            subtitle = "Show terminal commands row above keys",
            primaryColor = primaryColor
        ) {
            Switch(
                checked = settings.showSnippetBar,
                onCheckedChange = { repository.updateShowSnippetBar(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = primaryColor
                ),
                modifier = Modifier.testTag("switch_snippet_bar")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "THEME PRESETS",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TerminalThemePreset.entries.forEach { preset ->
            val isSelected = settings.themePreset == preset
            val presetColor = Color(preset.primaryColorHex)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(if (isSelected) Color(0xFF1E2020) else Color(0xFF121414))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) presetColor else Color(0xFF3C4B35),
                        shape = RectangleShape
                    )
                    .clickable { repository.updateTheme(preset) }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(presetColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = preset.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = preset.id.uppercase(),
                                color = Color(0xFFBACCB0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = presetColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    primaryColor: Color,
    control: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E2020))
            .border(1.dp, Color(0xFF3C4B35), RectangleShape)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFBACCB0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            control()
        }
    }
}
