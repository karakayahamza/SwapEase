package com.example.swapease.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.Product
import com.example.swapease.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class UserDashBoardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _uid = MutableLiveData<String>()
    val uid: LiveData<String> get() = _uid

    private val _userProfileImage = MutableLiveData<String>()
    val userProfileImage: LiveData<String> get() = _userProfileImage

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> get() = _statusMessage

    private var _user = MutableLiveData<User>()
    val user: MutableLiveData<User> get() = _user


    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        getAllProducts()
        updateUid()
        loadUserProfileImage()
        getUserInfo(auth.currentUser!!.uid)
    }

    private fun getUserInfo(userUid : String){
        Log.d(TAG,userUid)
        var user: User
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.document(userUid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Belge varsa, alan değerlerini alabilirsiniz
                    val userEmail = documentSnapshot.getString("email")
                    val userProfileImage = documentSnapshot.getString("userProfileImage") ?: ""
                    val userName = documentSnapshot.getString("username") ?: ""
                    //val completedSwapes = Random.nextInt(1, 6)
                    //val rating = String.format("%.1f", Random.nextDouble(2.0, 5.0)).toDouble()

                    user = User(
                        uid = userUid,
                        username = userName,
                        email = userEmail,
                        userProfileImage = userProfileImage,
                        completedSwapes = 1,
                        rating =  3.5
                    )
                    println(user.completedSwapes.toString()+user.rating.toString())

                    _user.postValue(user)


                } else {
                    Log.d(TAG,"Document not found.")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG,"Document not found: $exception")
            }


    }


    private fun getAllProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserUid = auth.currentUser?.uid

                val querySnapshot = db.collection("products")
                    .whereEqualTo("publisherUid", currentUserUid)
                    .get()
                    .await()

                val productList = mutableListOf<Product>()

                for (document in querySnapshot.documents) {
                    val productId = document.id
                    val publisherName = auth.currentUser?.displayName
                    val publisherUid = document.getString("publisherUid") ?: ""
                    val productName = document.getString("productName") ?: ""
                    val description = document.getString("description") ?: ""
                    val imageUrl = document.getString("imageUrl")
                    val category = document.getString("category")
                    val addedDate = document.getString("addedDate")

                    if (imageUrl != null) {
                        val product = Product(productId, publisherUid, publisherName, productName, description, category, imageUrl, addedDate)
                        productList.add(product)
                    }
                }

                _products.postValue(productList)

                val displayName = auth.currentUser?.uid
                val result: String = displayName?.take(5) ?: ""
                _uid.postValue("@$result")

                _isLoading.postValue(false)

            } catch (e: Exception) {
                Log.e(TAG, "Error getting documents", e)
                _isLoading.postValue(false)
            }
        }
    }

    private fun updateUid() {
        val displayName = auth.currentUser?.uid
        val result: String = displayName?.take(5) ?: ""
        _uid.value = "@$result"
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("products")
                    .document(productId)
                    .delete()
                    .await()

                getAllProducts()

            } catch (e: Exception) {
                Log.e(TAG, "Error deleting product", e)
                _statusMessage.postValue("Error deleting product")
            }
        }
    }

    fun uploadImageToFirebaseStorage(imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val storageRef = storage.reference
                val imageRef = storageRef.child("profile_images/${auth.currentUser?.uid}.jpg")

                val taskSnapshot = imageRef.putFile(imageUri).await()

                Log.d(TAG, "Image upload successful: ${taskSnapshot.metadata?.path}")

                val uri = taskSnapshot.storage.downloadUrl.await()

                updateUserProfileImage(uri.toString())

            } catch (e: Exception) {
                Log.e(TAG, "Image upload error", e)
                _statusMessage.postValue("Error uploading image")
            }
        }
    }

    private fun updateUserProfileImage(imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userDocRef = db.collection("users").document(auth.currentUser?.uid ?: "")
                userDocRef.update("userProfileImage", imageUrl).await()

                Log.d(TAG, "Profile image update successful")
                loadUserProfileImage()

            } catch (e: Exception) {
                Log.e(TAG, "Profile image update error", e)
                _statusMessage.postValue("Error updating profile image")
            }
        }
    }

    private fun loadUserProfileImage() {
        val userDocRef = db.collection("users").document(auth.currentUser?.uid ?: "")
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val imageUrl = documentSnapshot.getString("userProfileImage") ?: ""

                if (imageUrl.isBlank() && auth.currentUser?.photoUrl != null) {
                    _userProfileImage.value = auth.currentUser?.photoUrl.toString()
                } else {
                    _userProfileImage.value = imageUrl
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading user profile image", e)
                _statusMessage.value = "Error loading profile image"
            }
    }

    companion object {
        const val TAG = "UserDashboardViewModel"
    }
}