package com.example.teamatch.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.data.User
import com.example.teamatch.data.Match
import com.example.teamatch.util.base64ToBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun RequestDetailScreen(
    matchId: String?,
    userId: String?,
    navController: NavController,
    preferencesManager: PreferencesManager
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val scope = rememberCoroutineScope()

    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surfaceVariant

    var user by remember { mutableStateOf<User?>(null) }
    var match by remember { mutableStateOf<Match?>(null) }
    var requestId by remember { mutableStateOf<String?>(null) }
    var type by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val invalidReferenceText = stringResource(R.string.invalid_match_reference)
    val loadErrorText = stringResource(R.string.error_loading_match)
    val userNotFound = stringResource(R.string.user_not_found)
    val matchNotFound = stringResource(R.string.match_not_found)

    LaunchedEffect(matchId, userId) {
        if (matchId.isNullOrBlank() || userId.isNullOrBlank()) {
            error = invalidReferenceText
            return@LaunchedEffect
        }

        try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                error = userNotFound
                return@LaunchedEffect
            }
            user = userDoc.toObject(User::class.java)

            val matchDoc = db.collection("matches").document(matchId).get().await()
            if (!matchDoc.exists()) {
                error = matchNotFound
                return@LaunchedEffect
            }
            match = matchDoc.toObject(Match::class.java)

            val snapshot = db.collection("inviteRequests")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("fromUserId", userId)
                .whereEqualTo("matchId", matchId)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull()
            requestId = doc?.id
            type = doc?.getString("type")
        } catch (e: Exception) {
            error = loadErrorText
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundFootballIcons(preferencesManager = preferencesManager)

        if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            user?.let { u ->
                val imageBitmap = base64ToBitmap(u.profilePhotoBase64)?.asImageBitmap()
                imageBitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = stringResource(id = R.string.profile_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(100.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("${u.name} ${u.surname}", style = MaterialTheme.typography.headlineSmall, color = primaryTextColor)
                Text(u.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (u.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(u.bio, style = MaterialTheme.typography.bodyMedium, color = primaryTextColor)
                }
                if (u.socialLink.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(u.socialLink, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            match?.let { m ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${stringResource(R.string.pitch_name)}: ${m.pitchName}", style = MaterialTheme.typography.bodyLarge, color = primaryTextColor)
                        Text("${stringResource(R.string.district)}: ${m.district}", style = MaterialTheme.typography.bodyLarge, color = primaryTextColor)
                        Text("${stringResource(R.string.date)}: ${m.date}", style = MaterialTheme.typography.bodyLarge, color = primaryTextColor)
                        Text("${stringResource(R.string.time)}: ${m.startTime} - ${m.endTime}", style = MaterialTheme.typography.bodyLarge, color = primaryTextColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            requestId?.let { id ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                val acceptedUserId = if (type == "invite") currentUserId else userId
                                db.collection("matches").document(matchId!!)
                                    .update("participants", FieldValue.arrayUnion(acceptedUserId))

                                db.collection("inviteRequests").document(id)
                                    .update("status", "accepted")

                                navController.popBackStack()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.accept))
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                db.collection("inviteRequests").document(id)
                                    .update("status", "rejected")
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.reject))
                    }
                }
            }
        }
    }
}
