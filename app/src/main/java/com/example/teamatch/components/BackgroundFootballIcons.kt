package com.example.teamatch.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.teamatch.R
import kotlin.random.Random
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.example.teamatch.PreferencesManager

@Composable
fun BackgroundFootballIcons(
    preferencesManager: PreferencesManager
) {
    val iconSize = 80.dp
    val random = Random(System.currentTimeMillis())
    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val iconTintColor = if (isDarkTheme) Color(0xFF74BBFB) else Color.Black

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(6) {
            val xOffset = random.nextInt(0, 400).dp
            val yOffset = random.nextInt(0, 800).dp
            val rotationAngle = random.nextFloat() * 360f

            Image(
                painter = painterResource(id = R.drawable.ic_football),
                contentDescription = null,
                colorFilter = ColorFilter.tint(iconTintColor),
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = xOffset, y = yOffset)
                    .rotate(rotationAngle)
                    .alpha(0.2f)
            )
        }
    }
}
