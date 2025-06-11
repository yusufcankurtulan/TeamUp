package com.example.teamatch.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.data.Match
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun MatchListScreen(
    district: String,
    onBackPressed: () -> Unit,
    preferencesManager: PreferencesManager,
    navController: NavController
) {
    var matchList by remember { mutableStateOf<List<Match>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val backIconColor = if (isDarkTheme) Color.White else Color.Black

    LaunchedEffect(district) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = if (district == "all") {
                db.collection("matches").get().await()
            } else {
                db.collection("matches")
                    .whereEqualTo("district", district)
                    .get().await()
            }

            matchList = snapshot.mapNotNull { doc ->
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

            isLoading = false
        } catch (e: Exception) {
            errorMessage = context.getString(R.string.data_fetch_error)
            isLoading = false
        }
    }

    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        BackgroundFootballIcons(preferencesManager = preferencesManager)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = getTranslatedString(R.string.back),
                    tint = backIconColor
                )
            }
                Spacer(modifier = Modifier.width(8.dp))


                Text(
                text = if (district == "all")
                    getTranslatedString(R.string.matches_title_all)
                else
                    getTranslatedString(R.string.matches_title, district),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }


            Spacer(modifier = Modifier.height(2.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                errorMessage != null -> {
                    Text(errorMessage ?: "", color = Color.Red)
                }

                matchList.isEmpty() -> {
                    Text(
                        text = getTranslatedString(R.string.no_matches_found),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                else -> {
                    val lightOverlay = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        matchList.forEach { match ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        navController.navigate("match_detail/${match.id}")
                                    },
                                colors = CardDefaults.cardColors(containerColor = lightOverlay),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = getTranslatedString(R.string.field_label, match.pitchName),
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = getTranslatedString(R.string.date_label, match.date),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = getTranslatedString(
                                            R.string.time_label,
                                            match.startTime,
                                            match.endTime
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    Text(
                                        text = getTranslatedString(R.string.team_size_label, match.teamSize),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
