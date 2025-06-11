package com.example.teamatch.screen.welcome

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.example.teamatch.util.encodeImageToBase64
import com.google.accompanist.flowlayout.FlowRow

private val backgroundColor = Color(0xFF2C2C2C)
private val iceBlue = Color(0xFF74BBFB)

@Composable
fun WelcomeScreen(onNextClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome! Let's get to know each other.",
            fontSize = 28.sp,
            color = iceBlue,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onNextClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(56.dp)
                .background(color = iceBlue, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                tint = backgroundColor
            )
        }
    }
}

@Composable
fun FootballInfoScreen(
    selectedPositions: List<String>,
    onPositionsChanged: (List<String>) -> Unit,
    selectedFeet: List<String>,
    onFeetChanged: (List<String>) -> Unit,
    onNextClick: () -> Unit
) {
    val positions = listOf("Forward", "Midfielder", "Defender", "Goalkeeper")
    val footOptions = listOf("Right", "Left", "Both")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            Text(
                text = "Select your position and preferred foot",
                fontSize = 20.sp,
                color = iceBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text("Position", color = iceBlue, fontSize = 16.sp)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                positions.forEach { position ->
                    val isSelected = position in selectedPositions
                    OutlinedButton(
                        onClick = {
                            val updated = if (isSelected) selectedPositions - position else selectedPositions + position
                            onPositionsChanged(updated)
                        },
                        border = BorderStroke(1.dp, iceBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) iceBlue.copy(alpha = 0.2f) else Color.Transparent,
                            contentColor = iceBlue
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(position)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Preferred Foot", color = iceBlue, fontSize = 16.sp)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                footOptions.forEach { foot ->
                    val isSelected = foot in selectedFeet
                    OutlinedButton(
                        onClick = {
                            val updated = if (isSelected) selectedFeet - foot else selectedFeet + foot
                            onFeetChanged(updated)
                        },
                        border = BorderStroke(1.dp, iceBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) iceBlue.copy(alpha = 0.2f) else Color.Transparent,
                            contentColor = iceBlue
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(foot)
                    }
                }
            }
        }

        IconButton(
            onClick = onNextClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(56.dp)
                .background(color = iceBlue, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                tint = backgroundColor
            )
        }
    }
}

@Composable
fun PhysicalInfoScreen(
    height: String,
    weight: String,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onNextClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            Text(
                text = "Enter your height and weight",
                fontSize = 20.sp,
                color = iceBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = height,
                onValueChange = onHeightChange,
                label = { Text("Height (cm)", color = iceBlue) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = iceBlue),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iceBlue,
                    unfocusedBorderColor = iceBlue,
                    cursorColor = iceBlue,
                    focusedLabelColor = iceBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text("Weight (kg)", color = iceBlue) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = iceBlue),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iceBlue,
                    unfocusedBorderColor = iceBlue,
                    cursorColor = iceBlue,
                    focusedLabelColor = iceBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        IconButton(
            onClick = onNextClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(56.dp)
                .background(color = iceBlue, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                tint = backgroundColor
            )
        }
    }
}

@Composable
fun ProfileImageScreen(
    onImageSelected: (Uri) -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            onImageSelected(it)

            val source = ImageDecoder.createSource(context.contentResolver, it)
            val selectedBitmap = ImageDecoder.decodeBitmap(source)

            bitmap = selectedBitmap

            val base64String = encodeImageToBase64(selectedBitmap, quality = 10)
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            uid?.let { userId ->
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("profilePhotoBase64", base64String)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Selected Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            )
        } ?: Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("Tap to select a photo", color = Color.White)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { launcher.launch("image/*") }
        )

        IconButton(
            onClick = onNextClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
                .background(color = Color(0xFF74BBFB), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                tint = Color(0xFF2C2C2C)
            )
        }
    }
}

@Composable
fun LocationPermissionScreen(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    val permissionAlreadyGranted =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionRequested = true
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    if (!permissionAlreadyGranted && !permissionRequested) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "We need your location to find nearby matches.",
                        color = Color.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            permissionLauncher.launch(permission)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF74BBFB))
                    ) {
                        Text("Grant Permission", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { onPermissionDenied() }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            }
        }
    } else if (permissionAlreadyGranted) {
        onPermissionGranted()
    }
}

@Preview(showBackground = true)
@Composable
fun LocationPermissionPreview() {
    LocationPermissionScreen(
        onPermissionGranted = {},
        onPermissionDenied = {}
    )
}
