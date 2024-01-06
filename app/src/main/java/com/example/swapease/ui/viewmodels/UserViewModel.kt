package com.example.swapease.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _userImageUrl = MutableLiveData<String?>()
    val userImageUrl: LiveData<String?> get() = _userImageUrl

    fun getUserImageUrl(userUid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = userRepository.getUserImageUrl(userUid)
                _userImageUrl.postValue(imageUrl)
            } catch (e: Exception) {
                // Hata durumu ile ilgili işlemleri buraya ekleyebilirsiniz
                _userImageUrl.postValue(null)
            }
        }
    }
}