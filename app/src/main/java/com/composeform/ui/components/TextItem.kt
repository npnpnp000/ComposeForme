package com.composeform.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import com.composeform.utils.extensions.WindowInfo
import com.composeform.utils.extensions.rememberWidowInfo

@Composable
fun TextItem(text: String) {
    val widowInfo = rememberWidowInfo()
     val modifir = getModifierByScreenInfo(widowInfo)
    val style = getStyleByScreenInfo(widowInfo)
    AutoResizedText(
        text = text,
        color = Color.Black,
        textAlign = TextAlign.Center,
        modifier = modifir.fillMaxSize(),
        style = style

    )
}

@Composable
fun AutoResizedText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = style.background,
    textAlign: TextAlign
) {
    var resizedTextStyle by remember {
        mutableStateOf (style)
    }
    var shouldDraw by remember {
        mutableStateOf(false)
    }

    val defaultFontSize = MaterialTheme.typography.bodyLarge.fontSize

    Text(
        text = text,
        textAlign = textAlign,
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        softWrap = true,
        style = resizedTextStyle,
        color = color,
        onTextLayout = { result ->
            if (result.didOverflowWidth) {
                if (style.fontSize.isUnspecified) {
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = defaultFontSize
                    )
                }
                resizedTextStyle = resizedTextStyle.copy(
                    fontSize = resizedTextStyle.fontSize * 0.95
                )
            } else {
                shouldDraw = true
            }
        }
    )
}

@Composable
private fun getStyleByScreenInfo(widowInfo: WindowInfo): TextStyle {
    if (widowInfo.screenHeightInfo is WindowInfo.WindowType.Compact ||
        widowInfo.screenWithInfo is WindowInfo.WindowType.Expended){
        return MaterialTheme.typography.bodyLarge
    }else{
        return MaterialTheme.typography.bodySmall
    }
}
private fun getModifierByScreenInfo(widowInfo: WindowInfo) :Modifier{
    if (widowInfo.screenHeightInfo is WindowInfo.WindowType.Compact ||
        widowInfo.screenWithInfo is WindowInfo.WindowType.Expended){
        return Modifier
            .padding(5.dp)
            .width(200.dp)
    }else{
        return Modifier
            .padding(5.dp)
            .width(100.dp)
    }
}
