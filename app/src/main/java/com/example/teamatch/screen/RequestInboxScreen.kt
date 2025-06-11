package com.example.teamatch.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.AppBottomBar
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.navigation.BottomBarItem
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.viewmodel.RequestViewModel
import com.example.teamatch.viewmodel.RequestViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestInboxScreen(
    preferencesManager: PreferencesManager,
    navController: NavController,
    selectedItem: BottomBarItem,
    onItemSelected: (BottomBarItem) -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val requestViewModel: RequestViewModel = remember {
        ViewModelProvider(
            viewModelStoreOwner,
            RequestViewModelFactory(preferencesManager)
        )[RequestViewModel::class.java]
    }

    LaunchedEffect(Unit) {
        requestViewModel.checkPendingRequests()
        requestViewModel.markRequestsAsSeen()
    }

    val hasPendingRequests by requestViewModel.hasUnseenRequests.collectAsState()

    LaunchedEffect(Unit) {
        db.collection("inviteRequests")
            .whereEqualTo("toUserId", currentUserId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                requests = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val matchId = data["matchId"] as? String
                    if (matchId.isNullOrBlank()) return@mapNotNull null
                    data + ("requestId" to doc.id)
                }
            }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedItem = selectedItem,
                preferencesManager = preferencesManager,
                onItemSelected = onItemSelected,
                hasPendingRequests = hasPendingRequests
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BackgroundFootballIcons(preferencesManager = preferencesManager)

            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = getTranslatedString(R.string.request_box),
                            fontFamily = FontFamily(Font(R.font.winter)),
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (requests.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getTranslatedString(R.string.empty_inbox),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(requests.size) { index ->
                    val request = requests[index]
                    val type = request["type"] as? String ?: ""
                    val fromUserId = request["fromUserId"] as? String ?: ""
                    val matchId = request["matchId"] as? String ?: ""

                    var senderName by remember { mutableStateOf("") }
                    var matchInfo by remember { mutableStateOf("") }

                    val invalidMatchText = getTranslatedString(R.string.invalid_match_reference)
                    val errorText = getTranslatedString(R.string.error_loading_match)

                    LaunchedEffect(key1 = fromUserId, key2 = matchId) {
                        try {
                            val userDoc = db.collection("users").document(fromUserId).get().await()
                            val name = userDoc.getString("name") ?: ""
                            val surname = userDoc.getString("surname") ?: ""
                            senderName = "$name $surname"

                            if (matchId.isNotBlank()) {
                                val matchDoc = db.collection("matches").document(matchId).get().await()
                                val pitch = matchDoc.getString("pitchName") ?: "?"
                                val start = matchDoc.getString("startTime") ?: ""
                                val end = matchDoc.getString("endTime") ?: ""
                                matchInfo = "$pitch ($start - $end)"
                            } else {
                                matchInfo = invalidMatchText // ✔ güvenli
                            }
                        } catch (e: Exception) {
                            matchInfo = errorText // ✔ güvenli
                        }
                    }


                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                if (matchId.isNotBlank() && fromUserId.isNotBlank()) {
                                    navController.navigate("request_detail/$matchId/$fromUserId")
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val requestTypeText = if (type == "invite") {
                                getTranslatedString(R.string.request_type_invite)
                            } else {
                                getTranslatedString(R.string.request_type_join)
                            }

                            Text(
                                text = "${getTranslatedString(R.string.request_type)}: $requestTypeText",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${getTranslatedString(R.string.sender)}: $senderName",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${getTranslatedString(R.string.match)}: $matchInfo",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}
