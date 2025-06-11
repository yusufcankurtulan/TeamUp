package com.example.teamatch.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamatch.R
import com.example.teamatch.data.User
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.PreferencesManager
import com.example.teamatch.components.BackgroundFootballIcons
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun RatePlayerScreen(
    playerId: String,
    matchId: String,
    navController: NavController,
    preferencesManager: PreferencesManager
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val ratingDocId = "${matchId}_${currentUserId}_$playerId"

    var currentRating by remember { mutableStateOf(50.0) }
    var loading by remember { mutableStateOf(true) }
    var alreadyRated by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scores = remember { mutableStateListOf(*Array(10) { 5f }) }
    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val baseColor = if (isDarkTheme) Color(0xFF74BBFB) else Color.Black
    val backIconColor = if (isDarkTheme) Color(0xFF74BBFB) else Color.Black

    val questions = listOf(
        R.string.q1, R.string.q2, R.string.q3, R.string.q4, R.string.q5,
        R.string.q6, R.string.q7, R.string.q8, R.string.q9, R.string.q10
    )

    LaunchedEffect(playerId, matchId) {
        val doc = db.collection("users").document(playerId).get().await()
        val user = doc.toObject(User::class.java)
        currentRating = user?.rating ?: 50.0

        val ratingDoc = db.collection("ratings").document(ratingDocId).get().await()
        alreadyRated = ratingDoc.exists()

        loading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundFootballIcons(preferencesManager = preferencesManager)

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (alreadyRated) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

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
                }
                Text(
                    text = getTranslatedString(R.string.already_rated),
                    color = baseColor
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = getTranslatedString(R.string.rate_player_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = baseColor
                )

                questions.forEachIndexed { index, resId ->
                    Column {
                        Text(
                            text = getTranslatedString(resId),
                            fontSize = 16.sp,
                            color = baseColor
                        )
                        Slider(
                            value = scores[index],
                            onValueChange = { scores[index] = it },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                    }
                }

                Button(
                    onClick = {
                        val weights = listOf(0.05f, 0.07f, 0.07f, 0.07f, 0.05f, 0.15f, 0.07f, 0.07f, 0.15f, 0.25f)
                        val weightedAverage = scores.zip(weights).sumOf { (score, weight) -> (score * weight).toDouble() }
                        val normalizedAverage = weightedAverage * 10

                        val ratingChange = when (normalizedAverage) {
                            in 90.0..100.0 -> 5.0
                            in 75.0..89.9  -> 3.0
                            in 65.0..74.9  -> 2.0
                            in 50.0..64.9  -> 1.0
                            in 40.0..49.9  -> -1.0
                            in 25.0..39.9  -> -2.0
                            in 15.0..24.9  -> -4.0
                            in 1.0..14.9   -> -8.0
                            else -> 0.0
                        }

                        val newRating = (currentRating + ratingChange).coerceIn(0.0, 100.0)

                        db.collection("users").document(playerId)
                            .update(
                                mapOf(
                                    "rating" to newRating,
                                    "matchCount" to FieldValue.increment(1)
                                )
                            )

                        val ratingData = mapOf(
                            "playerId" to playerId,
                            "matchId" to matchId,
                            "raterId" to currentUserId,
                            "oldRating" to currentRating,
                            "newRating" to newRating,
                            "answers" to scores.toList(),
                            "timestamp" to System.currentTimeMillis()
                        )
                        db.collection("ratings").document(ratingDocId).set(ratingData)
                        db.collection("player_rating_history").add(ratingData)

                        Toast.makeText(context, context.getString(R.string.rating_saved), Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = getTranslatedString(R.string.save_and_go_back),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
