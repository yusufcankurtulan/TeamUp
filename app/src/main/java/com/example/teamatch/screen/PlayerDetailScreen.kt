package com.example.teamatch.screen

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teamatch.R
import com.example.teamatch.data.Match
import com.example.teamatch.data.User
import com.example.teamatch.util.base64ToBitmap
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(userId: String) {
    val db = FirebaseFirestore.getInstance()
    var user by remember { mutableStateOf<User?>(null) }
    var isAlreadyInvited by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var showMatchSheet by remember { mutableStateOf(false) }
    var userMatches by remember { mutableStateOf<List<Match>>(emptyList()) }

    LaunchedEffect(userId) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                user = doc.toObject(User::class.java)

                db.collection("inviteRequests")
                    .whereEqualTo("fromUserId", currentUserId)
                    .whereEqualTo("toUserId", userId)
                    .whereEqualTo("status", "pending")
                    .whereEqualTo("type", "invite")
                    .get()
                    .addOnSuccessListener { invites ->
                        isAlreadyInvited = !invites.isEmpty
                    }
            }
            .addOnFailureListener {
                Log.e("PlayerDetail", "User fetch failed", it)
            }
    }

    Scaffold(
        bottomBar = {
            user?.let {
                val isCurrentUserViewingSelf = currentUserId == it.uid

                if (!isCurrentUserViewingSelf) {
                    if (isAlreadyInvited) {
                        Text(
                            text = getTranslatedString(R.string.already_invited),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        Button(
                            onClick = {
                                showMatchSheet = true
                                FirebaseFirestore.getInstance()
                                    .collection("matches")
                                    .whereEqualTo("creatorId", currentUserId)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        userMatches = snapshot.mapNotNull { doc ->
                                            try {
                                                Match(
                                                    id = doc.id,
                                                    district = doc.getString("district") ?: "",
                                                    pitchName = doc.getString("pitchName") ?: "",
                                                    date = doc.getString("date") ?: "",
                                                    startTime = doc.getString("startTime") ?: "",
                                                    endTime = doc.getString("endTime") ?: "",
                                                    teamSize = doc.getString("teamSize") ?: "",
                                                    creatorId = doc.getString("creatorId") ?: ""
                                                )
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                    }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = getTranslatedString(R.string.invite_to_match), fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        user?.let { u ->
            val imageBitmap = base64ToBitmap(u.profilePhotoBase64)?.asImageBitmap()

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                    },
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = getTranslatedString(R.string.back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ekran_resmi_2025_02_20_00_02_17),
                        contentDescription = null,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "${u.name} ${u.surname}", fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)

                Spacer(modifier = Modifier.height(8.dp))
                ProfileInfoCard(
                    title = getTranslatedString(R.string.position),
                    value = translateValue(u.position, "position")
                )
                ProfileInfoCard(title = getTranslatedString(R.string.rating), value = u.rating.toInt().toString())
                ProfileInfoCard(title =getTranslatedString(R.string.match_count), value = u.matchCount.toInt().toString())
                ProfileInfoCard(title = getTranslatedString(R.string.height), value = "${u.height} cm")
                ProfileInfoCard(title = getTranslatedString(R.string.weight), value = "${u.weight} kg")
                ProfileInfoCard(title = getTranslatedString(R.string.birth_date), value = u.birthDate)
                ProfileInfoCard(title = getTranslatedString(R.string.district), value = u.district)
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
    val inviteSentText = getTranslatedString(R.string.invite_sent_success)

    if (showMatchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMatchSheet = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = getTranslatedString(R.string.select_a_match),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                userMatches.forEach { match ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                val invite = hashMapOf(
                                    "type" to "invite",
                                    "fromUserId" to currentUserId,
                                    "toUserId" to user?.uid,
                                    "matchId" to match.id,
                                    "status" to "pending"
                                )

                                FirebaseFirestore.getInstance()
                                    .collection("inviteRequests")
                                    .add(invite)

                                showMatchSheet = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(inviteSentText)
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = match.pitchName, fontSize = 16.sp)
                            Text(text = "${match.date} • ${match.startTime}-${match.endTime}", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun translateValue(key: String, type: String): String {
    return when (type) {
        "position" -> when (key.lowercase()) {
            "kaleci" -> getTranslatedString(R.string.goalkeeper)
            "defans" -> getTranslatedString(R.string.defender)
            "orta saha" -> getTranslatedString(R.string.midfielder)
            "forvet" -> getTranslatedString(R.string.forward)
            else -> key
        }
        "foot" -> when (key.lowercase()) {
            "sağ" -> getTranslatedString(R.string.right_foot)
            "sol" -> getTranslatedString(R.string.left_foot)
            "iki" -> getTranslatedString(R.string.both_feet)
            else -> key
        }
        else -> key
    }
}

