package com.example.teamatch.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamatch.R
import com.example.teamatch.viewmodel.AuthViewModel
import com.example.teamatch.components.AppBottomBar
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.localization.LocalAppLocale
import com.example.teamatch.navigation.BottomBarItem
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.util.LocaleHelper.translateFoot
import com.example.teamatch.util.LocaleHelper.translatePosition
import com.google.firebase.auth.FirebaseAuth
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.teamatch.PreferencesManager
import com.example.teamatch.viewmodel.RequestViewModel
import com.example.teamatch.viewmodel.RequestViewModelFactory
import java.io.ByteArrayInputStream

@Composable
fun ProfileScreen(
    navController: NavController,
    onEditClick: () -> Unit,
    authViewModel: AuthViewModel,
    preferencesManager: PreferencesManager,
) {
    LaunchedEffect(Unit) {
        authViewModel.fetchUserDataFromFirestore()
    }

    val darkGray = MaterialTheme.colorScheme.background
    val lightBlue = MaterialTheme.colorScheme.primary
    val locale = LocalAppLocale.current
    val context = LocalContext.current
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val requestViewModel = remember {
        ViewModelProvider(
            viewModelStoreOwner,
            RequestViewModelFactory(PreferencesManager(context))
        )[RequestViewModel::class.java]
    }

    val hasPendingRequests by requestViewModel.hasUnseenRequests.collectAsState()
    val base64Photo = authViewModel.profilePhotoBase64.value
    val bio = authViewModel.bio.value
    val social = authViewModel.socialLink.value
    val name = authViewModel.fullName.value
    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val position = translatePosition(authViewModel.userPosition.value, locale)
    val height = authViewModel.userHeight.value
    val weight = authViewModel.userWeight.value
    val foot = translateFoot(authViewModel.preferredFoot.value, locale)
    val district = authViewModel.userDistrict.value
    val birthDate = authViewModel.birthDate.value
    val rating = authViewModel.userRating.value
    val matchCount = authViewModel.matchCount.value

    Scaffold(
        containerColor = darkGray,
        bottomBar = {
            AppBottomBar(
                selectedItem = BottomBarItem.Profile,
                preferencesManager = preferencesManager,
                hasPendingRequests = hasPendingRequests,
                        onItemSelected = { item ->
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
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    if (!base64Photo.isNullOrBlank()) {
                        val decodedBytes = Base64.decode(base64Photo, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(decodedBytes))
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, darkGray),
                                    startY = 550f
                                )
                            )
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    if (bio.isNotBlank()) {
                        Text(
                            text = bio,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 4.dp)
                        )
                    }

                    if (social.isNotBlank()) {
                        val context = LocalContext.current
                        val validUrl = if (social.startsWith("http")) social else "https://$social"

                        Text(
                            text = social,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 2.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
                                    context.startActivity(intent)
                                }
                        )
                    }

                    Text(
                        text = email,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ProfileInfoCard(title = getTranslatedString(R.string.position), value = position)
                    ProfileInfoCard(title = getTranslatedString(R.string.preferred_foot), value = foot)
                    ProfileInfoCard(title = getTranslatedString(R.string.height), value = "$height cm")
                    ProfileInfoCard(title = getTranslatedString(R.string.weight), value = "$weight kg")
                    ProfileInfoCard(title = getTranslatedString(R.string.district), value = district)
                    ProfileInfoCard(title = getTranslatedString(R.string.rating), value = rating.toString())
                    ProfileInfoCard(title = getTranslatedString(R.string.match_count), value = matchCount.toString())
                    ProfileInfoCard(title = getTranslatedString(R.string.birth_date), value = birthDate)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, lightBlue)
                    ) {
                        Text(getTranslatedString(R.string.edit_profile), color = lightBlue)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileInfoCard(title: String, value: String) {
    val lightOverlay = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = lightOverlay),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 14.sp)
            Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}


