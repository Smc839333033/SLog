package ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import util.AppWindow
import java.awt.FileDialog
import java.io.File
import java.util.UUID
import kotlin.math.min

enum class PageType {
    File,
    PasteText
}

data class PageInfo(
    val pageName: String,
    val pageType: PageType = PageType.File,
    val filePath: String? = null,
    val pasteText: String? = null,
    val id: String = UUID.randomUUID().toString()
)

class MainScreenState {
    private val isShowPasteDialog = mutableStateOf(false)

    private val pages = mutableStateListOf<PageInfo>()

    fun isShow() = isShowPasteDialog.value

    fun getPage() = pages

    fun getPageInfo(index: Int) = pages[index]

    fun showPasteDialog() {
        isShowPasteDialog.value = true
    }

    fun hidePasteDialog() {
        isShowPasteDialog.value = false
    }

    fun closePage(pageInfo: PageInfo) {
        pages.remove(pageInfo)
    }

    fun addPageByDragFile(file: File, addResult: (Boolean) -> Unit) {
        addResult(pages.add(PageInfo(pageName = file.name, filePath = file.path)))
    }

    fun addPageByPaste(text: String, addResult: (Boolean) -> Unit) {
        val name = text.substring(IntRange(0, min(20, text.length - 1)))
        addResult(pages.add(PageInfo(pageName = name, pageType = PageType.PasteText, pasteText = text)))
    }

    fun addPageByFile(addResult: (Boolean) -> Unit) {
        openFileChoose { file ->
            addResult(pages.add(PageInfo(pageName = file.name, filePath = file.path)))
        }
    }

    private fun openFileChoose(m: (file: File) -> Unit) {
        val fd = FileDialog(AppWindow.window, "File open", FileDialog.LOAD)
        fd.isVisible = true
        if (fd.file != null) {
            m(File(fd.directory + File.separator + fd.file))
        }
    }
}

@Composable
fun rememberMainScreenState() = remember { MainScreenState() }