import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import util.getWindowState
import ui.screen.main.MainScreen

import util.AppEvent
import util.AppWindow
import util.isMacOs


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = getWindowState(),
        title = "SLog",
        icon = if (isMacOs()) null else painterResource("image/icon.png"),
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

