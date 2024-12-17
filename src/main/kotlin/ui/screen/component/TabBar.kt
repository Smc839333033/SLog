package ui.screen.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smc.slog.resources.Res
import com.smc.slog.resources.add_file
import com.smc.slog.resources.close_small
import com.smc.slog.resources.plus
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.state.PageInfo


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TabBar(
    pageInfoList: List<PageInfo>,
    pagerState: PagerState,
    closePage: (PageInfo) -> Unit,
    addPageByFile: () -> Unit
) {
    val rowScrollState = rememberScrollState()
    var isShowScrollBar by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.onPointerEvent(PointerEventType.Enter) { isShowScrollBar = true }
        .onPointerEvent(PointerEventType.Exit) { isShowScrollBar = false }.onPointerEvent(PointerEventType.Scroll) {
            scope.launch {
                rowScrollState.scrollTo((rowScrollState.value + (it.changes.first().scrollDelta.y).toInt() * 20))
            }
        }) {

        Row(
            modifier = Modifier.height(30.dp).fillMaxWidth().background(Color.DarkGray)
                .horizontalScroll(rowScrollState)
        ) {
            pageInfoList.forEachIndexed { index, pageInfo ->
                TabBarItem(
                    isSelect = pagerState.currentPage == index,
                    text = pageInfo.pageName,
                    filePath = pageInfo.filePath,
                    action = {
                        scope.launch {
                            pagerState.scrollToPage(index)
                        }
                    }, iconAction = {
                        closePage(pageInfo)
                    }
                )
            }

            TabAddFileBarItem {
                addPageByFile()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabBarItem(
    isSelect: Boolean,
    text: String,
    textSize: TextUnit = 12.sp,
    filePath: String?,
    action: () -> Unit,
    iconAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxHeight()
            .background(if (isSelect) Color.Black else Color.Gray).clickable {
                action()
            }.border(border = BorderStroke(1.dp, Color.DarkGray)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.width(10.dp))
        Image(painter = painterResource(Res.drawable.close_small), null, modifier = Modifier.clickable {
            iconAction()
        })
        Spacer(Modifier.width(5.dp))
        TooltipArea(
            modifier = Modifier.fillMaxSize(),
            tooltip = {
                filePath?.let {
                    Surface(
                        modifier = Modifier.shadow(4.dp),
                        color = Color.DarkGray,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = filePath,
                            modifier = Modifier.padding(10.dp),
                            color = Color.White
                        )
                    }
                }
            },
            delayMillis = 600,
            tooltipPlacement = TooltipPlacement.CursorPoint(
                alignment = Alignment.BottomEnd,
                offset = DpOffset((-16).dp, 30.dp)
            )
        ) {
            Text(
                text,
                color = Color.White,
                fontSize = textSize,
                modifier = Modifier.widthIn(80.dp, 320.dp).padding(end = 15.dp),
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}


@Composable
fun TabAddFileBarItem(
    action: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxHeight()
            .clickable {
                action()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(10.dp))
        Image(painter = painterResource(Res.drawable.plus), null)
        Spacer(Modifier.width(5.dp))
        Text(
            stringResource(Res.string.add_file),
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.widthIn(80.dp, 320.dp).padding(end = 15.dp, bottom = 3.dp),
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}