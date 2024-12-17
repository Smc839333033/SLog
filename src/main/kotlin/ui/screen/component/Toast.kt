package ui.screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smc.slog.resources.Res
import com.smc.slog.resources.icon
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.painterResource


class ToastState {
    var text: String = ""

    private val scope = CoroutineScope(Job())

    private val isShowToast = mutableStateOf(false)
    fun showToast(text: String, delayTime: Long = 2000L) {
        this.text = text
        isShowToast.value = true
        scope.launch(Dispatchers.IO) {
            delay(delayTime)
            isShowToast.value = false
        }
    }

    fun isShow() = isShowToast.value
}

@Composable
fun rememberToastState() = remember {
    ToastState()
}

@Composable
fun Toast(toastState: ToastState = rememberToastState()) {
    AnimatedVisibility(visible = toastState.isShow(), enter = scaleIn(), exit = scaleOut()) {
        Row(
            modifier = Modifier.height(200.dp).width(500.dp).padding(85.dp)
                .background(Color(232, 232, 233), shape = RoundedCornerShape(30.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(painterResource(Res.drawable.icon), null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = toastState.text, color = Color.Black, fontSize = 12.sp)
        }
    }
}