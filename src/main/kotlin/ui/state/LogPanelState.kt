package ui.state

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.screen.component.OperationText
import ui.screen.component.OperationType
import ui.screen.component.SP_Placeholders
import ui.screen.component.isSameList
import util.addStyle
import util.isShielded
import util.match
import java.io.File
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

data class LogPanelState(
    val pageInfo: PageInfo,
    val scope: CoroutineScope,

    val updateVersion: MutableState<Int> = mutableStateOf(0),
    val isParsing: MutableState<Boolean> = mutableStateOf(false),

    var operationTextList: ArrayList<OperationText> = ArrayList(),
    var highlightOperationList: List<OperationText> = ArrayList(),
    val wholeList: SnapshotStateList<LogInfo> = mutableStateListOf(),
    val filterList: SnapshotStateList<LogInfo> = mutableStateListOf(),
    val markList: SnapshotStateList<LogInfo> = mutableStateListOf(),

    val isShowSearchBar: MutableState<Boolean> = mutableStateOf(false),
    val searchText: MutableState<String> = mutableStateOf(""),
    val searchResultList: SnapshotStateList<SearchTextInfo> = mutableStateListOf(),
    var searchNowIndex: MutableState<Int> = mutableStateOf(-1)
) {
    fun initData() {
        scope.launch(Dispatchers.IO) {
            when (pageInfo.pageType) {
                PageType.File -> {
                    readFile()
                }

                PageType.PasteText -> {
                    readPasteText()
                }
            }
        }
    }

    private fun readFile() {
        pageInfo.filePath?.let {
            isParsing.value = true
            val file = File(it)
            if (!file.exists()) {
                return
            }
            val inputStream: InputStream = file.inputStream()
            val originalList: ArrayList<LogInfo> = ArrayList()
            inputStream.bufferedReader().useLines { lines ->
                lines.forEachIndexed { index, s ->
                    originalList.add(
                        LogInfo(
                            lineNumber = index,
                            text = if ("" != s) s + SP_Placeholders else s
                        )
                    )
                }
            }
            filterList.addAll(originalList)
            wholeList.addAll(originalList)
            isParsing.value = false
        }
    }

    private fun readPasteText() {
        this.pageInfo.pasteText?.let {
            isParsing.value = true
            val inputStream: InputStream = it.byteInputStream()
            val originalList: ArrayList<LogInfo> = ArrayList()
            inputStream.bufferedReader().useLines { lines ->
                lines.forEachIndexed { index, s ->
                    originalList.add(
                        LogInfo(
                            lineNumber = index,
                            text = if ("" != s) s + SP_Placeholders else s
                        )
                    )
                }
            }
            filterList.addAll(originalList)
            wholeList.addAll(originalList)
            isParsing.value = false
        }
    }

    fun search(text: String, result: (Boolean) -> Unit) {
        if (filterList.isEmpty()) return
        searchText.value = text
        scope.launch(Dispatchers.IO) {
            val resultList = ArrayList<SearchTextInfo>()
            repeat(filterList.size) {
                val logInfo = filterList[it]
                for (matchResult in Regex.fromLiteral(text).findAll(logInfo.text)) {
                    resultList.add(
                        SearchTextInfo(
                            lineNumber = logInfo.lineNumber,
                            listIndexNumber = it,
                            index = resultList.size,
                            matchResult.range.first,
                            matchResult.range.last + 1,
                            logInfo
                        )
                    )
                }
            }
            if (resultList.isNotEmpty()) {
                searchResultList.clear()
                searchResultList.addAll(resultList)
                searchNowIndex.value = 0
                updateVersion.value++
                result(true)
            } else {
                result(false)
            }
        }
    }

    fun searchPre() {
        searchNowIndex.value = max(0, searchNowIndex.value - 1)
        updateVersion.value++
    }

    fun searchNext() {
        searchNowIndex.value = min(searchResultList.size - 1, searchNowIndex.value + 1)
        updateVersion.value++
    }

    fun stopSearch() {
        searchResultList.clear()
        searchNowIndex.value = -1
        searchText.value = ""
        updateVersion.value++
    }

    fun getSearchJumpIndex(): Int {
        return searchResultList[searchNowIndex.value].listIndexNumber
    }

    fun getFirstVisibleItemLineNumber(index: Int): Int {
        if (index >= 0 && index < filterList.size) {
            return filterList[index].lineNumber
        }
        return -1
    }

    fun getIndexByLineNumber(lineNumber: Int): Int {
        filterList.forEachIndexed { index, logInfo ->
            if (logInfo.lineNumber > lineNumber) {
                return max(index - 1, 0)
            }
        }
        return 0
    }

    fun markLog(isMark: Boolean, logInfo: LogInfo) {
        if (!isMark) {
            markList.remove(logInfo)
        } else if (!markList.contains(logInfo)) {
            markList.add(logInfo)
            markList.sortBy { it.lineNumber }
        }
        logInfo.isMark.value = isMark
    }

    fun findJumpIndex(needFindLogInfo: LogInfo, list: List<LogInfo>): Int {
        for ((index, logInfo) in list.withIndex()) {
            if (logInfo.lineNumber == needFindLogInfo.lineNumber) {
                return index
            }
            if (logInfo.lineNumber > needFindLogInfo.lineNumber) {
                return max(0, index - 1)
            }
        }
        return -1
    }

    fun changeOperationTextList(list: ArrayList<OperationText>, hasChange: (Boolean) -> Unit) {
        if (!operationTextList.isSameList(list)) {
            this.operationTextList = list
            scope.launch(Dispatchers.IO) {
                delay(if (isParsing.value) 500 else 0)
                isShowSearchBar.value = false
                parseOperation(hasChange)
            }
        }
    }

    private var lastFilterOperations: List<OperationText> = ArrayList()
    private var lastShieldOperations: List<OperationText> = ArrayList()
    private fun parseOperation(hasChange: (Boolean) -> Unit) {
        val startTime = System.currentTimeMillis()
        var hasChangeFlag = false
        isParsing.value = true
        highlightOperationList =
            operationTextList.filter { it.operationType == OperationType.HIGHLIGHT || it.operationType == OperationType.FILTER }
        val filterOperations =
            operationTextList.filter { it.operationType == OperationType.FILTER || it.operationType == OperationType.REGEXP }
        val shieldOperations =
            operationTextList.filter { it.operationType == OperationType.SHIELD || it.operationType == OperationType.SHIELD_REGEXP }

        if (!lastFilterOperations.isSameList(filterOperations) || !lastShieldOperations.isSameList(shieldOperations)) {
            filterList.clear()
            filterList.addAll(
                parseOperationText(
                    wholeList,
                    shieldOperations,
                    filterOperations
                )
            )
            lastFilterOperations = filterOperations
            lastShieldOperations = shieldOperations
            hasChangeFlag = true
        }
        updateVersion.value++
        isParsing.value = false
        hasChange(hasChangeFlag)
        println("耗时：${System.currentTimeMillis() - startTime}")
    }

    private fun parseOperationText(
        list: List<LogInfo>,
        shieldOperations: List<OperationText>,
        filterOperations: List<OperationText>
    ): List<LogInfo> {
        val filterList = ArrayList<LogInfo>()
        list.forEach { logInfo ->
            if (!isShielded(logInfo.text, shieldOperations)) {
                if (filterOperations.isEmpty()) {
                    filterList.add(logInfo)
                } else if (match(logInfo.text, filterOperations)) {
                    filterList.add(logInfo)
                }
            }
        }
        return filterList
    }

    fun getSearchStyleAnnotatedString(
        logInfo: LogInfo
    ): AnnotatedString {
        return buildAnnotatedString {
            append(logInfo.text)
            highlightOperationList.forEach { operationText ->
                addStyle(SpanStyle(operationText.textColor), logInfo.text, operationText.text)
            }
            searchResultList.filter {
                it.lineNumber == logInfo.lineNumber
            }.forEach {
                if (searchNowIndex.value >= 0 && it == searchResultList[searchNowIndex.value]) {
                    addStyle(
                        SpanStyle(color = Color.Black, background = Color.Yellow),
                        it.firstIndex,
                        it.endIndex
                    )
                } else {
                    addStyle(
                        SpanStyle(color = Color(169, 183, 198), background = Color(50, 89, 61)),
                        it.firstIndex,
                        it.endIndex
                    )
                }
            }
        }
    }

    fun getStyleAnnotatedString(
        logInfo: LogInfo
    ): AnnotatedString {
        return buildAnnotatedString {
            append(logInfo.text)
            highlightOperationList.forEach { operationText ->
                addStyle(SpanStyle(operationText.textColor), logInfo.text, operationText.text)
            }
        }
    }
}


