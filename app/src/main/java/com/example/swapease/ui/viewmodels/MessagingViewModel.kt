package com.example.swapease.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.Message
import com.example.swapease.data.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MessagingViewModel : ViewModel() {

    private val _messageList = MutableLiveData<List<Pair<MessageAdapter.MessageType, Message>>>()
    val messageList: LiveData<List<Pair<MessageAdapter.MessageType, Message>>> get() = _messageList

    private val _otherUserName = MutableLiveData<String>()
    val otherUserName: LiveData<String> get() = _otherUserName

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var userId: String
    private lateinit var otherUserId: String
    private lateinit var chatId: String

    fun init(param1: Product?, param2: String?) {
        userId = auth.currentUser?.uid ?: ""
        otherUserId = param1?.publisherUid ?: splitChatIdTwoParts(param2)?.second ?: ""
        chatId = if (userId < otherUserId) "$userId-$otherUserId" else "$otherUserId-$userId"

        observeChatMessages()
        fetchUserData(userId)
    }

    fun addNewMessage(message: Message) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                firestore.collection("users")
                    .document(userId)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(message)
                    .await()

                firestore.collection("users")
                    .document(otherUserId)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(message)
                    .await()
            }
        }
    }

    fun fetchUserData(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val document = firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    if (document.exists()) {
                        _otherUserName.postValue(document.getString("username"))
                    } else {
                        println("Document not found or doesn't exist.")
                    }
                } catch (e: Exception) {
                    println("Error fetching data: $e")
                }
            }
        }
    }

    fun sendMessage(messageText: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val newMessage = Message(userId, otherUserName.value.orEmpty(), otherUserId, messageText, System.currentTimeMillis(), null)
                addNewMessage(newMessage)
            }
        }
    }

    fun observeChatMessages() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                firestore.collection("users")
                    .document(userId)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .orderBy("timestamp")
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            println(e)
                            return@addSnapshotListener
                        }

                        val messageList = mutableListOf<Pair<MessageAdapter.MessageType, Message>>()

                        for (dc in snapshots?.documentChanges.orEmpty()) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val message = dc.document.toObject(Message::class.java)
                                    message.messageId = dc.document.id

                                    if (message.senderUid == userId) {
                                        messageList.add(MessageAdapter.MessageType.ME to message)
                                    } else {
                                        messageList.add(MessageAdapter.MessageType.OTHER to message)
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    // Handle modified
                                }
                                DocumentChange.Type.REMOVED -> {
                                    // Handle removed
                                }
                            }
                        }

                        messageList.sortBy { it.second.timestamp }

                        _messageList.postValue(messageList)
                    }
            }
        }
    }


    private fun splitChatIdTwoParts(data: String?): Pair<String, String>? {
        var currentUserId = ""
        var otherUserId = ""

        val current = auth.currentUser?.uid

        data?.let { currentUserUid ->
            val parts = currentUserUid.split("-")

            if (parts.size == 2) {
                val firstPart = parts[0]
                val secondPart = parts[1]

                if (firstPart == current) {
                    currentUserId = firstPart
                    otherUserId = secondPart
                } else {
                    currentUserId = secondPart
                    otherUserId = firstPart
                }

                return Pair(currentUserId, otherUserId)
            } else {
                println("Data could not be split into two parts.")
            }
        }

        return null
    }
}