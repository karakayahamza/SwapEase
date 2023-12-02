package com.example.swapease.data.repositories

import com.example.swapease.data.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getAllProducts(): List<Product> {
        return try {
            firestore.collection("products")
                .get()
                .await()
                .toObjects(Product::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserProducts(userUid: String): List<Product> {
        return try {
            firestore.collection("products")
                .whereEqualTo("sellerUid", userUid)
                .get()
                .await()
                .toObjects(Product::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
