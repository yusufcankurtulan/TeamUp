package com.example.teamatch.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    preferencesManager: PreferencesManager
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val backgroundColor = Color(0xFF2C2C2C)
    val iceBlue = Color(0xFF74BBFB)
    val customFont = FontFamily(Font(R.font.winter))
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        BackgroundFootballIcons(
            preferencesManager = preferencesManager
        )
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "TeamUp",
            fontFamily = customFont,
            fontSize = 40.sp,
            color = iceBlue,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 50.dp)
                .offset(y = (-40).dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(getTranslatedString(R.string.email), color = iceBlue) },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = iceBlue,
                focusedIndicatorColor = iceBlue,
                cursorColor = iceBlue
            ),
            textStyle = LocalTextStyle.current.copy(color = iceBlue),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(30.dp, 0.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(getTranslatedString(R.string.password), color = iceBlue) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = iceBlue,
                focusedIndicatorColor = iceBlue,
                cursorColor = iceBlue
            ),
            textStyle = LocalTextStyle.current.copy(color = iceBlue),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(30.dp, 0.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        errorMessage?.let {
            Text(text = it, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
                .border(1.dp, iceBlue, RoundedCornerShape(30.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = iceBlue
            ),
            onClick = {
                authViewModel.login(email.trim(), password) { success, error ->
                    if (success) {
                        preferencesManager.setUserLoggedIn(true)
                        val isFirstLogin = preferencesManager.isFirstLogin()
                        onLoginSuccess(isFirstLogin)
                    } else {
                        errorMessage = error ?: context.getString(R.string.login_failed)
                    }
                }
            }
        ) {
            Text(getTranslatedString(R.string.login), color = iceBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Text(
            text = getTranslatedString(R.string.no_account_register),
            color = iceBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onRegisterClick() }
                .padding(8.dp)
        )
    }
    }
}
