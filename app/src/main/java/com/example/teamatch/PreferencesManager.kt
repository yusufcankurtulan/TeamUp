package com.example.teamatch

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("TeamatchPrefs", Context.MODE_PRIVATE)
    private val prefs = context.getSharedPreferences("TeamatchPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BIO = "userBio"
        private const val KEY_SOCIAL_LINK = "userSocialLink"
    }

    fun getUserName(): String = sharedPreferences.getString("userName", "") ?: ""
    fun setUserName(name: String) {
        sharedPreferences.edit { putString("userName", name) }
    }

    fun getUserSurname(): String = sharedPreferences.getString("userSurname", "") ?: ""
    fun setUserSurname(surname: String) {
        sharedPreferences.edit { putString("userSurname", surname) }
    }

    fun getUserPosition(): String = sharedPreferences.getString("userPosition", "") ?: ""
    fun setUserPosition(position: String) {
        sharedPreferences.edit { putString("userPosition", position) }
    }

    fun getUserHeight(): String = sharedPreferences.getString("userHeight", "") ?: ""
    fun setUserHeight(height: String) {
        sharedPreferences.edit { putString("userHeight", height) }
    }

    fun getUserWeight(): String = sharedPreferences.getString("userWeight", "") ?: ""
    fun setUserWeight(weight: String) {
        sharedPreferences.edit { putString("userWeight", weight) }
    }

    fun getPreferredFoot(): String = sharedPreferences.getString("preferredFoot", "") ?: ""
    fun setPreferredFoot(foot: String) {
        sharedPreferences.edit { putString("preferredFoot", foot) }
    }

    fun getMatchCount(): Int = sharedPreferences.getInt("matchCount", 0)
    fun setMatchCount(count: Int) {
        sharedPreferences.edit { putInt("matchCount", count) }
    }

    fun getUserRating(): Int = sharedPreferences.getInt("userRating", 55)
    fun setUserRating(rating: Int) {
        sharedPreferences.edit { putInt("userRating", rating) }
    }

    fun getBirthDate(): String {
        return sharedPreferences.getString("birth_date", "") ?: ""
    }

    fun setBirthDate(date: String) {
        sharedPreferences.edit { putString("birth_date", date) }
    }

    fun isUserLoggedIn(): Boolean = sharedPreferences.getBoolean("isUserLoggedIn", false)
    fun setUserLoggedIn(loggedIn: Boolean) {
        sharedPreferences.edit { putBoolean("isUserLoggedIn", loggedIn) }
    }

    fun isFirstLogin(): Boolean {
        return sharedPreferences.getBoolean("isFirstLogin", true)
    }

    fun setFirstLoginDone(value: Boolean) {
        sharedPreferences.edit { putBoolean("isFirstLogin", value) }
    }

    fun setFirstLogin(value: Boolean) {
        sharedPreferences.edit { putBoolean("isFirstLogin", value) }
    }

    fun isDarkThemeEnabled(): Boolean = prefs.getBoolean("isDarkTheme", false)
    fun setDarkThemeEnabled(enabled: Boolean) = prefs.edit { putBoolean("isDarkTheme", enabled) }

    fun getSelectedLanguage(): String = prefs.getString("language", "tr") ?: "tr"
    fun setSelectedLanguage(lang: String) = prefs.edit { putString("language", lang) }

    fun getUserDistrict(): String = sharedPreferences.getString("userDistrict", "") ?: ""
    fun setUserDistrict(district: String) {
        sharedPreferences.edit { putString("userDistrict", district) }
    }

    fun setProfileImageUri(uri: String) {
        sharedPreferences.edit { putString("profileImageUri", uri) }
    }

    fun getProfileImageUri(): String? {
        return sharedPreferences.getString("profileImageUri", null)
    }
    fun setProfilePhotoBase64(base64: String) {
        sharedPreferences.edit().putString("profile_photo_base64", base64).apply()
    }

    fun getProfileImageBase64(): String? {
        return sharedPreferences.getString("profile_photo_base64", null)
    }

    fun setSeenRequestIds(ids: Set<String>) {
        val joined = ids.joinToString(",")
        sharedPreferences.edit().putString("seen_request_ids", joined).apply()
    }

    fun getSeenRequestIds(): Set<String> {
        val stored = sharedPreferences.getString("seen_request_ids", "") ?: ""
        return if (stored.isEmpty()) emptySet() else stored.split(",").toSet()
    }

    fun setBio(bio: String) {
        sharedPreferences.edit { putString(KEY_BIO, bio) }
    }

    fun getBio(): String {
        return sharedPreferences.getString(KEY_BIO, "") ?: ""
    }

    fun setSocialLink(link: String) {
        sharedPreferences.edit { putString(KEY_SOCIAL_LINK, link) }
    }

    fun getSocialLink(): String {
        return sharedPreferences.getString(KEY_SOCIAL_LINK, "") ?: ""
    }
}

