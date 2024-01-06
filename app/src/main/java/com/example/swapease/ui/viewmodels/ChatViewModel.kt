package com.example.swapease.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.swapease.data.models.ChatBox
import com.example.swapease.ui.adapters.ChatListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatViewModel : ViewModel() {
    private val chatBoxesLiveData = MutableLiveData<List<ChatBox>>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        // ViewModel oluşturulduğunda verileri yükle
        loadChatBoxes()
    }

    // LiveData'nin getter'ı
    fun getChatBoxesLiveData(): LiveData<List<ChatBox>> {
        return chatBoxesLiveData
    }

    // Verileri yükleme metodu
    private fun loadChatBoxes() {
        val currentUserId = auth.currentUser?.uid
        currentUserId?.let {
            // Verileri Firestore'dan al
            val userChatsCollection = db.collection("users").document(it).collection("chats")

            userChatsCollection.get()
                .addOnSuccessListener { documents ->
                    val chatBoxes = mutableListOf<ChatBox>()
                    for (document in documents) {
                        val chatId = document.id
                        chatBoxes.add(ChatBox(chatId))
                    }
                    chatBoxesLiveData.value = chatBoxes
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting chat documents: ", exception)
                }
        }
    }

    fun getUserData(userId: String, onSuccess: (String, String) -> Unit, onFailure: (Exception) -> Unit) {
        // Firebase Firestore bağlantısını oluştur
        val userDocRef = db.collection("users").document(userId)

        // Kullanıcı belgesini getir
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Belge başarıyla alındığında callback'i çağır
                    val username = documentSnapshot.getString("username") ?: ""
                    val userProfileImage = documentSnapshot.getString("userProfileImage") ?: ""
                    onSuccess(username, userProfileImage)
                } else {
                    // Belge bulunamazsa hata durumunda callback'i çağır
                    onFailure(Exception("User document not found"))
                }
            }
            .addOnFailureListener { exception ->
                // Hata durumunda callback'i çağır
                onFailure(exception)
            }
    }

    fun splitChatIdTwoParts(data: String?): Pair<String, String>? {
        // Split the chat ID into two parts based on the '-' character
        val parts = data?.split("-")

        // If there are two parts
        if (parts?.size == 2) {
            // Get the first and second parts
            val (firstPart, secondPart) = parts

            // If the first part is equal to the current user's ID
            val (currentUserId, otherUserId) = if (firstPart == auth.currentUser?.uid) {
                Pair(firstPart, secondPart)
            } else {
                Pair(secondPart, firstPart)
            }

            // Return the two-part IDs
            return Pair(currentUserId, otherUserId)
        } else {
            // If unable to split into two parts or an error occurs
            Log.e(TAG, "Data could not be split into two parts.")
        }
        return null
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}