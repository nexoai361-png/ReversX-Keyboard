package com.example.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.SnippetRepository
import com.example.ui.theme.MyApplicationTheme

class TerminalInputMethodService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    private lateinit var repository: SnippetRepository

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        repository = SnippetRepository(applicationContext)
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@TerminalInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@TerminalInputMethodService)
            setViewTreeViewModelStoreOwner(this@TerminalInputMethodService)

            setContent {
                val settings by repository.settings.collectAsState()
                val snippets by repository.snippets.collectAsState()

                MyApplicationTheme {
                    TerminalKeyboardView(
                        settings = settings,
                        snippets = snippets,
                        onTextCommit = { text ->
                            currentInputConnection?.commitText(text, 1)
                        },
                        onBackspace = {
                            val ic = currentInputConnection ?: return@TerminalKeyboardView
                            val selectedText = ic.getSelectedText(0)
                            if (selectedText.isNullOrEmpty()) {
                                ic.deleteSurroundingText(1, 0)
                            } else {
                                ic.commitText("", 1)
                            }
                        },
                        onEnter = {
                            val ic = currentInputConnection ?: return@TerminalKeyboardView
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                        },
                        onTab = {
                            val ic = currentInputConnection ?: return@TerminalKeyboardView
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB))
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB))
                        },
                        onEsc = {
                            val ic = currentInputConnection ?: return@TerminalKeyboardView
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE))
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE))
                        },
                        onArrow = { direction ->
                            val ic = currentInputConnection ?: return@TerminalKeyboardView
                            val keyCode = when (direction) {
                                ArrowDirection.UP -> KeyEvent.KEYCODE_DPAD_UP
                                ArrowDirection.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
                                ArrowDirection.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
                                ArrowDirection.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
                            }
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
                        },
                        onOpenSettings = {
                            val intent = Intent(this@TerminalInputMethodService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }

        return composeView
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}
