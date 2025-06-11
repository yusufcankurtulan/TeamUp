package com.example.teamatch.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.data.Match
import com.example.teamatch.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri

@Composable
fun MatchDetailScreen(
    matchId: String,
    preferencesManager: PreferencesManager,
    navController: NavController
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var match by remember { mutableStateOf<Match?>(null) }
    var participants by remember { mutableStateOf<List<User>>(emptyList()) }
    var isParticipant by remember { mutableStateOf(false) }
    var isRequestPending by remember { mutableStateOf(false) }

    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val textColor = if (isDarkTheme) Color.Black else Color.White
    val backgroundColor = if (isDarkTheme) MaterialTheme.colorScheme.background else Color.White

    LaunchedEffect(matchId) {
        val doc = db.collection("matches").document(matchId).get().await()
        match = doc.toObject(Match::class.java)

        val participantIds = (doc["participants"] as? List<String> ?: emptyList()).toMutableList()
        val creatorId = doc.getString("creatorId")
        if (creatorId != null && !participantIds.contains(creatorId)) {
            participantIds.add(creatorId)
        }
        isParticipant = participantIds.contains(currentUserId)


        participants = participantIds.mapNotNull { uid ->
            val userSnap = db.collection("users").document(uid).get().await()
            userSnap.toObject(User::class.java)
        }

        db.collection("inviteRequests")
            .whereEqualTo("fromUserId", currentUserId)
            .whereEqualTo("matchId", matchId)
            .whereEqualTo("type", "join")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { docs ->
                isRequestPending = docs.documents.isNotEmpty()
            }
    }

    match?.let { m ->
        val isOwner = currentUserId == m.creatorId
        val maxPlayers = m.teamSize.toIntOrNull() ?: 0

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = stringResource(R.string.match_detail),
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                ProfileInfoCard(title = stringResource(R.string.pitch_name), value = m.pitchName)
                ProfileInfoCard(title = stringResource(R.string.date), value = m.date)
                ProfileInfoCard(title = stringResource(R.string.time), value = "${m.startTime} - ${m.endTime}")
                ProfileInfoCard(title = stringResource(R.string.team_size), value = m.teamSize)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val pitchQuery = "${m.pitchName}, ${m.district}, İstanbul"
                        val mapIntent = Intent(
                            Intent.ACTION_VIEW,
                            "geo:0,0?q=${Uri.encode(pitchQuery)}".toUri()
                        ).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = stringResource(R.string.view_location), color = textColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.participants),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                participants.forEach {
                    ProfileInfoCard(
                        title = "${it.name} ${it.surname}",
                        value = "${stringResource(R.string.position)}: ${it.position} • ${stringResource(R.string.rating)}: ${it.rating.toInt()}"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when {
                    isParticipant -> {
                        Text(
                            text = stringResource(R.string.already_participant),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    isRequestPending -> {
                        Text(
                            text = stringResource(R.string.request_pending),
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    !isOwner && participants.size < maxPlayers -> {
                        Button(
                            onClick = {
                                val request = hashMapOf(
                                    "type" to "join",
                                    "fromUserId" to currentUserId,
                                    "toUserId" to m.creatorId,
                                    "matchId" to m.id,
                                    "status" to "pending"
                                )
                                db.collection("inviteRequests").add(request)
                                isRequestPending = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(stringResource(R.string.join_request), fontSize = 16.sp, color = textColor)
                        }
                    }
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
