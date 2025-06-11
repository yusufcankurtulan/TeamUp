package com.example.teamatch.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teamatch.PreferencesManager
import com.example.teamatch.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri
import com.example.teamatch.data.Match
import com.example.teamatch.data.User
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val authRepository = AuthRepository(FirebaseAuth.getInstance())

    val bio = mutableStateOf(preferencesManager.getBio())
    val socialLink = mutableStateOf(preferencesManager.getSocialLink())


    val isUserLoggedIn = mutableStateOf(preferencesManager.isUserLoggedIn())
    val isFirstLogin = mutableStateOf(preferencesManager.isFirstLogin())

    val firstName = mutableStateOf(preferencesManager.getUserName())
    val lastName = mutableStateOf(preferencesManager.getUserSurname())
    val fullName = derivedStateOf { "${firstName.value} ${lastName.value}".trim() }

    val userPosition = mutableStateOf(preferencesManager.getUserPosition())
    val userHeight = mutableStateOf(preferencesManager.getUserHeight())
    val userWeight = mutableStateOf(preferencesManager.getUserWeight())
    val preferredFoot = mutableStateOf(preferencesManager.getPreferredFoot())

    val matchCount = mutableIntStateOf(preferencesManager.getMatchCount())
    val userRating = mutableIntStateOf(preferencesManager.getUserRating())
    val birthDate = mutableStateOf(preferencesManager.getBirthDate())
    val profilePhotoBase64 = mutableStateOf("")

    val profileImageUri = mutableStateOf<Uri?>(null)
    val base64Photo = mutableStateOf(preferencesManager.getProfileImageBase64() ?: "")
    val userDistrict = mutableStateOf("")
    val selectedPositions = mutableStateOf<List<String>>(emptyList())
    val selectedFeet = mutableStateOf<List<String>>(emptyList())

    init {
        loadProfileImageFromPreferences()
    }

    fun setProfileImage(uri: Uri?) {
        profileImageUri.value = uri
        uri?.let {
            val bitmap = uriToBitmap(getApplication(), it)
            val base64 = bitmapToBase64(bitmap)
            base64Photo.value = base64
            preferencesManager.setProfilePhotoBase64(base64)
        }
    }

    private fun loadProfileImageFromPreferences() {
        val base64 = preferencesManager.getProfileImageBase64()
        val profilePhotoBase64 = mutableStateOf<String?>(null)
        if (!base64.isNullOrEmpty()) {
            profilePhotoBase64.value = base64
        } else {
            preferencesManager.getProfileImageUri()?.let { uriString ->
                profileImageUri.value = uriString.toUri()
            }
        }
    }



    fun setBirthDate(date: String) {
        birthDate.value = date
        preferencesManager.setBirthDate(date)
    }

    fun setUserDistrict(district: String) {
        userDistrict.value = district
    }


    fun setUserHeight(height: String) {
        userHeight.value = height
        preferencesManager.setUserHeight(height)
    }

    fun setUserWeight(weight: String) {
        userWeight.value = weight
        preferencesManager.setUserWeight(weight)
    }

    fun setUserDetails(position: String, height: String, weight: String, foot: String) {
        preferencesManager.setUserPosition(position)
        preferencesManager.setUserHeight(height)
        preferencesManager.setUserWeight(weight)
        preferencesManager.setPreferredFoot(foot)

        userPosition.value = position
        userHeight.value = height
        userWeight.value = weight
        preferredFoot.value = foot
    }

    fun setUserName(name: String, surname: String) {
        preferencesManager.setUserName(name)
        preferencesManager.setUserSurname(surname)
        firstName.value = name
        lastName.value = surname
    }

    fun updateMatchCount(newCount: Int) {
        preferencesManager.setMatchCount(newCount)
        matchCount.intValue = newCount
    }

    fun updateUserRating(newRating: Int) {
        preferencesManager.setUserRating(newRating)
        userRating.intValue = newRating
    }

    fun completeFirstLoginFlow() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                authRepository.markFirstLoginComplete(user.uid)
            }
            preferencesManager.setFirstLoginDone(false)
            isFirstLogin.value = false
        }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        surname: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password)
            if (result.isSuccess) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userData = User(
                        name = name,
                        surname = surname,
                        email = email,
                        height = 0,
                        weight = 0,
                        position = "",
                        preferredFoot = "",
                        birthDate = birthDate.value,
                        profilePhotoBase64 = "",
                        rating = 50.0,
                        matchCount = 0,
                        uid = user.uid,
                        isFirstLogin = true
                    )

                    authRepository.createUserInFirestore(user.uid, userData)

                    preferencesManager.setUserLoggedIn(true)
                    preferencesManager.setFirstLogin(true)
                    setUserName(name, surname)
                    isFirstLogin.value = true

                    onResult(true, null)
                }
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Kayıt başarısız")
            }
        }
    }


    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            if (result.isSuccess) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    setUserName(
                        preferencesManager.getUserName(),
                        preferencesManager.getUserSurname()
                    )
                    loadProfileImageFromPreferences()
                    isFirstLogin.value = preferencesManager.isFirstLogin()

                    Log.d("Login", "Giriş başarılı: UID=${user.uid}")
                    onResult(true, null)
                }
            } else {
                Log.e("Login", "Giriş başarısız: ${result.exceptionOrNull()?.message}")
                onResult(false, result.exceptionOrNull()?.message ?: "Giriş başarısız")
            }
        }
    }


    fun logout() {
        authRepository.logoutUser()

        preferencesManager.setUserLoggedIn(false)
        preferencesManager.setUserName("")
        preferencesManager.setUserSurname("")
        preferencesManager.setUserPosition("")
        preferencesManager.setUserHeight("")
        preferencesManager.setUserWeight("")
        preferencesManager.setPreferredFoot("")
        preferencesManager.setProfileImageUri("")

        firstName.value = ""
        lastName.value = ""
        userPosition.value = ""
        userHeight.value = ""
        userWeight.value = ""
        preferredFoot.value = ""
        userDistrict.value = ""
        selectedPositions.value = emptyList()
        selectedFeet.value = emptyList()
        profileImageUri.value = null
        base64Photo.value = ""
        isUserLoggedIn.value = false
    }

    fun updateUserDetailsToFirestore(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val userUpdates = mapOf(
                "position" to userPosition.value,
                "preferredFoot" to preferredFoot.value,
                "height" to userHeight.value.toIntOrNull(),
                "weight" to userWeight.value.toIntOrNull(),
                "district" to userDistrict.value
            )

            try {
                Firebase.firestore.collection("users").document(uid).update(userUpdates).await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("FirestoreUpdate", "Kullanıcı güncellenemedi", e)
                onComplete(false)
            }
        }
    }

    fun fetchNearbyPlayers(currentUserId: String, onResult: (List<User>) -> Unit) {
        Firebase.firestore.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val players = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                    .filter { it.uid != currentUserId }
                onResult(players)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }


    fun fetchAllMatches(onResult: (List<Match>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("matches").get().addOnSuccessListener { snapshot ->
            val matches = snapshot.documents.mapNotNull { doc ->
                val pitch = doc.getString("pitchName") ?: return@mapNotNull null
                val start = doc.getString("startTime") ?: return@mapNotNull null
                val end = doc.getString("endTime") ?: return@mapNotNull null
                val id = doc.id
                Match(id = id, pitchName = pitch, startTime = start, endTime = end)
            }
            onResult(matches)
        }
    }


    fun fetchUserDataFromFirestore(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val result = authRepository.getUserFromFirestore(uid)
            if (result.isSuccess) {
                val user = result.getOrNull()
                user?.let {
                    setUserName(it.name, it.surname)
                    setUserDetails(
                        it.position,
                        it.height.toString(),
                        it.weight.toString(),
                        it.preferredFoot
                    )
                    setBirthDate(it.birthDate)
                    updateUserRating(it.rating.toInt())
                    updateMatchCount(it.matchCount)

                    if (it.district.isNotEmpty()) {
                        setUserDistrict(it.district)
                    }

                    if (it.profilePhotoBase64.isNotEmpty()) {
                        profilePhotoBase64.value = it.profilePhotoBase64
                        preferencesManager.setProfilePhotoBase64(it.profilePhotoBase64)
                    }

                    bio.value = user.bio
                    socialLink.value = user.socialLink

                    preferencesManager.setBio(user.bio)
                    preferencesManager.setSocialLink(user.socialLink)
                }
                onComplete?.invoke()
            }
        }
    }
    private fun uriToBitmap(context: Application, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }
}
