package com.example.swapease.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MessageViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val messages = MutableLiveData<List<Message>>()

    fun getMessages(senderUid: String, receiverUid: String): LiveData<List<Message>> {
        // Firebase Firestore'dan veriyi çekme
        db.collection("messages")
            .whereEqualTo("senderUid", senderUid)
            .whereEqualTo("receiverUid", receiverUid)
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null) {
                    val messageList = mutableListOf<Message>()
                    for (document in querySnapshot.documents) {
                        val message = document.toObject(Message::class.java)
                        message?.let {
                            messageList.add(it)
                        }
                    }
                    messages.value = messageList
                }
            }

        return messages
    }
}
