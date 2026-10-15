package com.rahab.dragonfly.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal700,
    onPrimary = Color.White,
    primaryContainer = Teal50,
    onPrimaryContainer = Teal900,
    secondary = Coral500,
    onSecondary = Color.White,
    secondaryContainer = Coral100,
    onSecondaryContainer = Ink900,
    background = Mist50,
    onBackground = Ink900,
    surface = Color.White,
    onSurface = Ink900,
    surfaceVariant = Mist100,
    onSurfaceVariant = Ink600,
    outline = Ink400,
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = Teal500,
    onPrimary = Teal900,
    primaryContainer = Teal900,
    onPrimaryContainer = Teal200,
    secondary = Coral400,
    onSecondary = Ink900,
    background = Ink900,
    onBackground = Mist50,
    surface = Color(0xFF292524),
    onSurface = Mist50,
    error = Color(0xFFF87171),
)

/**
 * 应用主题入口。
 *
 * 当前强制使用浅色主题：页面背景为手写的浅色，若跟随系统深色，
 * Material3 组件（输入框文字、占位符）会反白导致不可见。
 *
 * @author Rahab
 */
@Composable
fun DragonflyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DragonflyTypography,
        content = content,
    )
}
