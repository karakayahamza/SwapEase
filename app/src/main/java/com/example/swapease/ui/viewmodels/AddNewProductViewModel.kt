package com.example.swapease.ui.viewmodels

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.swapease.data.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import java.util.Locale

class AddNewProductViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val productsCollection = FirebaseFirestore.getInstance().collection("products")
    private val storage = FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var userNameFromFirebase = ""

    init {
        getUserName(auth.currentUser?.uid.toString()) { userName ->
            userNameFromFirebase = userName
        }
    }

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

    fun addItemToDatabase(product: Product, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val currentUserUid = auth.currentUser?.uid
        val addedDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

        val userName : String = if (auth.currentUser?.displayName != "" ){
            auth.currentUser?.displayName.toString()
        } else{
            userNameFromFirebase
        }

            Log.d("USERNAME ",userName)

        if (currentUserUid != null) {
            val productData = hashMapOf(
                "publisherUid" to currentUserUid,
                "publisherName" to userName,
                "productName" to product.productName,
                "description" to product.description,
                "category" to product.category,
                "imageUrl" to product.imageUrl,
                "addedDate" to addedDate
            )

            productsCollection.add(productData)
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
                .addOnFailureListener {
                    onFailure.invoke()
                }
        } else {
            onFailure.invoke()
        }
    }

    private fun getUserName(userId: String, callback: (String) -> Unit) {
        val userDocRef: DocumentReference = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                val userName = if (document != null && document.exists()) {
                    document.getString("username").toString()
                } else {
                    "Unknown"
                }
                callback(userName)
            }
            .addOnFailureListener { exception ->
                callback("Unknown")
            }
    }
}