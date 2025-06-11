package com.example.teamatch.screen.welcome

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.viewmodel.AuthViewModel
import com.example.teamatch.util.LocationHelper

@Composable
fun WelcomeFlowController(
    navController: NavController,
    authViewModel: AuthViewModel,
    preferencesManager: PreferencesManager

) {
    var currentStep by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val selectedPositions = remember { mutableStateOf(listOf<String>()) }
    val selectedFeet = remember { mutableStateOf(listOf<String>()) }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }

    val showNext: () -> Unit = { currentStep++ }

    when (currentStep) {
        0 -> WelcomeScreen(onNextClick = showNext)

        1 -> FootballInfoScreen(
            selectedPositions = selectedPositions.value,
            onPositionsChanged = { selectedPositions.value = it },
            selectedFeet = selectedFeet.value,
            onFeetChanged = { selectedFeet.value = it },
            onNextClick = {
                val position = selectedPositions.value.firstOrNull() ?: ""
                val foot = selectedFeet.value.firstOrNull() ?: ""
                authViewModel.setUserDetails(position, "", "", foot)
                showNext()
            }
        )

        2 -> PhysicalInfoScreen(
            height = height.value,
            weight = weight.value,
            onHeightChange = {
                height.value = it
                authViewModel.setUserHeight(it)
            },
            onWeightChange = {
                weight.value = it
                authViewModel.setUserWeight(it)
            },
            onNextClick = showNext
        )

        3 -> ProfileImageScreen(
            onImageSelected = { uri -> authViewModel.setProfileImage(uri) },
            onNextClick = showNext
        )

        4 -> LocationPermissionScreen(
            onPermissionGranted = {
                LocationHelper(context).getDistrict { district ->
                    if (district != null) {
                        authViewModel.setUserDistrict(district)
                        preferencesManager.setUserDistrict(district)

                        authViewModel.updateUserDetailsToFirestore { success ->
                            if (success) {
                                authViewModel.completeFirstLoginFlow()

                                navController.navigate("home/$district") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Bilgiler kaydedilemedi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "İlçe alınamadı", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onPermissionDenied = {
                Toast.makeText(context, "Konum izni reddedildi", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        )


    }
}
