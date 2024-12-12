package ui.screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class ShowDialogStata(val isShowDialog: Boolean = false) {
    val isShowDialogState = mutableStateOf(false)

    fun isShow() = isShowDialogState.value

    fun showDialog() {
        isShowDialogState.value = true
    }

    fun hideDialog() {
        isShowDialogState.value = false
    }
}

@Composable
fun rememberShowDialogStata() = remember { ShowDialogStata() }.apply {
    isShowDialogState.value = isShowDialog
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HelpDialog(showDialogStata: ShowDialogStata = rememberShowDialogStata()) {
    AnimatedVisibility(showDialogStata.isShow(),enter = scaleIn(), exit = scaleOut()) {
        Box(
            modifier = Modifier.fillMaxSize().onClick(onClick = { showDialogStata.hideDialog() }),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.width(400.dp).height(320.dp)
                    .background(Color(232, 232, 233), shape = RoundedCornerShape(10.dp)).verticalScroll(
                        rememberScrollState()
                    ).onClick { }
            ) {

                Spacer(Modifier.height(30.dp))

                Image(
                    painterResource("image/icon.png"),
                    null,
                    modifier = Modifier.size(60.dp).offset(x = 20.dp)
                )

                Spacer(Modifier.height(30.dp))
                SelectionContainer {
                    Text(
                        modifier = Modifier.padding(20.dp, 0.dp),
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            ) {
                                append("帮助：")
                            }
                            withStyle(style = SpanStyle(color = Color.Blue)) {
                                append("\n1、")
                            }
                            append("支持直接粘贴文本查阅日志")

                            withStyle(style = SpanStyle(color = Color.Blue)) {
                                append("\n2、")
                            }
                            append("支持同时拖拽多个文件到此打开")

                            withStyle(style = SpanStyle(color = Color.Blue)) {
                                append("\n3、")
                            }
                            append("双击行号进行跨面板跳转")

                            withStyle(style = SpanStyle(color = Color.Blue)) {
                                append("\n4、")
                            }
                            append("长按或（ ⌘ + 单击）行号对日志进行标记或取消")

                            withStyle(style = SpanStyle(color = Color.Blue)) {
                                append("\n5、")
                            }
                            append("文本处理：\n")
                            append("        字符屏蔽：不显示包含该文本的日志行\n        字符筛选：只显示包含该文本的日志行\n        字符高亮：对该文本进行突出，不做筛选或屏蔽处理\n        正则筛选：对该文本进行正则匹配，筛选出符合正则的行号\n        正则屏蔽：对该文本进行正则匹配，符合正则的行号不显示")
                        },
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                    )
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}