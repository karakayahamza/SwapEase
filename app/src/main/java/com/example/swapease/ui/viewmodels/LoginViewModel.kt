package com.example.swapease.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.swapease.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

class LoginViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun signInWithEmailAndPassword(email: String, password: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess.invoke("Sign-in succesfull")
                } else {
                    onFailure.invoke("Sign-in failed: ${task.exception?.message}")
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
                    val newUser = User(uid = userId, username = username, email = email, null)

                    firestore.collection("users").document(userId)
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
}