data class LogInfo(
    var lineNumber: Int = 0,
    var searchDraw: Boolean = false,
    var isRegex: Boolean = false,
    var regexLineColor: Color? = null,
    var isMark: MutableState<Boolean> = mutableStateOf(false),
    var text: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogInfo

        if (lineNumber != other.lineNumber) return false
        if (isMark != other.isMark) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lineNumber
        result = 31 * result + isMark.hashCode()
        return result
    }

    override fun toString(): String {
        return "LogInfo(line=$lineNumber, isMark=$isMark, text=$text)"
    }
}

data class SearchTextInfo(
    var lineNumber: Int,
    var listIndexNumber: Int,
    var index: Int,
    var firstIndex: Int = 0,
    var endIndex: Int = 0,
    var logInfo: LogInfo,
) {


    override fun toString(): String {
        return "SearchTextBean(firstIndex=$firstIndex, endIndex=$endIndex)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchTextInfo

        if (lineNumber != other.lineNumber) return false
        if (firstIndex != other.firstIndex) return false
        if (endIndex != other.endIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lineNumber
        result = 31 * result + firstIndex
        result = 31 * result + endIndex
        return result
    }
}

@Composable
fun rememberLogPanelState(pageInfo: PageInfo, scope: CoroutineScope) = remember {
    LogPanelState(pageInfo, scope)
}