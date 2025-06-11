package com.example.teamatch.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.teamatch.localization.LocalAppLocale
import java.util.*

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    @Composable
    fun getTranslatedString(id: Int, vararg formatArgs: Any): String {
        val context = LocalContext.current
        val locale = LocalAppLocale.current
        return getTranslatedString(context, id, locale, *formatArgs)
    }

    fun getTranslatedString(context: Context, id: Int, locale: Locale, vararg formatArgs: Any): String {
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config).resources.getString(id, *formatArgs)
    }

    fun translatePosition(value: String, locale: Locale): String {
        return when (value.lowercase(Locale.ROOT)) {
            "forvet" -> if (locale.language == "en") "Striker" else "Forvet"
            "orta saha" -> if (locale.language == "en") "Midfielder" else "Orta Saha"
            "defans" -> if (locale.language == "en") "Defender" else "Defans"
            "kaleci" -> if (locale.language == "en") "Goalkeeper" else "Kaleci"
            else -> value
        }
    }

    fun translateFoot(value: String, locale: Locale): String {
        return when (value.lowercase(Locale.ROOT)) {
            "sağ" -> if (locale.language == "en") "Right" else "Sağ"
            "sol" -> if (locale.language == "en") "Left" else "Sol"
            "iki" -> if (locale.language == "en") "Both" else "İki"
            else -> value
        }
    }


}
