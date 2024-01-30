package com.example.swapease.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _userImageUrl = MutableLiveData<String?>()
    val userImageUrl: LiveData<String?> get() = _userImageUrl

    fun getUserImageUrl(userUid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userDocRef = db.collection("users").document(userUid)
                val documentSnapshot: DocumentSnapshot = userDocRef.get().await()

                if (documentSnapshot.exists()) {
                    val imageUrl = documentSnapshot.getString("userProfileImage")
                    _userImageUrl.postValue(imageUrl)
                } else {
                    _userImageUrl.postValue(null)
                }
            } catch (e: Exception) {
                // Handle the error here if needed
                _userImageUrl.postValue(null)
            }
        }
    }
}