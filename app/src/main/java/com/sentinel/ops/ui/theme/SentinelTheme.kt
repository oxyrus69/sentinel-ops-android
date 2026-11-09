package com.sentinel.ops.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy950 = Color(0xFF08111F)
val Navy900 = Color(0xFF0D1828)
val Navy850 = Color(0xFF122133)
val Slate400 = Color(0xFF91A2B9)
val Slate200 = Color(0xFFD7E0EC)
val SentinelTeal = Color(0xFF27C2B8)
val SentinelBlue = Color(0xFF5AA7FF)
val CriticalRed = Color(0xFFFF6B6B)
val WarningAmber = Color(0xFFF4B85A)
val HealthyGreen = Color(0xFF5DCC96)

private val DarkColors = darkColorScheme(
    primary = SentinelTeal,
    onPrimary = Navy950,
    secondary = SentinelBlue,
    background = Navy950,
    onBackground = Slate200,
    surface = Navy900,
    onSurface = Slate200,
    surfaceVariant = Navy850,
    onSurfaceVariant = Slate400,
    outline = Color(0xFF2A3B50),
    error = CriticalRed,
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF087F78),
    secondary = Color(0xFF2869A8),
    background = Color(0xFFF4F7FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFEAF0F5),
    outline = Color(0xFFCAD4DF),
)

@Composable
fun SentinelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
