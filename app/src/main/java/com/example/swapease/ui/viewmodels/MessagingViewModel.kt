package com.example.swapease.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.Message
import com.example.swapease.data.models.Product
import com.example.swapease.ui.adapters.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MessagingViewModel : ViewModel() {
    private val _messageList = MutableLiveData<List<Pair<MessageAdapter.MessageType, Message>>>()
    val messageList: LiveData<List<Pair<MessageAdapter.MessageType, Message>>> get() = _messageList

    private val _otherUserName = MutableLiveData<String>()
    val otherUserName: LiveData<String> get() = _otherUserName

    private val fireStore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var userId: String
    private lateinit var otherUserId: String
    private lateinit var chatId: String

    fun init(param1: Product?, param2: String?) {
        userId = auth.currentUser?.uid.orEmpty()
        otherUserId = param1?.publisherUid ?: splitChatIdTwoParts(param2)?.second.orEmpty()
        chatId = if (userId < otherUserId) "$userId-$otherUserId" else "$otherUserId-$userId"

        createChatCollections(userId, otherUserId, chatId)
        observeChatMessages()
        fetchUserData(userId)
    }

    private fun createChatCollections(userId: String, otherUserId: String, chatId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Create the "chats" collection for the current user
                fireStore.collection("users").document(userId).collection("chats").document(chatId).set(mapOf("dummy" to "data")).await()

                // Create the "chats" collection for the other user
                fireStore.collection("users").document(otherUserId).collection("chats").document(chatId).set(mapOf("dummy" to "data")).await()
            } catch (e: Exception) {
                println("Error creating chat collections: $e")
            }
        }
    }

    private fun addNewMessage(message: Message) {
        viewModelScope.launch(Dispatchers.IO) {
            fireStore.collection("users").document(userId).collection("chats").document(chatId).collection("messages").add(message).await()
            fireStore.collection("users").document(otherUserId).collection("chats").document(chatId).collection("messages").add(message).await()
        }
    }

    private fun fetchUserData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = fireStore.collection("users").document(userId).get().await()

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

    fun sendMessage(messageText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newMessage = Message(userId, otherUserName.value.orEmpty(), otherUserId, messageText, System.currentTimeMillis(), null)
            addNewMessage(newMessage)
        }
    }

    private fun observeChatMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            fireStore.collection("users").document(userId).collection("chats").document(chatId).collection("messages").orderBy("timestamp")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        println(e)
                        return@addSnapshotListener
                    }

                    val messageList = mutableListOf<Pair<MessageAdapter.MessageType, Message>>()

                    for (dc in snapshots?.documentChanges.orEmpty()) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val message = dc.document.toObject(Message::class.java).apply { messageId = dc.document.id }

                                val messageType = if (message.senderUid == userId) {
                                    MessageAdapter.MessageType.ME
                                } else {
                                    MessageAdapter.MessageType.OTHER
                                }

                                messageList.add(messageType to message)
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

    private fun splitChatIdTwoParts(data: String?): Pair<String, String>? {
        data?.let { currentUserUid ->
            val parts = currentUserUid.split("-")

            if (parts.size == 2) {
                val (firstPart, secondPart) = parts

                return if (firstPart == auth.currentUser?.uid) {
                    Pair(firstPart, secondPart)
                } else {
                    Pair(secondPart, firstPart)
                }
            } else {
                println("Data could not be split into two parts.")
            }
        }
        return null
    }
}