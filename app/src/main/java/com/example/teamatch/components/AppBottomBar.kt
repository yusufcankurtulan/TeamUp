package com.example.teamatch.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.teamatch.PreferencesManager
import com.example.teamatch.navigation.BottomBarItem

@Composable
fun AppBottomBar(
    selectedItem: BottomBarItem,
    preferencesManager: PreferencesManager,
    onItemSelected: (BottomBarItem) -> Unit,
    hasPendingRequests: Boolean = false,
    showBackground: Boolean = true
) {
    val items = listOf(
        BottomBarItem.Profile,
        BottomBarItem.MatchHistory,
        BottomBarItem.Home,
        BottomBarItem.RequestInbox,
        BottomBarItem.Settings
    )
    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val barBackground = if (isDarkTheme) Color(0xFF141414) else Color(0xFFF2F2F2)
    val barStrokeColor = if (isDarkTheme) Color.Transparent else Color.LightGray.copy(alpha = 0.4f)
    val iconDefaultColor = Color(0xFF74BBFB)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        if (showBackground) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 5.dp)
                    .fillMaxWidth()
                    .height(85.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(barBackground)
                    .border(
                        width = 1.dp,
                        color = barStrokeColor,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                val transition by animateFloatAsState(
                    targetValue = if (isSelected) 1.5f else 1f,
                    label = "IconScale"
                )
                val iconOffsetY by animateDpAsState(
                    targetValue = if (isSelected) (-25).dp else 0.dp,
                    label = "IconYPosition"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = iconOffsetY)
                            .size(if (isSelected) 60.dp else 48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF74BBFB) else Color.Transparent
                            )
                            .clickable { onItemSelected(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) Color.White else iconDefaultColor,
                            modifier = Modifier.size(30.dp * transition)
                        )
                    }

                    if (item == BottomBarItem.RequestInbox && hasPendingRequests) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-4).dp)
                                .background(Color.Red, shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}
