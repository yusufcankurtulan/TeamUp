package com.example.teamatch.screen

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.AppBottomBar
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.navigation.BottomBarItem
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.viewmodel.AuthViewModel
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.teamatch.viewmodel.RequestViewModel
import com.example.teamatch.viewmodel.RequestViewModelFactory

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLanguageChange: (String) -> Unit,
    navController: NavController,
    selectedItem: BottomBarItem,
    authViewModel: AuthViewModel,
    preferencesManager: PreferencesManager
) {
    val context = LocalContext.current
    val lightBlue = Color(0xFF74BBFB)
    val customFont = FontFamily(Font(R.font.winter))
    var currentItem by rememberSaveable { mutableStateOf(selectedItem) }
    var selectedLanguage by rememberSaveable { mutableStateOf(preferencesManager.getSelectedLanguage() ?: "tr") }
    val isDarkTheme = isSystemInDarkTheme()
    val supportEmail = "yusufcan.kurtulan@std.yeditepe.edu.tr"
    val textColor = MaterialTheme.colorScheme.onBackground
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val requestViewModel = remember {
        ViewModelProvider(
            viewModelStoreOwner,
            RequestViewModelFactory(PreferencesManager(context))
        )[RequestViewModel::class.java]
    }

    val hasPendingRequests by requestViewModel.hasUnseenRequests.collectAsState()

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedItem = currentItem,
                preferencesManager = preferencesManager,
                hasPendingRequests = hasPendingRequests,
                onItemSelected = { item ->
                    currentItem = item
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
            BackgroundFootballIcons(
                preferencesManager = preferencesManager

            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {

                Spacer(modifier = Modifier.height(80.dp))

                Text(
                    text = getTranslatedString(R.string.settings),
                    fontFamily = customFont,
                    fontSize = 40.sp,
                    color = textColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = getTranslatedString(R.string.theme),
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (darkTheme) onThemeToggle() },
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!darkTheme) lightBlue else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, lightBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = getTranslatedString(R.string.light_theme),
                            color = if (!darkTheme) Color.White else lightBlue
                        )
                    }

                    OutlinedButton(
                        onClick = { if (!darkTheme) onThemeToggle() },
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (darkTheme) lightBlue else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, lightBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = getTranslatedString(R.string.dark_theme),
                            color = if (darkTheme) Color.White else lightBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = getTranslatedString(R.string.language),
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedLanguage = "tr"
                            preferencesManager.setSelectedLanguage("tr")
                            onLanguageChange("tr")
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedLanguage == "tr") lightBlue else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, lightBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = getTranslatedString(R.string.turkish),
                            color = if (selectedLanguage == "tr") {
                                if (isDarkTheme) Color.Black else Color.White
                            } else {
                                lightBlue
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            selectedLanguage = "en"
                            preferencesManager.setSelectedLanguage("en")
                            onLanguageChange("en")
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedLanguage == "en") lightBlue else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, lightBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = getTranslatedString(R.string.english),
                            color = if (selectedLanguage == "en") {
                                if (isDarkTheme) Color.Black else Color.White
                            } else {
                                lightBlue
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                HorizontalDivider(thickness = 1.dp, color = textColor.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(24.dp))

                Text(text = getTranslatedString(R.string.app_info), style = MaterialTheme.typography.titleMedium, color = textColor)
                Text(text = getTranslatedString(R.string.app_version), color = textColor)

                Text(
                    text = getTranslatedString(R.string.support_feedback),
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:$supportEmail".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, "Teamatch - Geri Bildirim")
                                putExtra(Intent.EXTRA_TEXT, "Merhaba, uygulama hakkında geri bildirimim:")
                            }
                            context.startActivity(intent)
                        }

                )
                Text(
                    text = getTranslatedString(R.string.terms_privacy),
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("user_agreement")
                        }

                )
                Text(text = getTranslatedString(R.string.copyright), color = textColor.copy(alpha = 0.6f), fontSize = 12.sp)

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, lightBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(getTranslatedString(R.string.logout), color = lightBlue)
                }
            }
        }
    }
}

