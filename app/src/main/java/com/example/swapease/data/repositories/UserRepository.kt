package com.example.swapease.data.repositories

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getUserImageUrl(userUid: String): String? {
        return try {
            val userDocRef = db.collection("users").document(userUid)
            val documentSnapshot: DocumentSnapshot = userDocRef.get().await()

            if (documentSnapshot.exists()) {
                documentSnapshot.getString("userProfileImage")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}