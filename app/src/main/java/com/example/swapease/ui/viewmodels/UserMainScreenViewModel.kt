package com.example.swapease.ui.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.Product
import com.example.swapease.utils.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserMainScreenViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products
    private val _errorEvent = MutableLiveData<Event<Exception>>()
    val errorEvent: LiveData<Event<Exception>> get() = _errorEvent
    fun getAllProducts() {
        viewModelScope.launch {
            try {
                val querySnapshot = productsCollection.get().await()
                val productList = mutableListOf<Product>()
                for (document in querySnapshot) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                _products.value = productList
            } catch (e: Exception) {
                // Handle exceptions
                handleError(e)
            }
        }
    }
    private fun handleError(exception: Exception) {
        // Notify observers about the error using the MutableLiveData
        _errorEvent.value = Event(exception)
        Log.e(TAG, "Error getting products", exception)
    }

    companion object {
        const val TAG = "UserMainScreenViewModel"
    }
}