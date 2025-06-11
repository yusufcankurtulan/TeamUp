package com.example.teamatch.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.data.User
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun PlayerRatingScreen(
    matchId: String,
    navController: NavController,
    preferencesManager: PreferencesManager
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var participants by remember { mutableStateOf<List<User>>(emptyList()) }

    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val backgroundColor = MaterialTheme.colorScheme.background
    val backIconColor = if (isDarkTheme) Color(0xFF74BBFB) else Color.Black


    LaunchedEffect(matchId) {
        val matchDoc = db.collection("matches").document(matchId).get().await()
        val participantIds = (matchDoc["participants"] as? List<String> ?: emptyList()).toMutableList()

        val creatorId = matchDoc.getString("creatorId")
        if (creatorId != null && !participantIds.contains(creatorId)) {
            participantIds.add(creatorId)
        }

        participants = participantIds
            .filter { it != currentUserId }
            .mapNotNull { uid ->
                val userSnap = db.collection("users").document(uid).get().await()
                userSnap.toObject(User::class.java)
            }

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        BackgroundFootballIcons(preferencesManager = preferencesManager)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = getTranslatedString(R.string.back),
                    tint = backIconColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getTranslatedString(R.string.rate_team_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (participants.isEmpty()) {
                Text(
                    text = getTranslatedString(R.string.no_teammates_found),
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                participants.forEach { user ->
                    val lightOverlay = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                navController.navigate("rate_player/${user.uid}/$matchId")
                            },
                        colors = CardDefaults.cardColors(containerColor = lightOverlay),
                        shape = RoundedCornerShape(12.dp)
                    )
                    {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${user.name} ${user.surname}",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = getTranslatedString(
                                    R.string.player_rating_info,
                                    user.position,
                                    user.rating.toInt()
                                ),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
