package com.example.swapease.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.swapease.data.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserDashBoardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            db.collection("products")
                .whereEqualTo("publisherUid", currentUserUid)
                .addSnapshotListener { querySnapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Error getting documents", e)
                        // Hata durumunda işlemleri gerçekleştir
                        return@addSnapshotListener
                    }

                    val productList = mutableListOf<Product>()
                    for (document in querySnapshot!!) {
                        val productId = document.id
                        val sellerUid = document.getString("publisherUid") ?: ""
                        val productName = document.getString("productName") ?: ""
                        val description = document.getString("description") ?: ""
                        val imageUrl = document.getString("imageUrl") ?: ""
                        val product =
                            Product(productId, sellerUid, productName, description, imageUrl)
                        productList.add(product)
                    }
                    _products.value = productList
                }
        }
    }

    fun deleteProduct(productId: String) {
        db.collection("products")
            .document(productId)
            .delete()
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting product", e)
            }
    }

    companion object {
        const val TAG = "UserDashBoardViewModel"
    }
}