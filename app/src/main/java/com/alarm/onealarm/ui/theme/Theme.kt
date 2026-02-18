package com.alarm.onealarm.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors
import androidx.compose.ui.graphics.Color

private val WearColors = Colors(
    primary = Color(0xFF80CBC4),
    primaryVariant = Color(0xFF4DB6AC),
    secondary = Color(0xFFFFAB40),
    background = Color.Black,
    surface = Color(0xFF1A1A1A),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun AlarmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearColors,
        content = content
    )
}
