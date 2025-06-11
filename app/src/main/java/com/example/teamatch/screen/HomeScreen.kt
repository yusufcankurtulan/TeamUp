package com.example.teamatch.screen

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.teamatch.data.User
import com.example.teamatch.navigation.BottomBarItem
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.util.base64ToBitmap
import com.example.teamatch.viewmodel.AuthViewModel
import com.example.teamatch.viewmodel.RequestViewModel
import com.example.teamatch.viewmodel.RequestViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    preferencesManager: PreferencesManager
) {
    LaunchedEffect(Unit) {
        authViewModel.fetchUserDataFromFirestore()
    }

    var showPlayers by remember { mutableStateOf(false) }
    var playerList by remember { mutableStateOf<List<User>>(emptyList()) }

    var showMatches by remember { mutableStateOf(false) }
    var matchList by remember { mutableStateOf<List<Match>>(emptyList()) }
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
    val fullName = authViewModel.fullName.value
    val customFont = FontFamily(Font(R.font.winter))
    var selectedItem by rememberSaveable { mutableStateOf(BottomBarItem.Home) }

    LaunchedEffect(showPlayers) {
        if (showPlayers && currentUserId != null) {
            authViewModel.fetchNearbyPlayers(currentUserId) { players ->
                playerList = players
            }
        }
    }

    LaunchedEffect(showMatches) {
        if (showMatches) {
            authViewModel.fetchAllMatches { matches ->
                matchList = matches
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomBar(
                selectedItem = selectedItem,
                preferencesManager = preferencesManager,
                hasPendingRequests = hasPendingRequests,
                onItemSelected = { item ->
                    selectedItem = item
                    when (item) {
                        BottomBarItem.Home -> {}
                        BottomBarItem.Profile -> navController.navigate("profile")
                        BottomBarItem.Settings -> navController.navigate("settings")
                        BottomBarItem.MatchHistory -> navController.navigate("match_result")
                        BottomBarItem.RequestInbox -> navController.navigate("request_inbox")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            BackgroundFootballIcons(preferencesManager = preferencesManager)

            Text(
                text = getTranslatedString(R.string.app_name_alt),
                fontFamily = customFont,
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    showPlayers -> {
                        BackButton(onBackClick = { showPlayers = false })
                        Spacer(modifier = Modifier.height(16.dp))
                        PlayerList(playerList, navController = navController)
                    }

                    showMatches -> {
                        BackButton(onBackClick = { showMatches = false })
                        Spacer(modifier = Modifier.height(16.dp))
                        MatchList(matchList, navController = navController)
                    }

                    else -> {
                        Text(
                            text = getTranslatedString(R.string.welcome_user, fullName),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        val buttonModifier = Modifier
                            .width(300.dp)
                            .height(60.dp)
                        val buttonShape = RoundedCornerShape(20.dp)
                        val buttonColors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )

                        Button(
                            onClick = {
                                showPlayers = true
                            },
                            modifier = buttonModifier,
                            shape = buttonShape,
                            colors = buttonColors
                        ) {
                            Text(
                                text = getTranslatedString(R.string.find_player),
                                fontSize = 20.sp,
                                fontFamily = customFont,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                showMatches = true
                            },
                            modifier = buttonModifier,
                            shape = buttonShape,
                            colors = buttonColors
                        ) {
                            Text(
                                text = getTranslatedString(R.string.find_match),
                                fontSize = 20.sp,
                                fontFamily = customFont,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { navController.navigate("create_event") },
                            modifier = buttonModifier,
                            shape = buttonShape,
                            colors = buttonColors
                        ) {
                            Text(
                                text = getTranslatedString(R.string.create_match),
                                fontSize = 20.sp,
                                fontFamily = customFont,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { navController.navigate("my_matches") },
                            modifier = buttonModifier,
                            shape = buttonShape,
                            colors = buttonColors
                        ) {
                            Text(
                                text = getTranslatedString(R.string.my_matches_title),
                                fontSize = 20.sp,
                                fontFamily = customFont,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchList(matches: List<Match>, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (matches.isEmpty()) {
                Text(
                    text = getTranslatedString(R.string.no_matches_found),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 70.dp)
                )
            } else {
                matches.forEach { match ->
                    MatchCard(match = match) {
                        navController.navigate("match_detail/${match.id}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
fun MatchCard(match: Match, onClick: () -> Unit) {
    val lightOverlay = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = lightOverlay),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${getTranslatedString(id = R.string.pitch_name)}: ${match.pitchName}",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${getTranslatedString(id = R.string.time)}: ${match.startTime} - ${match.endTime}"
            )
        }
    }
}



@Composable
fun BackButton(onBackClick: () -> Unit) {
    IconButton(onClick = onBackClick, modifier = Modifier.padding(16.dp)) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = getTranslatedString(R.string.back),
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun PlayerList(players: List<User>, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(top = 20.dp)
            .padding(horizontal = 16.dp)

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (players.isEmpty()) {
                Text(
                    text = getTranslatedString(R.string.no_players_found),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 70.dp)
                )
            } else {
                players.forEach { user ->
                    PlayerCard(user = user) {
                        navController.navigate("player_detail/${user.uid}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}






@Composable
fun PlayerCard(user: User, onClick: () -> Unit) {
    val lightOverlay = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = lightOverlay),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val bitmap = remember(user.profilePhotoBase64) {
                try {
                    if (user.profilePhotoBase64.isNotBlank()) {
                        base64ToBitmap(user.profilePhotoBase64)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ekran_resmi_2025_02_20_00_02_17),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "${user.name} ${user.surname}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${getTranslatedString(id = R.string.position)}: ${user.position}",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${getTranslatedString(id = R.string.rating)}: ${user.rating.toInt()}",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

