package com.example.swapease.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.Product
import com.example.swapease.data.repositories.ProductRepository
import kotlinx.coroutines.launch


/*class ProductAddViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _isProductAdded = MutableLiveData<Boolean>()
    val isProductAdded: LiveData<Boolean> get() = _isProductAdded

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.uploadProduct(product)
                _isProductAdded.value = true
            } catch (e: Exception) {
                _isProductAdded.value = false
            }
        }
    }
}*/




