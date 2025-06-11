package com.example.teamatch

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.teamatch.navigation.BottomBarItem
import com.example.teamatch.screen.*
import com.example.teamatch.screen.welcome.WelcomeFlowController
import com.example.teamatch.viewmodel.AuthViewModel
import com.example.teamatch.viewmodel.EditProfileViewModel
import com.example.teamatch.viewmodel.EditProfileViewModelFactory
import com.example.teamatch.viewmodel.RequestViewModel
import com.example.teamatch.viewmodel.RequestViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavigation(
    navController: NavHostController,
    preferencesManager: PreferencesManager,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLanguageChange: (String) -> Unit,
) {
    val authViewModel: AuthViewModel = viewModel()

    val context = LocalContext.current
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val requestViewModel: RequestViewModel = remember {
        ViewModelProvider(
            viewModelStoreOwner,
            RequestViewModelFactory(PreferencesManager(context))
        )[RequestViewModel::class.java]
    }

    LaunchedEffect(Unit) {
        requestViewModel.checkPendingRequests()
    }

    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val isFirebaseLoggedIn = firebaseUser != null
    val isLocallyLoggedIn = preferencesManager.isUserLoggedIn()
    val isFirstLogin = preferencesManager.isFirstLogin()
    val district = preferencesManager.getUserDistrict()

    val startDestination = when {
        isFirebaseLoggedIn && isLocallyLoggedIn -> {
            if (isFirstLogin) "welcome" else "home/$district"
        }
        else -> "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                preferencesManager = preferencesManager,
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { isFirstLogin ->
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        Firebase.firestore.collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                val isFirstLogin = doc.getBoolean("isFirstLogin") != false
                                preferencesManager.setFirstLogin(isFirstLogin)

                                if (isFirstLogin) {
                                    navController.navigate("welcome") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                } else {
                                    val district = preferencesManager.getUserDistrict()
                                    navController.navigate("home/$district") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    FirebaseAuth.getInstance().signOut()
                    preferencesManager.setUserLoggedIn(false)
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                preferencesManager = preferencesManager,
                onLoginClick = { navController.navigate("login") }
            )
        }

        composable("welcome") {
            WelcomeFlowController(
                navController = navController,
                authViewModel = authViewModel,
                preferencesManager = preferencesManager
            )
        }

        composable(
            "home/{district}",
            arguments = listOf(navArgument("district") { type = NavType.StringType })
        ) { backStack ->
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                preferencesManager = preferencesManager,
            )
        }

        composable("match_list/{district}", arguments = listOf(navArgument("district") { type = NavType.StringType })) {
            val district = it.arguments?.getString("district") ?: ""
            MatchListScreen(
                district = district,
                preferencesManager = preferencesManager,
                navController = navController,
                onBackPressed = { navController.popBackStack() }
            )

        }

        composable("rate_match_players/{matchId}") {
            val matchId = it.arguments?.getString("matchId") ?: return@composable
            PlayerRatingScreen(matchId, navController, preferencesManager)
        }

        composable("rate_player/{playerId}/{matchId}") {
            val playerId = it.arguments?.getString("playerId") ?: return@composable
            val matchId = it.arguments?.getString("matchId") ?: return@composable
            RatePlayerScreen(playerId, matchId, navController, preferencesManager)
        }

        composable("my_matches") {
            MyMatchesScreen(navController, preferencesManager)
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                preferencesManager = preferencesManager,
                onEditClick = { navController.navigate("edit_profile") },
                authViewModel = authViewModel
            )
        }


        composable("edit_profile") {
            val owner = checkNotNull(LocalViewModelStoreOwner.current)
            val viewModel = remember {
                ViewModelProvider(
                    owner,
                    EditProfileViewModelFactory(
                        authViewModel.userPosition.value,
                        authViewModel.bio.value,
                        authViewModel.socialLink.value
                    )
                )[EditProfileViewModel::class.java]
            }
            EditProfileScreen(
                authViewModel = authViewModel,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditFieldClick = { field, value ->
                    val encodedValue = URLEncoder.encode(value, "UTF-8")
                    navController.navigate("edit_single_field/$field/$encodedValue")
                },
                onSaveChanges = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    uid?.let {
                        viewModel.saveProfileChanges(it) {
                            navController.popBackStack()
                        }
                    }
                },
                preferencesManager = preferencesManager
            )
        }

        composable("edit_single_field/{field}/{value}", arguments = listOf(
            navArgument("field") { type = NavType.StringType },
            navArgument("value") { type = NavType.StringType })
        ) {
            val field = URLDecoder.decode(it.arguments?.getString("field") ?: "", "UTF-8")
            val value = URLDecoder.decode(it.arguments?.getString("value") ?: "", "UTF-8")
            val owner = checkNotNull(LocalViewModelStoreOwner.current)
            val viewModel = remember {
                ViewModelProvider(
                    owner,
                    EditProfileViewModelFactory(
                        authViewModel.userPosition.value,
                        authViewModel.bio.value,
                        authViewModel.socialLink.value
                    )
                )[EditProfileViewModel::class.java]
            }
            if (field == "position" && value.isNotEmpty()) {
                viewModel.position.value = value
            }
            EditSingleFieldScreen(field, authViewModel, preferencesManager, viewModel) {
                navController.popBackStack()
            }
        }

        composable("match_result") {
            MatchResultScreen(navController, preferencesManager)
        }

        composable("create_event") {
            CreateEventScreen(
                navController = navController,
                preferencesManager = preferencesManager,
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable("user_agreement") {
            UserAgreementScreen(
                navController = navController,
                preferencesManager = preferencesManager
            )
        }

        composable("settings") {
            SettingsScreen(
                darkTheme,
                onThemeToggle,
                onLanguageChange,
                navController,
                BottomBarItem.Settings,
                authViewModel,
                preferencesManager
            )
        }

        composable("player_detail/{userId}") {
            val userId = it.arguments?.getString("userId") ?: return@composable
            PlayerDetailScreen(userId)
        }

        composable("request_inbox") {
            RequestInboxScreen(preferencesManager, navController, BottomBarItem.RequestInbox) { item ->
                navController.navigate(item.route)
            }
        }

        composable("match_detail/{matchId}") {
            val matchId = it.arguments?.getString("matchId") ?: return@composable
            MatchDetailScreen(matchId, navController = navController, preferencesManager = preferencesManager)
        }

        composable("request_detail/{matchId}/{userId}") {
            val matchId = it.arguments?.getString("matchId") ?: ""
            val userId = it.arguments?.getString("userId") ?: ""
            RequestDetailScreen(matchId, userId, navController, preferencesManager)
        }
    }
}
