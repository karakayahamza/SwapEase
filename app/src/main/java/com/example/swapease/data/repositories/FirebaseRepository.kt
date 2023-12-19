package com.example.swapease.data.repositories

import com.example.swapease.data.models.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    // Using a suspending function for sending messages
    suspend fun sendChatMessage(message: Message) {
        db.collection("messages")
            .add(message)
            .await() // Make sure you have the necessary dependencies for `await()`
    }

    // Returning a Firestore Query for retrieving messages
    fun getChatMessages(senderUid: String, receiverUid: String): Query {
        return db.collection("messages")
            .whereEqualTo("senderUid", senderUid)
            .whereEqualTo("receiverUid", receiverUid)
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }
}