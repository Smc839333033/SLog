import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.*
import com.smc.slog.resources.Res
import com.smc.slog.resources.app_name
import com.smc.slog.resources.icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import util.getWindowState
import ui.screen.main.MainScreen

import util.AppEvent
import util.AppWindow
import util.isMacOs
import java.awt.event.WindowEvent
import javax.swing.SwingUtilities


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    application(exitProcessOnExit = false) {
        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides WindowExceptionHandlerFactory { window ->
                WindowExceptionHandler {
                    SwingUtilities.invokeLater {
                        window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
                    }
                }
            }
        ) {
            Window(
                onCloseRequest = ::exitApplication,
                state = getWindowState(),
                title = stringResource(Res.string.app_name),
                icon = if (isMacOs()) null else painterResource(Res.drawable.icon),
                undecorated = false,
                onKeyEvent = {
                    AppEvent.notify(AppEvent.identifyKeyEvent(it))
                    false
                }
            ) {
                AppWindow.window = window
                App()
            }
        }
    }
}

@Composable
internal fun App() = MaterialTheme {
    MainScreen()
}

