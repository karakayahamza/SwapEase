package com.example.swapease.ui.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun addUserToFirestore(uid: String, username: String, email: String) {
        val user = hashMapOf(
            "uid" to uid,
            "username" to username,
            "email" to email,
            "products" to emptyList<String>(),
            "chatBox" to hashMapOf(
                "chatBoxId" to "",
                "participants" to emptyList<String>()
            )
        )

        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                // Başarıyla eklendi
            }
            .addOnFailureListener { e ->
                // Hata durumu
            }
    }

    fun signInWithEmailAndPassword(email: String, password: String, onComplete: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun signInWithGoogle(idToken: String, onComplete: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun getCurrentUserUsername(onComplete: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: ""
                    onComplete(username)
                }
        }
    }

    fun getCurrentUserChatBoxId(onComplete: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val chatBoxId = document.getString("chatBox.chatBoxId") ?: ""
                    onComplete(chatBoxId)
                }
        }
    }

    fun updateUserChatBoxId(chatBoxId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .update("chatBox.chatBoxId", chatBoxId)
                .addOnSuccessListener {
                    // Başarıyla güncellendi
                }
                .addOnFailureListener { e ->
                    // Hata durumu
                }
        }
    }
}
