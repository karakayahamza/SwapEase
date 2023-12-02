package com.example.swapease.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.Product
import com.example.swapease.data.repositories.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>>
        get() = _products

    fun fetchProducts() {
        productsCollection.get()
            .addOnSuccessListener { result ->
                val productList = mutableListOf<Product>()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                _products.value = productList
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }
}


