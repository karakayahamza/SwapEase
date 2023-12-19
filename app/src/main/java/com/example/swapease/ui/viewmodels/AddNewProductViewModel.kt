package com.example.swapease.ui.viewmodels

import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.swapease.data.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import java.util.Locale

class AddNewProductViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val productsCollection = firestore.collection("products")

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

        if (currentUserUid != null) {
            val productData = hashMapOf(
                "publisherUid" to currentUserUid,
                "publisherName" to auth.currentUser?.displayName,
                "productName" to product.productName,
                "description" to product.description,
                "imageUrl" to product.imageUrl,
                "addedDate" to addedDate
            )

            productsCollection.add(productData)
                .addOnSuccessListener { documentReference ->
                    onSuccess.invoke()
                }
                .addOnFailureListener {
                    onFailure.invoke()
                }
        } else {
            onFailure.invoke()
        }
    }

    /*
    private fun updateProductList(
        currentUserUid: String,
        productId: String,
        product: Product,
        onSuccess: () -> Unit
    ) {
        // Kullanıcının mevcut ürün listesini alın
        firestore.collection("users").document(currentUserUid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                val productList = user?.products?.toMutableList() ?: mutableListOf()

                // Yeni ürünü listeye ekleyin
                val newProduct = Product(productId, currentUserUid, product.publisherName, product.productName, product.description, product.imageUrl,product.addedDate)
                productList.add(newProduct)

                // Güncellenmiş ürün listesini Firestore'a kaydedin
                firestore.collection("users").document(currentUserUid)
                    .update("products", productList)
                    .addOnSuccessListener {
                        onSuccess.invoke()
                    }
                    .addOnFailureListener {
                        // Hata durumunda onFailure çağrılabilir
                    }
            }
            .addOnFailureListener {
                // Hata durumunda onFailure çağrılabilir
            }
    }*/
}
