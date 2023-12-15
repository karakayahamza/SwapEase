package com.example.swapease.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.Message
import com.example.swapease.data.repositories.FirebaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(private val repository: FirebaseRepository) : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    fun sendMessage(senderUid: String, receiverUid: String, text: String) {
        val timestamp = System.currentTimeMillis()
        val message = Message(senderUid, receiverUid, text, timestamp)

        viewModelScope.launch(Dispatchers.IO) {
            repository.sendChatMessage(message)
        }
    }

    fun loadChatMessages(senderUid: String, receiverUid: String) {
        val query = repository.getChatMessages(senderUid, receiverUid)
        query.addSnapshotListener { snapshot, _ ->
            val messages = snapshot?.toObjects(Message::class.java)
            _messages.value = messages!!
        }
    }
}