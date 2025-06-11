package com.example.teamatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class EditProfileViewModel(
    initialPosition: String,
    initialBio: String,
    initialSocial: String
) : ViewModel() {
    val position = mutableStateOf(initialPosition)
    val bio = mutableStateOf(initialBio)
    val socialLink = mutableStateOf(initialSocial)
    val profilePhotoBase64 = mutableStateOf("")


    fun saveProfileChanges(uid: String, onComplete: () -> Unit) {
        val updates = mapOf(
            "position" to position.value,
            "bio" to bio.value,
            "socialLink" to socialLink.value,
            "profilePhotoBase64" to profilePhotoBase64.value

        )

        Firebase.firestore.collection("users").document(uid).update(updates)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}

class EditProfileViewModelFactory(
    private val initialPosition: String,
    private val initialBio: String,
    private val initialSocial: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(initialPosition, initialBio, initialSocial) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

