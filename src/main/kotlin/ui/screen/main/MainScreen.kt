package ui.screen.main

import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.*
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.install4j.api.launcher.StartupNotification
import kotlinx.coroutines.launch
import ui.screen.component.DialogPanel
import ui.screen.component.TabBar
import ui.state.MainScreenState
import ui.state.rememberMainScreenState
import ui.theme.logPanelBgColor
import util.AppEvent
import util.AppWindow
import util.UserIntent
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(mainScreenState: MainScreenState = rememberMainScreenState()) {
    val pagerState = rememberPagerState { mainScreenState.getPage().size }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        StartupNotification.registerStartupListener { parameters ->
            AppWindow.window.requestFocus()
            AppWindow.window.toFront()
            if (File(parameters).exists()) {
                mainScreenState.addPageByDragFile(File(parameters)) {
                    if (it) {
                        scope.launch {
                            pagerState.scrollToPage(pagerState.pageCount)
                        }
                    }
                }
            }
        }
    }

    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                if (event.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    val files = event.awtTransferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                    files.filter {
                        !(it as File).isDirectory
                    }.forEach { any ->
                        mainScreenState.addPageByDragFile(any as File) {
                            if (it) {
                                scope.launch {
                                    pagerState.scrollToPage(pagerState.pageCount)
                                }
                            }
                        }
                    }
                }
                return true
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    return@dragAndDropTarget event.action == DragAndDropTransferAction.Move
                }, target = dragAndDropTarget
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabBar(mainScreenState.getPage(), pagerState,
                closePage = {
                    mainScreenState.closePage(it)
                },
                addPageByFile = {
                    mainScreenState.addPageByFile {
                        if (it) {
                            scope.launch {
                                pagerState.scrollToPage(pagerState.pageCount)
                            }
                        }
                    }
                })
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                VerticalPager(
                    modifier = Modifier.fillMaxSize().size(300.dp).background(Color.DarkGray),
                    state = pagerState,
                    beyondViewportPageCount = 10,
                    userScrollEnabled = false
                ) {
                    LogViewPanel(mainScreenState.getPage()[it])
                }
                if (mainScreenState.getPage().isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(logPanelBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("拖拽文件或粘贴内容到此处以查看日志", fontSize = 12.sp, color = Color.LightGray)
                    }
                }
            }
        }

        DialogPanel(mainScreenState.isShow(),
            pasteTipDialogSubmit = {
                mainScreenState.addPageByPaste(clipboardManager.getText().toString()) {
                    if (it)
                        scope.launch {
                            pagerState.scrollToPage(pagerState.pageCount)
                        }
                }
                mainScreenState.hidePasteDialog()
            }, pasteTipDialogDismiss = {
                mainScreenState.hidePasteDialog()
            })
    }

    DisposableEffect(Unit) {
        val notifyListener = object : AppEvent.NotifyListener {
            override fun notify(intent: UserIntent) {
                if (intent == UserIntent.Paste) {
                    mainScreenState.showPasteDialog()
                }
            }
        }
        AppEvent.register(notifyListener)
        onDispose {
            AppEvent.unregister(notifyListener)
        }
    }
}


