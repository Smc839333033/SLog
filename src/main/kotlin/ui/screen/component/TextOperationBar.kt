package ui.screen.component

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.foundation.text.TextContextMenu
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ui.theme.DefaultFilterTextColorList
import ui.theme.operationTextBarBgColor
import util.hasChineseChar
import kotlin.random.Random

enum class OperationType {
    SHIELD_REGEXP,
    SHIELD,
    REGEXP,
    FILTER,
    HIGHLIGHT
}

data class OperationText(
    val text: String,
    var operationType: OperationType = OperationType.FILTER,
    var isSelect: MutableState<Boolean> = mutableStateOf(true),
    var textColor: Color = Color.Gray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OperationText

        return text == other.text
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun toString(): String {
        return "OperationText(text='$text', operationType=$operationType, isSelect=${isSelect.value}, textColor=${textColor.value})"
    }
}

data class DropMenuItem(
    val operationType: OperationType,
    val iconPath: String,
    val text: String
)

val DropMenuItemList = listOf(
    DropMenuItem(OperationType.FILTER, "image/preview-open.svg", "字符筛选"),
    DropMenuItem(OperationType.SHIELD, "image/preview-close.svg", "字符屏蔽"),
    DropMenuItem(OperationType.HIGHLIGHT, "image/dome-light.svg", "字符高亮"),
    DropMenuItem(OperationType.REGEXP, "image/file-conversion-one.svg", "正则筛选"),
    DropMenuItem(OperationType.SHIELD_REGEXP, "image/file-failed-one.svg", "正则屏蔽")
)

class TextOperationBarState {
    private val operationTexts = mutableStateListOf<OperationText>()
    private val textColors = DefaultFilterTextColorList.shuffled()
    private var notify: (MutableList<OperationText>) -> Unit = { }

    fun getOperationTextByIndex(index: Int): OperationText {
        return operationTexts[index]
    }

    fun addOperationText(operationText: OperationText) {
        operationTexts.add(operationText.apply {
            textColor = if (operationTexts.size < textColors.size) {
                textColors[operationTexts.size]
            } else {
                textColors[Random.nextInt(textColors.size)]
            }
        })
        notify(getOperationTextsChangeList(getOperationTexts()))
    }

    fun removeOperationTextAt(index: Int) {
        operationTexts.removeAt(index)
        notify(getOperationTextsChangeList(getOperationTexts()))
    }

    fun changeSelectState(index: Int, isSelect: Boolean) {
        operationTexts[index].isSelect.value = isSelect
        notify(getOperationTextsChangeList(getOperationTexts()))
    }

    fun contains(operationText: OperationText): Boolean {
        return operationTexts.contains(operationText)
    }

    fun getOperationTexts() = operationTexts.toMutableList()

    fun operationTextsSize() = operationTexts.size

    fun setOperationTextsChangeNotify(notify: (MutableList<OperationText>) -> Unit) {
        this.notify = notify
    }
}

