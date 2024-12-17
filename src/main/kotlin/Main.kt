import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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


fun main() = application {
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

@Composable
internal fun App() = MaterialTheme {
    MainScreen()
}

