package com.example.teamatch.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomBarItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    Home("Ana Sayfa", Icons.Default.Home, "home/{district}"),
    Profile("Profil", Icons.Default.Person, "profile"),
    Settings("Ayarlar", Icons.Default.Settings, "settings"),
    MatchHistory("Geçmiş Maçlar", Icons.Default.History, "match_result"),
    RequestInbox("İstekler", Icons.Default.MailOutline, "request_inbox")

}