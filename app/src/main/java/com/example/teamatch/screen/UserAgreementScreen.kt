package com.example.teamatch.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.teamatch.PreferencesManager
import com.example.teamatch.util.LocaleHelper.getTranslatedString
import com.example.teamatch.R
import com.example.teamatch.components.BackgroundFootballIcons


@Composable
fun UserAgreementScreen(
    navController: NavController,
    preferencesManager: PreferencesManager
) {
    val isDarkTheme = preferencesManager.isDarkThemeEnabled()
    val baseColor = if (isDarkTheme) Color(0xFF74BBFB) else Color.Black
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val titleFont = FontFamily(Font(R.font.winter)) // Uygulama fontun
    val agreementText = getTranslatedString(R.string.user_agreement_content)
    val titleText = getTranslatedString(R.string.terms_privacy)
    val backText = getTranslatedString(R.string.back)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundFootballIcons(preferencesManager = preferencesManager)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titleText,
                    fontSize = 32.sp,
                    fontFamily = titleFont,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = agreementText,
                    fontSize = 16.sp,
                    color = textColor,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    border = BorderStroke(1.dp, baseColor),
                    shape = RoundedCornerShape(35.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = baseColor
                    )
                ) {
                    Text(text = backText, fontSize = 16.sp)
                }
            }
        }
    }
}
