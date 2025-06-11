package com.example.teamatch.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teamatch.PreferencesManager
import com.example.teamatch.R
import com.example.teamatch.components.BackgroundFootballIcons
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.viewmodel.AuthViewModel
import java.util.Calendar

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    preferencesManager: PreferencesManager
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val backgroundColor = Color(0xFF2C2C2C)
    val iceBlue = Color(0xFF74BBFB)
    val customFont = FontFamily(Font(R.font.winter))


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
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = getTranslatedString(R.string.register),
                fontSize = 40.sp,
                fontFamily = customFont,
                color = iceBlue,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(getTranslatedString(R.string.name), color = iceBlue) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = outlinedColors(),
                textStyle = LocalTextStyle.current.copy(color = iceBlue),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                label = { Text(getTranslatedString(R.string.surname), color = iceBlue) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = outlinedColors(),
                textStyle = LocalTextStyle.current.copy(color = iceBlue),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                OutlinedTextField(
                    value = day,
                    onValueChange = {
                        if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                            if ((it.toIntOrNull() ?: 0) in 1..31) day = it
                            else errorMessage = context.getString(R.string.invalid_day)
                        }
                    },
                    label = { Text(getTranslatedString(R.string.day), color = iceBlue) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = outlinedColors(),
                    textStyle = LocalTextStyle.current.copy(color = iceBlue),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = month,
                    onValueChange = {
                        if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                            if ((it.toIntOrNull() ?: 0) in 1..12) month = it
                            else errorMessage = context.getString(R.string.invalid_month)
                        }
                    },
                    label = { Text(getTranslatedString(R.string.month), color = iceBlue) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = outlinedColors(),
                    textStyle = LocalTextStyle.current.copy(color = iceBlue),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = year,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            val entered = it.toIntOrNull() ?: 0
                            if (currentYear - entered >= 18) year = it
                            else errorMessage = context.getString(R.string.underage)
                        }
                    },
                    label = { Text(getTranslatedString(R.string.year), color = iceBlue) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = outlinedColors(),
                    textStyle = LocalTextStyle.current.copy(color = iceBlue),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(getTranslatedString(R.string.email), color = iceBlue) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = outlinedColors(),
                textStyle = LocalTextStyle.current.copy(color = iceBlue),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(getTranslatedString(R.string.password), color = iceBlue) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            icon,
                            contentDescription = getTranslatedString(R.string.show_password),
                            tint = iceBlue
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = outlinedColors(),
                textStyle = LocalTextStyle.current.copy(color = iceBlue),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val birth = "$day.$month.$year"
                    val formattedName = formatName(name)
                    val formattedSurname = formatName(surname)

                    if (isPasswordValid(password)) {
                        authViewModel.setBirthDate(birth)
                        authViewModel.register(
                            email = email,
                            password = password,
                            name = formattedName,
                            surname = formattedSurname
                        ) { success, error ->
                            if (success) onRegisterSuccess()
                            else errorMessage =
                                error ?: context.getString(R.string.registration_failed)
                        }
                    } else {
                        errorMessage = context.getString(R.string.invalid_password)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .border(1.dp, iceBlue, RoundedCornerShape(30.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = iceBlue
                )
            ) {
                Text(
                    getTranslatedString(R.string.register),
                    color = iceBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Text(
                text = getTranslatedString(R.string.have_account_login),
                color = iceBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onLoginClick() }
                    .padding(8.dp)
            )
        }
    }
}

fun isPasswordValid(password: String): Boolean {
    return password.length >= 6 && password.any { it.isDigit() } && password.any { it.isLetter() }
}

fun formatName(input: String): String {
    return input.lowercase().replaceFirstChar { it.uppercase() }
}

@Composable
fun outlinedColors() = TextFieldDefaults.colors(
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedIndicatorColor = Color(0xFF74BBFB),
    focusedIndicatorColor = Color(0xFF74BBFB),
    cursorColor = Color(0xFF74BBFB),
    disabledContainerColor = Color.Transparent
)