@Composable
fun rememberTextOperationBarState() = remember {
    TextOperationBarState()
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextOperationBar(
    textOperationBarState: TextOperationBarState = rememberTextOperationBarState(),
    onOperationTextsChange: (MutableList<OperationText>) -> Unit,
    onOperationTextRepeat: () -> Unit
) {
    LaunchedEffect(Unit) {
        textOperationBarState.setOperationTextsChangeNotify(onOperationTextsChange)
    }
    Row(
        modifier = Modifier.height(30.dp).fillMaxWidth().background(operationTextBarBgColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InputOperationTextField { operationText ->
            if (!textOperationBarState.contains(operationText)) {
                textOperationBarState.addOperationText(operationText)
            } else {
                onOperationTextRepeat()
            }
        }

        val rowScrollState = rememberScrollState()
        val scope = rememberCoroutineScope()
        var isShowScrollBar by remember { mutableStateOf(false) }

        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.fillMaxWidth().onPointerEvent(PointerEventType.Enter) { isShowScrollBar = true }
            .onPointerEvent(PointerEventType.Exit) { isShowScrollBar = false }.onPointerEvent(PointerEventType.Scroll) {
                scope.launch {
                    rowScrollState.scrollTo((rowScrollState.value + (it.changes.first().scrollDelta.y).toInt() * 20))
                }
            }) {
            Row(
                modifier = Modifier.fillMaxSize().horizontalScroll(rowScrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(textOperationBarState.operationTextsSize()) { index ->
                    OperationTextItem(textOperationBarState.getOperationTextByIndex(index),
                        onSelect = {
                            textOperationBarState.changeSelectState(index, it)
                        }, deleteItem = {
                            textOperationBarState.removeOperationTextAt(index)
                        })
                }
            }

            if (isShowScrollBar) {
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    adapter = rememberScrollbarAdapter(scrollState = rowScrollState),
                    style = ScrollbarStyle(
                        minimalHeight = 40.dp,
                        thickness = 3.dp,
                        shape = RoundedCornerShape(6.dp),
                        hoverDurationMillis = 100,
                        unhoverColor = Color.White.copy(alpha = 0.5f),
                        hoverColor = Color.White.copy(alpha = 1f)
                    )
                )
            }
        }
    }
}


@Composable
fun InputOperationTextField(operationTextReceiver: (OperationText) -> Unit) {
    Row(
        modifier = Modifier.height(30.dp).wrapContentWidth().padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        var isAllowInput by remember { mutableStateOf(false) }
        var operationType by remember { mutableStateOf(OperationType.FILTER) }

        Image(
            painter = if (!isAllowInput) painterResource("image/plus.svg") else painterResource("image/left.svg"),
            null,
            modifier = Modifier.align(Alignment.CenterVertically).clickable {
                isAllowInput = !isAllowInput
            }
        )

        Spacer(Modifier.width(5.dp))
        AnimatedVisibility(visible = !isAllowInput) {
            LogText(
                "文本处理",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                modifier = Modifier.clickable {
                    isAllowInput = !isAllowInput
                }
            )
        }
        AnimatedVisibility(visible = isAllowInput) {

            var showMenu by remember { mutableStateOf(false) }

            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            if (showMenu) {
                DropdownMenu(expanded = true, onDismissRequest = {
                    showMenu = false
                }, modifier = Modifier.background(Color.DarkGray), offset = DpOffset((-10).dp, 10.dp)) {
                    DropMenuItemList.forEach {
                        DropdownMenuItem(onClick = {
                            operationType = it.operationType
                            showMenu = false
                        }) {
                            Image(painter = painterResource(it.iconPath), contentDescription = "")
                            LogText(
                                text = it.text,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 10.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            }


            Box(contentAlignment = Alignment.Center, modifier = Modifier.width(260.dp).fillMaxHeight().clickable {
                showMenu = !showMenu
            }) {
                var inputText by remember { mutableStateOf("") }
                Row(modifier = Modifier.height(30.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(getImagePathByOperationType(operationType)),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        modifier = Modifier.widthIn(10.dp, 150.dp).padding(bottom = 3.5.dp),
                        text = getTextByOperationType(operationType),
                        color = Color.White,
                        fontSize = 10.sp,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    CustomTextMenuProvider {
                        BasicTextField(
                            modifier = Modifier.width(200.dp).background(color = Color.White)
                                .align(Alignment.CenterVertically)
                                .padding(top = 5.dp).height(20.dp).onKeyEvent {
                                    if (it.utf16CodePoint == 10 && it.type == KeyEventType.KeyUp && inputText.isNotEmpty()) {
                                        operationTextReceiver(
                                            OperationText(
                                                text = inputText,
                                                operationType = operationType
                                            )
                                        )
                                        isAllowInput = !isAllowInput
                                        inputText = ""
                                    }
                                    true
                                }.focusRequester(focusRequester),
                            value = inputText,
                            onValueChange = {
                                inputText = it
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 15.sp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OperationTextItem(operationText: OperationText, onSelect: (Boolean) -> Unit, deleteItem: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Spacer(Modifier.width(8.dp))
        Checkbox(
            modifier = Modifier.scale(0.8f).padding(bottom = 2.5.dp),
            checked = operationText.isSelect.value,
            onCheckedChange = { check ->
                onSelect(check)
            }, colors = CheckboxDefaults.colors(uncheckedColor = Color.White)
        )
        Image(
            painterResource(
                getImagePathByOperationType(operationText.operationType)
            ),
            contentDescription = null,
            modifier = Modifier.scale(0.9f).offset(x = (-10).dp)
        )
        TooltipArea(
            tooltip = {
                Surface(
                    modifier = Modifier.shadow(4.dp),
                    color = Color.DarkGray,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    LogText(
                        text = operationText.text,
                        modifier = Modifier.padding(10.dp),
                        color = Color.White
                    )
                }
            },
            delayMillis = 600,
            tooltipPlacement = TooltipPlacement.CursorPoint(
                alignment = Alignment.BottomEnd,
                offset = DpOffset((-16).dp, 30.dp)
            )
        ) {
            LogSelectionContainer {
                LogText(
                    modifier = Modifier.widthIn(10.dp, 150.dp),
                    text = operationText.text,
                    color = operationText.textColor,
                    fontSize = 12.sp,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    style = LocalTextStyle.current.copy(baselineShift = BaselineShift(if (hasChineseChar(operationText.text)) -0.1f else -0.3f))
                )
            }
        }
        Spacer(Modifier.width(5.dp))
        Image(
            painter = painterResource("image/delete-one.svg"),
            null,
            modifier = Modifier.scale(0.8f).clickable {
                deleteItem()
            }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomTextMenuProvider(content: @Composable () -> Unit) {
    val textMenu = staticCompositionLocalOf {
        object : TextContextMenu {
            @Composable
            override fun Area(
                textManager: TextContextMenu.TextManager,
                state: ContextMenuState,
                content: @Composable () -> Unit
            ) {
                val items = {
                    listOfNotNull(
                        textManager.cut?.let {
                            ContextMenuItem("剪切", it)
                        },
                        textManager.copy?.let {
                            ContextMenuItem("复制", it)
                        },
                        textManager.paste?.let {
                            ContextMenuItem("粘贴", it)
                        },
                        textManager.selectAll?.let {
                            ContextMenuItem("全选", it)
                        },
                    )
                }
                ContextMenuArea(items, state, content = content)
            }
        }
    }.current
    CompositionLocalProvider(
        LocalTextContextMenu provides object : TextContextMenu {
            @Composable
            override fun Area(
                textManager: TextContextMenu.TextManager,
                state: ContextMenuState,
                content: @Composable () -> Unit
            ) {
                ContextMenuDataProvider({
                    emptyList()
                }) {
                    textMenu.Area(textManager, state, content = content)
                }
            }
        },
        content = content
    )
}

private fun getImagePathByOperationType(operationType: OperationType) = when (operationType) {
    OperationType.FILTER -> {
        "image/preview-open.svg"
    }

    OperationType.SHIELD -> {
        "image/preview-close.svg"
    }

    OperationType.HIGHLIGHT -> {
        "image/dome-light.svg"
    }

    OperationType.REGEXP -> {
        "image/file-conversion-one.svg"
    }

    else -> {
        "image/file-failed-one.svg"
    }
}

private fun getTextByOperationType(operationType: OperationType) = when (operationType) {
    OperationType.FILTER -> {
        "字符筛选"
    }

    OperationType.SHIELD -> {
        "字符屏蔽"
    }

    OperationType.HIGHLIGHT -> {
        "字符高亮"
    }

    OperationType.REGEXP -> {
        "正则筛选"
    }

    else -> {
        "正则屏蔽"
    }
}

private fun getOperationTextsChangeList(list: List<OperationText>) =
    list.filter { it.isSelect.value }.sortedBy { it.text.length }.reversed().toMutableList()

fun java.util.ArrayList<OperationText>.isSameList(arrayList: ArrayList<OperationText>): Boolean =
    this.size == arrayList.size && arrayList.containsAll(this)

fun List<OperationText>.isSameList(arrayList: List<OperationText>): Boolean =
    this.size == arrayList.size && arrayList.containsAll(this)




