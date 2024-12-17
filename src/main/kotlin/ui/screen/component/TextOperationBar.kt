package ui.screen.component

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smc.slog.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
}

data class DropMenuItem(
    val operationType: OperationType,
    val drawableResource: DrawableResource,
    val stringResource: StringResource
)

val DropMenuItemList = listOf(
    DropMenuItem(OperationType.FILTER, Res.drawable.preview_open, Res.string.text_filter),
    DropMenuItem(OperationType.SHIELD, Res.drawable.preview_close, Res.string.text_shield),
    DropMenuItem(OperationType.HIGHLIGHT, Res.drawable.dome_light, Res.string.text_highlight),
    DropMenuItem(OperationType.REGEXP, Res.drawable.file_conversion_one, Res.string.rex_filter),
    DropMenuItem(OperationType.SHIELD_REGEXP, Res.drawable.file_failed_one, Res.string.rex_shield)
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

    private fun getOperationTexts() = operationTexts.toMutableList()

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
                    OperationTextItem(
                        textOperationBarState.getOperationTextByIndex(index),
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
            painter = if (!isAllowInput) painterResource(Res.drawable.plus) else painterResource(Res.drawable.left),
            null,
            modifier = Modifier.align(Alignment.CenterVertically).clickable {
                isAllowInput = !isAllowInput
            }
        )

        Spacer(Modifier.width(5.dp))
        AnimatedVisibility(visible = !isAllowInput) {
            LogText(
                stringResource(Res.string.text_operation),
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
                            Image(painter = painterResource(it.drawableResource), contentDescription = "")
                            LogText(
                                text = stringResource(it.stringResource),
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
                        text = stringResource(getTextByOperationType(operationType)),
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
                                inputText = it.trimEnd('\n')
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
            painter = painterResource(Res.drawable.delete_one),
            null,
            modifier = Modifier.scale(0.8f).clickable {
                deleteItem()
            }
        )
    }
}


private fun getImagePathByOperationType(operationType: OperationType) = when (operationType) {
    OperationType.FILTER -> {
        Res.drawable.preview_open
    }

    OperationType.SHIELD -> {
        Res.drawable.preview_close
    }

    OperationType.HIGHLIGHT -> {
        Res.drawable.dome_light
    }

    OperationType.REGEXP -> {
        Res.drawable.file_conversion_one
    }

    else -> {
        Res.drawable.file_failed_one
    }
}

private fun getTextByOperationType(operationType: OperationType) = when (operationType) {
    OperationType.FILTER -> {
        Res.string.text_filter
    }

    OperationType.SHIELD -> {
        Res.string.text_shield
    }

    OperationType.HIGHLIGHT -> {
        Res.string.text_highlight
    }

    OperationType.REGEXP -> {
        Res.string.rex_filter
    }

    else -> {
        Res.string.rex_shield
    }
}

private fun getOperationTextsChangeList(list: List<OperationText>) =
    list.filter { it.isSelect.value }.sortedBy { it.text.length }.reversed().toMutableList()

fun java.util.ArrayList<OperationText>.isSameList(arrayList: ArrayList<OperationText>): Boolean =
    this.size == arrayList.size && arrayList.containsAll(this)

fun List<OperationText>.isSameList(arrayList: List<OperationText>): Boolean =
    this.size == arrayList.size && arrayList.containsAll(this)




