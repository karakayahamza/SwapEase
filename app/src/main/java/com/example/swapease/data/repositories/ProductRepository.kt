package com.example.swapease.data.repositories

import com.example.swapease.data.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun addProductToDatabase(product: Product, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("products")
            .add(product)
            .addOnSuccessListener { documentReference ->
                onSuccess.invoke()
            }
            .addOnFailureListener { e ->
                onFailure.invoke("Error adding product: ${e.message}")
            }
    }

    fun getUserProducts(onSuccess: (List<Product>) -> Unit, onFailure: (String) -> Unit) {
        val currentUserUid = getCurrentUserId()

        if (currentUserUid != null) {
            db.collection("products")
                .whereEqualTo("sellerUid", currentUserUid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val products = mutableListOf<Product>()
                    for (document in querySnapshot.documents) {
                        val product = document.toObject(Product::class.java)
                        if (product != null) {
                            products.add(product)
                        }
                    }
                    onSuccess.invoke(products)
                }
                .addOnFailureListener { e ->
                    onFailure.invoke("Error getting user products: ${e.message}")
                }
        } else {
            onFailure.invoke("User not authenticated")
        }
    }

    fun getAllProducts(onSuccess: (List<Product>) -> Unit, onFailure: (String) -> Unit) {
        db.collection("products")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val products = mutableListOf<Product>()
                for (document in querySnapshot.documents) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        products.add(product)
                    }
                }
                onSuccess.invoke(products)
            }
            .addOnFailureListener { e ->
                onFailure.invoke("Error getting all products: ${e.message}")
            }
    }
}
