package ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smc.slog.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.screen.component.*
import ui.state.LogInfo
import ui.state.LogPanelState
import ui.state.PageInfo
import ui.state.rememberLogPanelState
import ui.theme.logPanelBgColor
import ui.theme.markBgColor
import ui.theme.titleBgColor
import util.countSubstringOccurrences
import java.lang.StringBuilder
import kotlin.math.max


@Composable
fun LogViewPanel(pageInfo: PageInfo) {
    val showHelpDialogStata = rememberShowDialogStata()
    val scope = rememberCoroutineScope()
    val textOperationBarState = rememberTextOperationBarState()
    val logPanelState = rememberLogPanelState(pageInfo)
    val toastState = rememberToastState()
    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            logPanelState.initData()
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(modifier = Modifier.fillMaxSize()) {
            val panelState = rememberVerticalDragPanelState()
            val topFilterLogListState = rememberLazyListState()
            val wholeLogListState = rememberLazyListState()
            val markLogLogListState = rememberLazyListState()

            TextOperationBar(
                textOperationBarState,
                onOperationTextRepeat = {
                    scope.launch {
                        toastState.showToast(getString(Res.string.already_exists))
                    }
                },
                onOperationTextsChange = {
                    val firstVisibleItemLineNumber =
                        logPanelState.getFirstVisibleItemLineNumber(topFilterLogListState.firstVisibleItemIndex)
                    scope.launch(Dispatchers.IO) {
                        logPanelState.changeOperationTextList(it as ArrayList<OperationText>) { hasChange ->
                            if (hasChange) {
                                scope.launch {
                                    topFilterLogListState.scrollToItem(
                                        logPanelState.getIndexByLineNumber(
                                            firstVisibleItemLineNumber
                                        )
                                    )
                                }
                            }
                        }
                    }
                })

            VerticalDragPanel(
                modifier = Modifier.fillMaxWidth().weight(1f),
                state = panelState,
                topPanel = {
                    TopFilterLogContentPanel(
                        logPanelState,
                        topFilterLogListState,
                        onToast = {
                            toastState.showToast(it)
                        },
                        onJump = {
                            scope.launch {
                                val wholeListIndex = logPanelState.findJumpIndex(it, logPanelState.wholeList)
                                if (wholeListIndex != -1) {
                                    wholeLogListState.scrollToItem(wholeListIndex)
                                }
                                val markListIndex = logPanelState.findJumpIndex(it, logPanelState.markList)
                                if (markListIndex != -1) {
                                    markLogLogListState.scrollToItem(markListIndex)
                                }
                                toastState.showToast(getString(Res.string.jump_completed), 1000)
                            }
                        },
                        addOperationText = {
                            if (!textOperationBarState.contains(it)) {
                                textOperationBarState.addOperationText(it)
                            } else {
                                scope.launch {
                                    toastState.showToast(getString(Res.string.already_exists))
                                }
                            }
                        })
                },
                bottomLeftPanel = {
                    WholeLogContentPanel(
                        logPanelState,
                        wholeLogListState,
                        onJump = {
                            scope.launch {
                                val filterListIndex = logPanelState.findJumpIndex(it, logPanelState.filterList)
                                if (filterListIndex != -1) {
                                    topFilterLogListState.scrollToItem(filterListIndex)
                                }
                                toastState.showToast(getString(Res.string.jump_completed), 1000)
                            }
                        },
                        addOperationText = {
                            textOperationBarState.addOperationText(it)
                        },
                        dismiss = { panelState.hideBottomPanel() })
                },
                bottomRightPanel = {
                    MarkLogContentPanel(
                        logPanelState,
                        markLogLogListState,
                        onJump = {
                            scope.launch {
                                val filterListIndex = logPanelState.findJumpIndex(it, logPanelState.filterList)
                                if (filterListIndex != -1) {
                                    topFilterLogListState.scrollToItem(filterListIndex)
                                }
                                toastState.showToast(getString(Res.string.jump_completed), 1000)
                            }
                        },
                        onToast = {
                            toastState.showToast(it)
                        }, addOperationText = {
                            textOperationBarState.addOperationText(it)
                        },
                        dismiss = { panelState.hideBottomPanel() })
                })

            ToolBar(
                modifier = Modifier.fillMaxWidth().height(30.dp).background(Color.DarkGray),
                isShowWholeLog = panelState.getShowPanelType() == ShowPanelType.Left,
                wholeLogBarClick = {
                    if (panelState.getShowPanelType() == ShowPanelType.Left)
                        panelState.hideBottomPanel() else panelState.showLeftBottomPanel()
                },
                isShowMarkLog = panelState.getShowPanelType() == ShowPanelType.Right,
                markLogBarClick = {
                    if (panelState.getShowPanelType() == ShowPanelType.Right)
                        panelState.hideBottomPanel() else panelState.showRightBottomPanel()
                },
                helpBarClick = {
                    showHelpDialogStata.showDialog()
                })
        }
        Toast(toastState)
        HelpDialog(showHelpDialogStata)
    }
}


