package com.example.teamatch.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.AppBottomBar
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.data.Match
import com.example.teamatch.navigation.BottomBarItem
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.viewmodel.RequestViewModel
import com.example.teamatch.viewmodel.RequestViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun MatchResultScreen(
    navController: NavController,
    preferencesManager: PreferencesManager
) {
    var matchList by remember { mutableStateOf<List<Match>>(emptyList()) }
    var selectedItem by remember { mutableStateOf(BottomBarItem.MatchHistory) }
    val context = LocalContext.current
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val requestViewModel = remember {
        ViewModelProvider(
            viewModelStoreOwner,
            RequestViewModelFactory(PreferencesManager(context))
        )[RequestViewModel::class.java]
    }

    val hasPendingRequests by requestViewModel.hasUnseenRequests.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(currentUserId) {
        currentUserId?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            val participantMatches = db.collection("matches")
                .whereArrayContains("participants", uid)
                .get()
                .await()

            val createdMatches = db.collection("matches")
                .whereEqualTo("creatorId", uid)
                .get()
                .await()

            val allDocs = participantMatches.documents + createdMatches.documents

            matchList = allDocs.distinctBy { it.id }.mapNotNull { doc ->
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
    }


    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedItem = selectedItem,
                preferencesManager = preferencesManager,
                hasPendingRequests = hasPendingRequests,
                onItemSelected = { item ->
                    selectedItem = item
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BackgroundFootballIcons(preferencesManager = preferencesManager)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = getTranslatedString(R.string.match_feedback),
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (matchList.isEmpty()) {
                    Text(
                        text = getTranslatedString(R.string.match_not_found),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    matchList.forEach { match ->
                        val lightOverlay = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("rate_match_players/${match.id}")
                                }
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = lightOverlay),
                            shape = RoundedCornerShape(12.dp)
                        )
                        {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = match.pitchName,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${match.startTime} - ${match.endTime}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
