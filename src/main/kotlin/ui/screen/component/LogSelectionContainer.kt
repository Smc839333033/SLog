package ui.screen.component

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import ui.theme.textSelectionColors

const val SP_Placeholders = " \t\t "
const val SP_Enter = "\n"

@Composable
fun LogSelectionContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
        SelectionContainer(modifier = modifier.onKeyEvent {
            if (it.type == KeyEventType.KeyUp && (it.key == Key.CtrlLeft || it.key == Key.CtrlRight)) {
                if (clipboardManager.hasText()) {
                    clipboardManager.getText()?.text?.let { text ->
                        if (text.contains(SP_Placeholders)) {
                            clipboardManager.setText(AnnotatedString(text.replace(SP_Placeholders, SP_Enter)))
                        }
                    }
                }
            }
            false
        }) {
            content()
        }
    }
}

