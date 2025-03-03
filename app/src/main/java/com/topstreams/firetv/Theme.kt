package com.topstreams.firetv

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5252),
    onPrimary = Color.White,
    secondary = Color(0xFF8B0000),
    onSecondary = Color.White,
    background = Color(0xFF0C1015),
    onBackground = Color.White,
    surface = Color(0xFF222632),
    onSurface = Color.White
)

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF5252),
    onPrimary = Color.White,
    secondary = Color(0xFF8B0000),
    onSecondary = Color.White,
    background = Color(0xFFEEEEEE),
    onBackground = Color(0xFF121212),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF121212)
)

@Composable
fun PorkStreamsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Extension colors for custom elements
object AppColors {
    val cardDark = Color(0xFF222632)
    val cardLight = Color(0xFFFFFFFF)
    val liveGameDark = Color(0xFF8B0000)
    val liveGameLight = Color(0xFFB71C1C)
    val liveIndicator = Color.Red
    val selectedBorder = Color.Red
    val drawerBackgroundDark = Color(0xFF0A0D12)
    val drawerBackgroundLight = Color(0xFFE5E5E5)
}