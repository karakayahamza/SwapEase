package com.example.swapease.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.swapease.data.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddNewProductViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val productsCollection = firestore.collection("products")
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products


    // Function to upload image to Firebase Storage
    fun uploadImageToFirebaseStorage(uri: Uri, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${currentUserUid}_${System.currentTimeMillis()}")
            val uploadTask = imageRef.putFile(uri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val imageUrl = task.result.toString()
                    onSuccess.invoke(imageUrl)
                } else {
                    onFailure.invoke()
                }
            }
        }
    }

    // Function to add item to Firestore
    fun addItemToDatabase(productName: String, description: String, imageUrl: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            val product = hashMapOf(
                "publisherUid" to currentUserUid,
                "productName" to productName,
                "description" to description,
                "imageUrl" to imageUrl
            )

            productsCollection.add(product)
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
                .addOnFailureListener { e ->
                    onFailure.invoke()
                }
        } else {
            onFailure.invoke()
        }
    }
}
