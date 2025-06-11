package com.example.teamatch.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.teamatch.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RequestViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _hasUnseenRequests = MutableStateFlow(false)
    val hasUnseenRequests: StateFlow<Boolean> = _hasUnseenRequests

    fun checkPendingRequests() {
        val uid = auth.currentUser?.uid ?: return
        val seenIds = preferencesManager.getSeenRequestIds()

        firestore.collection("inviteRequests")
            .whereEqualTo("toUserId", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                val currentIds = snapshot?.documents?.mapNotNull { it.id }?.toSet() ?: emptySet()
                val unseenIds = currentIds.subtract(seenIds)

                _hasUnseenRequests.value = unseenIds.isNotEmpty()

                Log.d("RequestViewModel", "🔁 Gelen ID'ler: $currentIds")
                Log.d("RequestViewModel", "📌 Görülen ID'ler: $seenIds")
                Log.d("RequestViewModel", "🔔 Badge Gösterilecek mi?: ${unseenIds.isNotEmpty()}")
            }
    }

    fun markRequestsAsSeen() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("inviteRequests")
            .whereEqualTo("toUserId", uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                val ids = snapshot.documents.mapNotNull { it.id }.toSet()
                preferencesManager.setSeenRequestIds(ids)
                _hasUnseenRequests.value = false

                Log.d("RequestViewModel", "📥 markRequestsAsSeen() ÇAĞRILDI - Görülen ID'ler: $ids")
            }
    }
}

class RequestViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RequestViewModel::class.java)) {
            return RequestViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
