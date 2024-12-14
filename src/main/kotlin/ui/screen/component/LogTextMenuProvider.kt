package ui.screen.component

import androidx.compose.foundation.*
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.foundation.text.TextContextMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString



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
                val sp = "      "
                val items = {
                    listOfNotNull(
                        textManager.cut?.let {
                            ContextMenuItem("${sp}剪切", it)
                        },
                        textManager.copy?.let {
                            ContextMenuItem("${sp}复制", it)
                        },
                        textManager.paste?.let {
                            ContextMenuItem("${sp}粘贴", it)
                        },
                        textManager.selectAll?.let {
                            ContextMenuItem("${sp}全选", it)
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogTextMenuProvider(
    isShowSearch: Boolean = false,
    search: (String) -> Unit,
    addOperationText: (OperationText) -> Unit,
    content: @Composable () -> Unit
) {
    val sp = "      "
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
                            ContextMenuItem("${sp}剪切", it)
                        },
                        textManager.paste?.let {
                            ContextMenuItem("${sp}粘贴", it)
                        },
                        textManager.selectAll?.let {
                            ContextMenuItem("${sp}全选", it)
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
                val clipboardManager = LocalClipboardManager.current
                ContextMenuDataProvider({
                    val text = textManager.selectedText.text
                    if (text == "") {
                        return@ContextMenuDataProvider emptyList()
                    }

                    val list = mutableListOf<ContextMenuItem>()
                    list.add(
                        ContextMenuItem("${sp}复制") {
                            clipboardManager.setText(
                                AnnotatedString(
                                    if (text.contains(SP_Placeholders))
                                        text.replace(SP_Placeholders, SP_Enter) else text
                                )
                            )
                        })
                    if (isShowSearch) {
                        list.add(ContextMenuItem("${sp}搜索") {
                            search(text)
                        })
                    }
                    list.addAll(
                        listOf(
                            ContextMenuItem("${sp}筛选") {
                                addOperationText(OperationText(text, OperationType.FILTER))
                            },
                            ContextMenuItem("${sp}屏蔽") {
                                addOperationText(OperationText(text, OperationType.SHIELD))
                            },
                            ContextMenuItem("${sp}高亮") {
                                addOperationText(OperationText(text, OperationType.HIGHLIGHT))
                            },
                        )
                    )
                    list
                }) {
                    textMenu.Area(textManager, state, content = content)
                }
            }
        },
        content = content
    )
}