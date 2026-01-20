package com.composeform.utils.extensions


import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.composeform.dependency_injection.ViewModelModule


inline fun <reified VM : ViewModel> ComponentActivity.provideViewModel() =
    ViewModelModule.provideViewModel<VM>(this)


@Composable
fun rememberWidowInfo(): WindowInfo{
    val configuration = LocalConfiguration.current
    return WindowInfo(
        screenHeightInfo = when {
            configuration.screenWidthDp < 600 -> WindowInfo.WindowType.Compact
            configuration.screenWidthDp < 840 -> WindowInfo.WindowType.Medium
            else -> WindowInfo.WindowType.Expended
        },
        screenWithInfo = when {
            configuration.screenHeightDp < 480 -> WindowInfo.WindowType.Compact
            configuration.screenHeightDp < 900 -> WindowInfo.WindowType.Medium
            else -> WindowInfo.WindowType.Expended},
        screenWith = configuration.screenWidthDp.dp,
        screenHeight = configuration.screenHeightDp.dp
    )
}

data class WindowInfo(
    val screenWithInfo: WindowType,
    val screenHeightInfo: WindowType,
    val screenWith: Dp,
    val screenHeight: Dp
){
    sealed class WindowType{
        object Compact: WindowType()
        object Medium: WindowType()
        object Expended: WindowType()
    }
}

