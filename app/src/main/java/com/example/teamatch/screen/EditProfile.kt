package com.example.teamatch.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.viewmodel.AuthViewModel
import com.example.teamatch.viewmodel.EditProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import android.util.Base64
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teamatch.util.base64ToBitmap
import com.example.teamatch.util.encodeImageToBase64
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.io.ByteArrayInputStream

@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    viewModel: EditProfileViewModel,
    onNavigateBack: () -> Unit,
    onSaveChanges: () -> Unit,
    preferencesManager: PreferencesManager,
    onEditFieldClick: (field: String, value: String) -> Unit
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val profileImageUri = authViewModel.profileImageUri.value

    val position = authViewModel.userPosition.value
    val bio = authViewModel.bio.value
    val socialLink = authViewModel.socialLink.value

    viewModel.position.value = position
    viewModel.bio.value = bio
    viewModel.socialLink.value = socialLink

    val darkGray = MaterialTheme.colorScheme.background
    val lightBlue = MaterialTheme.colorScheme.primary
    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val textColor = Color.White
    val positionLabel = getTranslatedString(R.string.position)
    val bioLabel = getTranslatedString(R.string.bio)
    val socialLabel = getTranslatedString(R.string.social_media)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkGray)
    ) {
        BackgroundFootballIcons(preferencesManager = preferencesManager)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = getTranslatedString(R.string.back),
                    tint = lightBlue
                )
            }

            ProfileImageEditor(
                currentUri = profileImageUri,
                authViewModel = authViewModel,
                onImageSelected = { uri ->
                    authViewModel.setProfileImage(uri)
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    val base64 = bitmapToBase64(bitmap)
                    user?.uid?.let { uid ->
                        FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                            .update("profilePhotoBase64", base64)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                getTranslatedString(R.string.edit_profile),
                fontSize = 28.sp,
                color = lightBlue,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileItem(
                label = positionLabel,
                value = translatePosition(viewModel.position.value),
                onClick = {
                    onEditFieldClick("position", viewModel.position.value)
                }
            )


            ProfileItem(label = bioLabel, value = viewModel.bio.value, onClick = {
                onEditFieldClick("bio", viewModel.bio.value)
            })

            ProfileItem(label = socialLabel, value = viewModel.socialLink.value, isLink = false, onClick = {
                onEditFieldClick("social", viewModel.socialLink.value)
            })

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    user?.uid?.let { uid ->
                        viewModel.saveProfileChanges(uid) {
                            authViewModel.bio.value = viewModel.bio.value
                            authViewModel.socialLink.value = viewModel.socialLink.value
                            authViewModel.userPosition.value = viewModel.position.value
                            preferencesManager.setBio(viewModel.bio.value)
                            preferencesManager.setSocialLink(viewModel.socialLink.value)
                            onSaveChanges()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    getTranslatedString(R.string.save_changes),
                    color = textColor,
                    fontSize = 16.sp
                )
            }
        }
    }
}


fun bitmapToBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val byteArray = stream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

@Composable
fun ProfileItem(label: String, value: String, onClick: () -> Unit, isLink: Boolean = false) {
    val lightBlue = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = Color.Gray, fontSize = 14.sp)
            Text(
                value,
                color = if (isLink) lightBlue else MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (isLink) TextDecoration.Underline else TextDecoration.None
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = lightBlue
        )
    }
}

@Composable
fun translatePosition(position: String): String {
    return when (position.lowercase()) {
        "forvet" -> getTranslatedString(R.string.position_forward)
        "orta saha" -> getTranslatedString(R.string.position_midfield)
        "defans" -> getTranslatedString(R.string.position_defense)
        "kaleci" -> getTranslatedString(R.string.position_goalkeeper)
        else -> position
    }
}


@Composable
fun ProfileImageEditor(
    currentUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    authViewModel: AuthViewModel
) {
    val viewModel: EditProfileViewModel = viewModel()
    val context = LocalContext.current
    val base64Photo = authViewModel.profilePhotoBase64.value
    var imageUri by remember { mutableStateOf(currentUri) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val iceBlue = Color(0xFF74BBFB)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val selectedBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            val base64 = encodeImageToBase64(selectedBitmap, quality = 30)

            viewModel.profilePhotoBase64.value = base64
            authViewModel.profilePhotoBase64.value = base64

            bitmap = selectedBitmap
            imageUri = it
            onImageSelected(it)
        }
    }


    LaunchedEffect(base64Photo) {
        if (base64Photo.isNotEmpty()) {
            bitmap = base64ToBitmap(base64Photo)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            val decodedBytes = Base64.decode(base64Photo, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(decodedBytes))
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            }

            IconButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(32.dp)
                    .background(iceBlue, CircleShape)
                    .border(1.dp, Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = getTranslatedString(R.string.edit),
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}




@Composable
fun EditSingleFieldScreen(
    fieldName: String,
    authViewModel: AuthViewModel,
    preferencesManager: PreferencesManager,
    viewModel: EditProfileViewModel,
    onSave: () -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val darkGray = MaterialTheme.colorScheme.background
    val lightBlue = MaterialTheme.colorScheme.primary
    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val textColor = Color.White
    val positionMap = mapOf(
        "Forvet" to getTranslatedString(R.string.position_forward),
        "Orta Saha" to getTranslatedString(R.string.position_midfield),
        "Defans" to getTranslatedString(R.string.position_defense),
        "Kaleci" to getTranslatedString(R.string.position_goalkeeper)
    )
    var selectedPosition by remember { mutableStateOf(viewModel.position.value) }

    val label = when (fieldName) {
        "position" -> getTranslatedString(R.string.position)
        "bio" -> getTranslatedString(R.string.bio)
        "social" -> getTranslatedString(R.string.social_media)
        else -> getTranslatedString(R.string.edit)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = lightBlue,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (fieldName == "position") {
            positionMap.forEach { (rawValue, translatedValue) ->
                val isSelected = selectedPosition == rawValue
                Button(
                    onClick = {
                        selectedPosition = rawValue
                        viewModel.position.value = rawValue
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) lightBlue else Color.Transparent,
                        contentColor = if (isSelected) Color.White else lightBlue
                    ),
                    border = BorderStroke(1.dp, lightBlue),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(translatedValue)
                }
            }
        }
        else {
            val textState = when (fieldName) {
                "bio" -> viewModel.bio
                "social" -> viewModel.socialLink
                else -> remember { mutableStateOf("") }
            }

            TextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = lightBlue,
                    unfocusedIndicatorColor = lightBlue,
                    cursorColor = lightBlue
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (uid != null) {
                    val updates = when (fieldName) {
                        "position" -> {
                            authViewModel.userPosition.value = viewModel.position.value
                            mapOf("position" to viewModel.position.value)
                        }

                        "bio" -> {
                            authViewModel.bio.value = viewModel.bio.value
                            mapOf("bio" to viewModel.bio.value)
                        }

                        "social" -> {
                            authViewModel.socialLink.value = viewModel.socialLink.value
                            mapOf("socialLink" to viewModel.socialLink.value)
                        }

                        else -> emptyMap()
                    }

                    Firebase.firestore.collection("users").document(uid).update(updates)
                        .addOnSuccessListener {
                            onSave()
                        }
                } else {
                    onSave()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                getTranslatedString(R.string.save_changes),
                color = textColor,
                fontSize = 16.sp,

            )
        }
    }
}
