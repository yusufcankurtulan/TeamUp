package com.example.teamatch

import android.content.Context
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.teamatch.ui.theme.TeamatchTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import com.example.teamatch.localization.LocalAppLocale
import com.example.teamatch.util.LocaleHelper
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferencesManager(newBase)
        val language = prefs.getSelectedLanguage() ?: "tr"
        val updatedContext = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(updatedContext)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)

        setContent {
            val preferencesManager = PreferencesManager(this)
            var darkTheme by remember {
                mutableStateOf(preferencesManager.isDarkThemeEnabled())
            }
            var appLocale by remember { mutableStateOf(Locale(preferencesManager.getSelectedLanguage() ?: "tr")) }


            CompositionLocalProvider(LocalAppLocale provides appLocale) {
                    TeamatchTheme(darkTheme = darkTheme) {
                        val navController = rememberNavController()

                        AppNavigation(
                            navController = navController,
                            preferencesManager = preferencesManager,
                            darkTheme = darkTheme,
                            onThemeToggle = {
                                darkTheme = !darkTheme
                                preferencesManager.setDarkThemeEnabled(darkTheme)
                            },
                            onLanguageChange = { langCode ->
                                preferencesManager.setSelectedLanguage(langCode)
                                appLocale = Locale(langCode)
                            }
                        )
                    }
                }
        }
    }
}
