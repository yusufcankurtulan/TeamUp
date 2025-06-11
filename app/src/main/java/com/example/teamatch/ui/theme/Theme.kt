package com.example.teamatch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF1E1E1E),
    onBackground = Color.White,
    primary = Color(0xFF74BBFB),
    onPrimary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    background = Color.White,
    onBackground = Color(0xFF1C1C1C),

    primary = Color(0xFF74BBFB),
    onPrimary = Color.Black,

    surface = Color.White,
    onSurface = Color(0xFF333333),

    secondary = Color(0xFFEEEEEE),
    onSecondary = Color.Black,

    outline = Color(0xFFD0D5DC),
    error = Color(0xFFB00020),
    onError = Color.White,
)


    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */


@Composable
fun TeamatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        typography = Typography,
        content = content,
        colorScheme = colors,
        shapes = Shapes(),
    )
}