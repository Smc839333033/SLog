package ui.screen.component

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import ui.theme.textSelectionColors

@Composable
fun LogSelectionContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
        SelectionContainer {
            content()
        }
    }
}

