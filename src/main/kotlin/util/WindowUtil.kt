package util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import java.awt.Toolkit
import java.util.ArrayList
import javax.swing.JFrame

object AppWindow {
    var window = JFrame()
}

enum class UserIntent {
    Paste,
    Null
}

@Composable
fun getWindowState(): WindowState {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val screenHeight = screenSize.height - 300
    val screenWidth = screenSize.width - 500
    return rememberWindowState(
        position = WindowPosition(Alignment.Center),
        size = DpSize(screenWidth.dp, screenHeight.dp)
    )
}


object AppEvent {
    private val notifyListeners = ArrayList<NotifyListener>()
    fun identifyKeyEvent(keyEvent: KeyEvent): UserIntent {
        if (keyEvent.isMetaPressed && keyEvent.key == Key.V && keyEvent.type == KeyEventType.KeyUp) {
            return UserIntent.Paste
        }

        if (!isMacOs() && keyEvent.isCtrlPressed && keyEvent.key == Key.V && keyEvent.type == KeyEventType.KeyUp) {
            return UserIntent.Paste
        }
        return UserIntent.Null
    }

    fun register(listener: NotifyListener) {
        notifyListeners.add(listener)
    }

    fun unregister(listener: NotifyListener) {
        notifyListeners.remove(listener)
    }

    fun notify(intent: UserIntent) {
        notifyListeners.forEach {
            it.notify(intent)
        }
    }

    interface NotifyListener {
        fun notify(intent: UserIntent)
    }
}


fun isMacOs(): Boolean {
    val osName = System.getProperty("os.name")
    return osName != null && osName.startsWith("Mac")
}