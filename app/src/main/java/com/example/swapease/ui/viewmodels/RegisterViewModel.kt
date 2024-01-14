package com.example.swapease.ui.viewmodels

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.Result

class RegisterViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    private val _registrationResult = MutableLiveData<Result<String>>()
    val registrationResult: LiveData<Result<String>> get() = _registrationResult

    private val _googleSignInResult = MutableLiveData<Result<String>>()
    val googleSignInResult: LiveData<Result<String>> get() = _googleSignInResult

    fun registerWithEmailAndPassword(email: String, password: String, username: String) {
        viewModelScope.launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val userId = firebaseAuth.currentUser?.uid ?: ""
                val newUser = User(uid = userId, username = username, email = email, null,0,0.0)
                fireStore.collection("users").document(userId).set(newUser).await()
                sendVerificationEmail()
                _registrationResult.value = Result.success("Registration successful")
            } catch (e: Exception) {
                _registrationResult.value = Result.failure(e)
            }
        }
    }


    fun signInWithGoogle(account: GoogleSignInAccount, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val authCredential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(authCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid ?: ""
                    val username = account.displayName ?: ""
                    val email = account.email ?: ""
                    val newUser = User(uid = userId, username = username, email = email, firebaseAuth.currentUser?.photoUrl.toString(),0,0.0)

                    fireStore.collection("users").document(userId)
                        .set(newUser)
                        .addOnSuccessListener {
                            onSuccess.invoke("Sign-in succesfull")
                        }
                        .addOnFailureListener {
                            onFailure.invoke("Failed to add user to Firestore: ${it.message}")
                        }
                } else {
                    onFailure.invoke("Authentication Failed: ${task.exception?.message}")
                }
            }
    }

    private suspend fun sendVerificationEmail() {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()?.await()
    }
}
