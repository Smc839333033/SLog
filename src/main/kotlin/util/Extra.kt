package util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import ui.screen.component.OperationText
import ui.screen.component.OperationType
import java.util.regex.Matcher
import java.util.regex.Pattern

fun hasChineseChar(text: String): Boolean {
    val chineseCharPattern = Regex("[\\u4e00-\\u9fa5]")
    return chineseCharPattern.containsMatchIn(text)
}

fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: String) {
    addStyle(style, text, Regex.fromLiteral(regexp))
}

fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: Regex) {
    for (result in regexp.findAll(text)) {
        addStyle(style, result.range.first, result.range.last + 1)
    }
}

fun isShielded(text: String, shieldOperations: List<OperationText>): Boolean {
    shieldOperations.forEach { opText ->
        if (opText.operationType == OperationType.SHIELD && text.contains(opText.text)) {
            return true
        } else if (opText.operationType == OperationType.SHIELD_REGEXP &&
            Pattern.compile(opText.text).matcher(text).find()
        ) {
            return true
        }
    }
    return false
}


fun match(text: String, regexpOperations: List<OperationText>): Boolean {
    regexpOperations.forEach { opText ->
        if (opText.operationType == OperationType.REGEXP && Pattern.compile(opText.text).matcher(text).find()) {
            return true
        } else if (opText.operationType == OperationType.FILTER && text.contains(opText.text)) {
            return true
        }
    }
    return false
}

fun String.countSubstringOccurrences(substring: String): Int {
    val pattern: Pattern = Pattern.compile(substring)
    val matcher: Matcher = pattern.matcher(this)
    var count = 0
    while (matcher.find()) {
        count++
    }
    return count
}

fun getStringBlocking(stringResource: StringResource) = runBlocking {
    getString(stringResource)
}