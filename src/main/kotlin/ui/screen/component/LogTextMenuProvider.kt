package ui.screen.component

import androidx.compose.foundation.*
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.foundation.text.TextContextMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.smc.slog.resources.*
import util.getStringBlocking


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
                            ContextMenuItem(getStringBlocking(Res.string.cut), it)
                        },
                        textManager.copy?.let {
                            ContextMenuItem(getStringBlocking(Res.string.copy), it)
                        },
                        textManager.paste?.let {
                            ContextMenuItem(getStringBlocking(Res.string.paste), it)
                        },
                        textManager.selectAll?.let {
                            ContextMenuItem(getStringBlocking(Res.string.select_all), it)
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
                            ContextMenuItem(getStringBlocking(Res.string.cut), it)
                        },
                        textManager.paste?.let {
                            ContextMenuItem(getStringBlocking(Res.string.paste), it)
                        },
                        textManager.selectAll?.let {
                            ContextMenuItem(getStringBlocking(Res.string.select_all), it)
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
                        ContextMenuItem(getStringBlocking(Res.string.copy)) {
                            clipboardManager.setText(
                                AnnotatedString(
                                    if (text.contains(SP_Placeholders))
                                        text.replace(SP_Placeholders, SP_Enter) else text
                                )
                            )
                        })
                    if (isShowSearch) {
                        list.add(ContextMenuItem(getStringBlocking(Res.string.search)) {
                            search(text)
                        })
                    }
                    list.addAll(
                        listOf(
                            ContextMenuItem(getStringBlocking(Res.string.filter)) {
                                addOperationText(OperationText(text, OperationType.FILTER))
                            },
                            ContextMenuItem(getStringBlocking(Res.string.shield)) {
                                addOperationText(OperationText(text, OperationType.SHIELD))
                            },
                            ContextMenuItem(getStringBlocking(Res.string.highlight)) {
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