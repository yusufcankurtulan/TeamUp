package com.example.teamatch.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(email: String, password: String): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logoutUser() {
        auth.signOut()
    }

    suspend fun createUserInFirestore(uid: String, user: User) {
        val userMap = mapOf(
            "name" to user.name,
            "surname" to user.surname,
            "email" to user.email,
            "height" to user.height,
            "weight" to user.weight,
            "position" to user.position,
            "preferredFoot" to user.preferredFoot,
            "birthDate" to user.birthDate,
            "profilePhotoUrl" to user.profilePhotoBase64,
            "rating" to user.rating,
            "matchCount" to user.matchCount,
            "uid" to user.uid,
            "isFirstLogin" to user.isFirstLogin
        )
        firestore.collection("users").document(uid).set(userMap).await()
    }

    suspend fun markFirstLoginComplete(uid: String) {
        firestore.collection("users").document(uid)
            .update("isFirstLogin", false)
            .await()
    }

    suspend fun getUserFromFirestore(uid: String): Result<User> {
        return try {
            val snapshot = Firebase.firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Kullanıcı verisi bulunamadı."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
