package ui.screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ui.theme.logPanelBgColor
import java.awt.Cursor

enum class ShowPanelType {
    Left,
    Right,
    None
}

data class VerticalDragPanelState(
    var showPanelType: MutableState<ShowPanelType> = mutableStateOf(ShowPanelType.None),
) {
    fun getShowPanelType() = showPanelType.value

    fun hideBottomPanel() {
        this.showPanelType.value = ShowPanelType.None
    }

    fun showLeftBottomPanel() {
        this.showPanelType.value = ShowPanelType.Left
    }

    fun showRightBottomPanel() {
        this.showPanelType.value = ShowPanelType.Right
    }
}

@Composable
fun rememberVerticalDragPanelState() = remember { VerticalDragPanelState() }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalDragPanel(
    modifier: Modifier,
    state: VerticalDragPanelState = rememberVerticalDragPanelState(),
    topPanel: @Composable () -> Unit,
    bottomLeftPanel: @Composable () -> Unit,
    bottomRightPanel: @Composable () -> Unit
) {
    Column(modifier.background(logPanelBgColor)) {
        val defaultHeight = remember { 300 }
        val minHeight = remember { 60 }

        var height by remember { mutableStateOf(300) }
        val topBoxOffset = remember { mutableStateOf(Offset(0f, 0f)) }
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            topPanel()
        }
        AnimatedVisibility(visible = state.getShowPanelType() != ShowPanelType.None) {
            Box(modifier = Modifier.height(height.dp).fillMaxWidth()) {
                Spacer(Modifier.height(1.dp).fillMaxWidth().background(Color.LightGray))
                Row {
                    AnimatedVisibility(visible = state.getShowPanelType() == ShowPanelType.Left) {
                        bottomLeftPanel()
                    }
                    AnimatedVisibility(
                        visible = state.getShowPanelType() == ShowPanelType.Right,
                        enter = EnterTransition.None
                    ) {
                        bottomRightPanel()
                    }
                }

                Spacer(modifier = Modifier.height(3.dp).fillMaxWidth().pointerHoverIcon(
                    PointerIcon(
                        Cursor(Cursor.S_RESIZE_CURSOR)
                    )
                ).pointerInput(Unit) {
                    detectDragGestures(
                        matcher = PointerMatcher.Primary
                    ) {
                        topBoxOffset.value += it
                        height =
                            if (defaultHeight - ((topBoxOffset.value.y) / 2) < minHeight) minHeight else (defaultHeight - ((topBoxOffset.value.y) / 2)).toInt()
                    }
                })
            }
        }
    }
}