@Composable
fun TopFilterLogContentPanel(
    logPanelState: LogPanelState,
    lazyListState: LazyListState,
    onToast: (String) -> Unit,
    onJump: (LogInfo) -> Unit,
    addOperationText: (OperationText) -> Unit
) {
    Column {
        var isShowSearchBar by remember { logPanelState.isShowSearchBar }
        val scope = rememberCoroutineScope()

        LaunchedEffect(isShowSearchBar) {
            if (!isShowSearchBar) {
                logPanelState.stopSearch()
            }
        }

        Spacer(modifier = Modifier.height(0.5.dp).fillMaxWidth().background(Color.LightGray))
        Box(
            modifier = Modifier.height(25.dp).fillMaxWidth().background(titleBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(Res.string.operation_log), color = Color.White, fontSize = 11.sp)
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.offset(y = (-3).dp),
                    text = "${logPanelState.filterList.size}",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Image(
                    painter = painterResource(Res.drawable.search),
                    null,
                    modifier = Modifier.width(30.dp).clickable {
                        isShowSearchBar = !isShowSearchBar
                    })
            }
        }
        AnimatedVisibility(isShowSearchBar) {
            SearchBar(
                logPanelState,
                onSearch = {
                    if (it.contains("\n")) {
                        scope.launch {
                            onToast(getString(Res.string.not_supported))
                        }
                        return@SearchBar
                    }
                    scope.launch(Dispatchers.IO) {
                        logPanelState.search(it, lazyListState.firstVisibleItemIndex) { hasResult ->
                            if (hasResult) {
                                scope.launch {
                                    val scrollToIndex = logPanelState.getSearchJumpIndex()
                                    val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                                    if (visibleItems.size > 2) {
                                        if (visibleItems.subList(1, visibleItems.size - 1)
                                                .none { item -> item.index == scrollToIndex }
                                        ) {
                                            lazyListState.scrollToItem(scrollToIndex)
                                        }
                                    } else {
                                        lazyListState.scrollToItem(scrollToIndex)
                                    }
                                }
                            } else {
                                scope.launch {
                                    onToast(getString(Res.string.not_found))
                                }
                            }
                        }
                    }
                },
                findPre = {
                    logPanelState.searchPre()
                    scope.launch {
                        val scrollToIndex = logPanelState.getSearchJumpIndex()
                        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                        if (visibleItems.size > 2) {
                            if (visibleItems.subList(1, visibleItems.size).none { it.index == scrollToIndex }) {
                                val visibleHeight = lazyListState.layoutInfo.viewportSize.height
                                lazyListState.scrollToItem(scrollToIndex, -max(0, visibleHeight / 2))
                            }
                        } else {
                            lazyListState.scrollToItem(scrollToIndex)
                        }
                    }
                },
                findNext = {
                    logPanelState.searchNext()
                    scope.launch {
                        val scrollToIndex = logPanelState.getSearchJumpIndex()
                        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                        if (visibleItems.size > 2) {
                            if (visibleItems.subList(0, visibleItems.size - 1).none { it.index == scrollToIndex }) {
                                lazyListState.scrollToItem(scrollToIndex)
                            }
                        } else {
                            lazyListState.scrollToItem(scrollToIndex)
                        }
                    }
                },
                close = {
                    isShowSearchBar = false
                })
        }
        Box {
            LogTextMenuProvider(
                isShowSearch = true,
                search = {
                    if (it.contains(SP_Placeholders) &&
                        !(it.countSubstringOccurrences(SP_Placeholders) == 1 && it.endsWith(SP_Placeholders))
                    ) {
                        scope.launch {
                            onToast(getString(Res.string.not_supported))
                        }
                        return@LogTextMenuProvider
                    }
                    isShowSearchBar = true
                    scope.launch(Dispatchers.IO) {
                        logPanelState.search(it, lazyListState.firstVisibleItemIndex) { hasResult ->
                            if (hasResult) {
                                scope.launch {
                                    val scrollToIndex = logPanelState.getSearchJumpIndex()
                                    val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                                    if (visibleItems.size > 2) {
                                        if (visibleItems.subList(1, visibleItems.size - 1)
                                                .none { item -> item.index == scrollToIndex }
                                        ) {
                                            lazyListState.scrollToItem(scrollToIndex)
                                        }
                                    } else {
                                        lazyListState.scrollToItem(scrollToIndex)
                                    }
                                }
                            } else {
                                scope.launch {
                                    onToast(getString(Res.string.not_found))
                                }
                            }
                        }
                    }
                },
                addOperationText = addOperationText
            ) {
                LogSelectionContainer {
                    LazyColumn(modifier = Modifier.fillMaxSize().background(logPanelBgColor), state = lazyListState) {
                        items(logPanelState.filterList.size, key = { logPanelState.filterList[it].lineNumber }) {
                            TextItem(
                                logPanelState,
                                logPanelState.filterList[it],
                                onJump = { onJump(logPanelState.filterList[it]) },
                                isShowSearch = true
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(scrollState = lazyListState),
                style = ScrollbarStyle(
                    minimalHeight = 40.dp,
                    thickness = 8.dp,
                    shape = RoundedCornerShape(6.dp),
                    hoverDurationMillis = 100,
                    unhoverColor = Color.White.copy(alpha = 0.5f),
                    hoverColor = Color.White.copy(alpha = 1f)
                )
            )
        }
    }
}

@Composable
fun WholeLogContentPanel(
    logPanelState: LogPanelState,
    lazyListState: LazyListState,
    onJump: (LogInfo) -> Unit,
    dismiss: () -> Unit,
    addOperationText: (OperationText) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(0.5.dp).fillMaxWidth().background(Color.LightGray))
        Box(
            modifier = Modifier.height(25.dp).fillMaxWidth().background(titleBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(Res.string.all_log), color = Color.White, fontSize = 11.sp)
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.offset(y = (-3).dp),
                    text = "${logPanelState.wholeList.size}",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Image(
                    painter = painterResource(Res.drawable.minus),
                    null,
                    modifier = Modifier.width(30.dp).clickable {
                        dismiss()
                    })
            }
        }
        Box {
            LogTextMenuProvider(isShowSearch = false, search = {}, addOperationText = addOperationText) {
                LogSelectionContainer {
                    LazyColumn(modifier = Modifier.fillMaxSize().background(logPanelBgColor), state = lazyListState) {
                        items(logPanelState.wholeList.size, key = { logPanelState.wholeList[it].lineNumber }) {
                            TextItem(
                                logPanelState,
                                logPanelState.wholeList[it],
                                onJump = { onJump(logPanelState.wholeList[it]) })
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(scrollState = lazyListState),
                style = ScrollbarStyle(
                    minimalHeight = 40.dp,
                    thickness = 8.dp,
                    shape = RoundedCornerShape(6.dp),
                    hoverDurationMillis = 100,
                    unhoverColor = Color.White.copy(alpha = 0.5f),
                    hoverColor = Color.White.copy(alpha = 1f)
                )
            )
        }
    }
}

@Composable
fun MarkLogContentPanel(
    logPanelState: LogPanelState,
    lazyListState: LazyListState,
    onJump: (LogInfo) -> Unit,
    onToast: (String) -> Unit,
    dismiss: () -> Unit,
    addOperationText: (OperationText) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(0.5.dp).fillMaxWidth().background(Color.LightGray))
        Box(
            modifier = Modifier.height(25.dp).fillMaxWidth().background(titleBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(Res.string.mark_log), color = Color.White, fontSize = 11.sp)
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.offset(y = (-3).dp),
                    text = "${logPanelState.markList.size}",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Row {
                    val scope = rememberCoroutineScope()
                    val clipboardManager: ClipboardManager = LocalClipboardManager.current
                    Image(
                        painter = painterResource(Res.drawable.clipboard),
                        null,
                        modifier = Modifier.width(30.dp).clickable {
                            if (logPanelState.markList.isNotEmpty()) {
                                val stringBuffer = StringBuilder("")
                                logPanelState.markList.forEach {
                                    stringBuffer.append(
                                        "${it.lineNumber + 1}    ${
                                            it.text.replace(
                                                SP_Placeholders,
                                                ""
                                            )
                                        }\n"
                                    )
                                }
                                clipboardManager.setText(AnnotatedString(stringBuffer.toString()))
                                scope.launch {
                                    onToast(getString(Res.string.copied_to_clipboard))
                                }
                            }
                        })
                    Image(
                        painter = painterResource(Res.drawable.delete_one),
                        null,
                        modifier = Modifier.width(30.dp).clickable {
                            logPanelState.markList.forEach {
                                it.isMark.value = false
                            }
                            logPanelState.markList.clear()
                        })
                    Image(
                        painter = painterResource(Res.drawable.minus),
                        null,
                        modifier = Modifier.width(30.dp).clickable {
                            dismiss()
                        })
                }

            }
        }
        Box {
            LogTextMenuProvider(isShowSearch = false, search = {}, addOperationText = addOperationText) {
                LogSelectionContainer {
                    LazyColumn(modifier = Modifier.fillMaxSize().background(logPanelBgColor), state = lazyListState) {
                        items(logPanelState.markList.size, key = { logPanelState.markList[it].lineNumber }) {
                            TextItem(
                                logPanelState,
                                logPanelState.markList[it],
                                onJump = { onJump(logPanelState.markList[it]) },
                                isShowMarkLogBgColor = false
                            )
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(scrollState = lazyListState),
                style = ScrollbarStyle(
                    minimalHeight = 40.dp,
                    thickness = 8.dp,
                    shape = RoundedCornerShape(6.dp),
                    hoverDurationMillis = 100,
                    unhoverColor = Color.White.copy(alpha = 0.5f),
                    hoverColor = Color.White.copy(alpha = 1f)
                )
            )
        }
    }
}

@Composable
fun SearchBar(
    logPanelState: LogPanelState,
    onSearch: (String) -> Unit,
    findPre: () -> Unit,
    findNext: () -> Unit,
    close: () -> Unit
) {
    Row(
        modifier = Modifier.background(Color(60, 63, 65)).padding(10.dp, 0.dp, 18.dp, 0.dp).fillMaxWidth()
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Image(painterResource(Res.drawable.search), null)

        Spacer(modifier = Modifier.width(10.dp))

        var searchText by remember { logPanelState.searchText }

        CustomTextMenuProvider {
            BasicTextField(
                modifier = Modifier.width(300.dp).height(25.dp).background(Color(69, 73, 74))
                    .padding(top = 5.dp).fillMaxHeight().onKeyEvent {
                        if (it.utf16CodePoint == 10 && it.type == KeyEventType.KeyUp && searchText.isNotEmpty()) {
                            onSearch(searchText)
                        }
                        true
                    }.focusRequester(focusRequester),
                value = searchText,
                onValueChange = {
                    searchText = it.trimEnd('\n')
                },
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, color = Color.White),
                cursorBrush = SolidColor(Color.White)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))

        val index by remember { logPanelState.searchNowIndex }

        Text(
            modifier = Modifier.offset(y = 0.dp).widthIn(30.dp, 150.dp),
            color = Color.White,
            text = if (logPanelState.searchResultList.size == 0) "0" else "${index + 1}",
            fontSize = 15.sp
        )

        Text(
            modifier = Modifier.offset(y = 0.dp),
            color = Color.White,
            text = " / ${logPanelState.searchResultList.size}",
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.width(50.dp))

        Spacer(modifier = Modifier.width(20.dp))

        Image(
            painterResource(Res.drawable.arrow_left),
            null,
            modifier = Modifier.scale(0.8f).rotate(90f).clickable {
                findPre()
            })

        Spacer(modifier = Modifier.width(10.dp))

        Image(
            painterResource(Res.drawable.arrow_right),
            null,
            modifier = Modifier.scale(0.8f).rotate(90f).clickable {
                findNext()
            })

        Spacer(modifier = Modifier.fillMaxWidth().weight(1f))

        Image(painterResource(Res.drawable.close_small), null, modifier = Modifier.clickable {
            close()
        })
    }
}

@Composable
fun ToolBar(
    modifier: Modifier,
    isShowWholeLog: Boolean = false,
    wholeLogBarClick: () -> Unit,
    isShowMarkLog: Boolean = false,
    markLogBarClick: () -> Unit,
    helpBarClick: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(100.dp).fillMaxHeight()
                .background(if (isShowWholeLog) Color.Black else Color.Transparent).clickable {
                    wholeLogBarClick()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            LogText(
                text = stringResource(Res.string.all_log),
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier.width(100.dp).fillMaxHeight()
                .background(if (isShowMarkLog) Color.Black else Color.Transparent).clickable {
                    markLogBarClick()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            LogText(
                text = stringResource(Res.string.mark_log),
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.fillMaxWidth().weight(1f))
        Image(
            painter = painterResource(Res.drawable.help),
            null,
            modifier = Modifier.width(30.dp).clickable {
                helpBarClick()
            }
        )
        Spacer(modifier = Modifier.width(20.dp))
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun TextItem(
    logPanelState: LogPanelState,
    logInfo: LogInfo,
    onJump: () -> Unit,
    isShowMarkLogBgColor: Boolean = true,
    isShowSearch: Boolean = false
) {
    val isMark by remember { logInfo.isMark }
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (isMark && isShowMarkLogBgColor) markBgColor else Color.Transparent)
            .padding(horizontal = 10.dp)
    ) {
        var hover by remember { mutableStateOf(false) }
        var annotatedString by remember { mutableStateOf(AnnotatedString(logInfo.text)) }
        val change by remember { logPanelState.updateVersion }
        LaunchedEffect(change) {
            withContext(Dispatchers.IO) {
                annotatedString = if (isShowSearch) logPanelState.getSearchStyleAnnotatedString(logInfo)
                else logPanelState.getStyleAnnotatedString(logInfo)
            }
        }
        DisableSelection {
            LogText(
                text = (logInfo.lineNumber + 1).toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (hover) Color.Green else Color.LightGray,
                modifier = Modifier.onPointerEvent(PointerEventType.Enter) { hover = true }
                    .onPointerEvent(PointerEventType.Exit) { hover = false }.onClick(
                        onClick = {},
                        onDoubleClick = {
                            onJump()
                        },
                        onLongClick = {
                            logPanelState.markLog(!isMark, logInfo)
                        }
                    ).onPointerEvent(PointerEventType.Press) { pointerEvent ->
                        when {
                            pointerEvent.keyboardModifiers.isMetaPressed -> {
                                logPanelState.markLog(!isMark, logInfo)
                            }

                            pointerEvent.keyboardModifiers.isCtrlPressed -> {
                                logPanelState.markLog(!isMark, logInfo)
                            }
                        }
                    },
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        LogText(
            text = annotatedString,
            fontSize = 11.sp,
            color = Color.LightGray
        )
    }
}