package ui.theme

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString


val DefaultFilterTextColorList = listOf(
    Color.Red,
    Color.Yellow,
    Color.Cyan,
    Color.Magenta,
    Color.Green,
    Color(33, 150, 255, 255),
    Color(239, 83, 80, 255),
    Color(221, 173, 0, 223),
    Color(255, 64, 129, 255),
    Color(251, 140, 0, 255),
    Color(201, 79, 79, 255),
    Color(120, 130, 254, 255),
    Color(220, 253, 139, 255)
)

val titleBgColor = Color(43, 45, 48)
val logPanelBgColor = Color(30, 31, 34)
val operationTextBarBgColor = Color(61, 50, 35)
val markBgColor = Color(41, 65, 127)
val textSelectionColors = TextSelectionColors(Color(33, 66, 131), Color(33, 66, 131))

@Preview
@Composable
fun Preview() {
    LazyColumn(Modifier.fillMaxSize().background(logPanelBgColor)) {
        items(DefaultFilterTextColorList.size) {
            Text(text = buildAnnotatedString {
                append("${it + 1}  10-29 14:05:02.870 12187 12246 D HWUI    : SkiaOpenGLPipeline::setSurface: this=0xb4000077692b2400, surface=NULL")
                addStyle(SpanStyle(color = DefaultFilterTextColorList[it]), 5, 20)
            }, color = LightGray)
        }
    }
}