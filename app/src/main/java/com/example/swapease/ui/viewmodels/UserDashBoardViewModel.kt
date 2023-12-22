package com.example.swapease.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.swapease.data.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserDashBoardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _uid = MutableLiveData<String>()
    val uid: LiveData<String> get() = _uid

    init {
        getAllProducts()
    }

    fun getAllProducts() {
        val currentUserUid = auth.currentUser?.uid

        db.collection("products")
            .whereEqualTo("publisherUid", currentUserUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val productList = mutableListOf<Product>()

                for (document in querySnapshot.documents) {
                    val productId = document.id
                    val publisherName = auth.currentUser?.displayName
                    val publisherUid = document.getString("publisherUid") ?: ""
                    val productName = document.getString("productName") ?: ""
                    val description = document.getString("description") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""

                    val product = Product(productId, publisherUid, publisherName, productName, description, imageUrl)
                    productList.add(product)
                }

                _products.value = productList

                val displayName = auth.currentUser?.uid
                val result: String = displayName?.take(5) ?: ""
                _uid.value = "@${result}"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents", e)
                // Hata durumunda işlemleri gerçekleştir
            }
    }

    fun deleteProduct(productId: String) {
        db.collection("products")
            .document(productId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Product deleted successfully")
                getAllProducts()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting product", e)
                // Ürün silme başarısız olduysa yapılacak işlemler
            }
    }

    fun updateUserProfileImage(imageUrl: String) {
        val userDocRef = db.collection("users").document(auth.currentUser?.uid ?: "")
        userDocRef.update("userProfileImage", imageUrl)
            .addOnSuccessListener {
                Log.d(TAG, "Profil resmi güncelleme başarılı")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Profil resmi güncelleme hatası", e)
            }
    }

    fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef =
            storageRef.child("profile_images/${FirebaseAuth.getInstance().currentUser?.uid}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "Resim yükleme başarılı: ${taskSnapshot.metadata?.path}")

                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    updateUserProfileImage(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Resim yükleme hatası", e)
            }
    }

    companion object {
        const val TAG = "UserDashboardViewModel"
    }
}
