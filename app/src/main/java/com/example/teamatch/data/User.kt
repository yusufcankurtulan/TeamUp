package com.example.teamatch.data

data class User(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val height: Int? = null,
    val weight: Int = 0,
    val position: String = "",
    val preferredFoot: String = "",
    val birthDate: String = "",
    val rating: Double = 50.0,
    val district: String = "",
    val matchCount: Int = 0,
    val uid: String = "",
    val bio: String = "",
    val socialLink: String = "",
    var isFirstLogin: Boolean = true,
    val profilePhotoBase64: String = ""
)

