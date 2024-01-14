package com.example.swapease.ui.viewmodels

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.swapease.data.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> get() = _loginResult

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _loginResult.value = LoginResult(success = "Sign-in successful")
            }
            .addOnFailureListener { e ->
                _loginResult.value = LoginResult(error = "Sign-in failed: ${e.message}")
            }
    }

    suspend fun handleGoogleSignInResult(data: Intent?,
                                         onSuccess: (String) -> Unit,
                                         onFailure: (String) -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            val idToken = account?.idToken
            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

                if (isNewUser) {
                    val userId = auth.currentUser?.uid ?: ""
                    val username = auth.currentUser?.displayName ?: ""
                    val profileImage = auth.currentUser?.photoUrl ?: ""
                    val email = auth.currentUser?.email ?: ""
                    val newUser = User(uid = userId, username = username, email = email, profileImage.toString(), 0, 0.0)
                    addUserToFirestore(newUser)
                }

                onSuccess("Google sign-in successful!")
            } else {
                onFailure("Google sign-in failed.")
            }
        } catch (e: ApiException) {
            onFailure("Google sign-in failed. ${e.message}")
        }
    }

    private suspend fun addUserToFirestore(newUser: User) {
        try {
            val usersCollection = firestore.collection("users")
            newUser.uid?.let { usersCollection.document(it).set(newUser).await() }
        } catch (e: Exception) {
            Log.e("UserDashBoardViewModel", "Error adding user to Firestore: ${e.message}", e)
        }
    }



    data class LoginResult(val success: String? = null, val error: String? = null)

    companion object {
        private val TAG = "LoginViewModel"
    }
}