package com.example.swapease.data.repositories

import com.example.swapease.data.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun sendChatMessage(message: Message) {
        db.collection("messages")
            .add(message)
            .await()
    }

    fun getChatMessages(senderUid: String, receiverUid: String): Query {
        return db.collection("messages")
            .whereEqualTo("senderUid", senderUid)
            .whereEqualTo("receiverUid", receiverUid)
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }
}