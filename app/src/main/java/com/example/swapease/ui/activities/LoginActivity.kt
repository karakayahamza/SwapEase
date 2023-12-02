package com.example.swapease.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.swapease.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}
        /*

        // Initialize sign in options the client-id is copied form google-services.json file
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, googleSignInOptions)
        binding.btSignIn.setOnClickListener { // Initialize sign in intent
            val intent: Intent = googleSignInClient.signInIntent
            // Start activity for result
            startActivityForResult(intent, 100)
        }



        binding.signUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            //val username = binding.etUsername.text.toString()
            val username = "Usernam"


            // Check if email and password are not empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerWithEmailAndPassword(email, password,username)
            } else {
                displayToast("Please enter both email and password")
            }
        }

        // Initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // Initialize firebase user
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
        // Check condition
        if (firebaseUser != null) {
            // When user already sign in redirect to profile activity
            startActivity(
                Intent(
                    this@LoginActivity,
                    ProfileActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun registerWithEmailAndPassword(email: String, password: String,username: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, redirect to profile activity
                    startActivity(
                        Intent(
                            this@LoginActivity,
                            ProfileActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )

                    val user = firebaseAuth.currentUser
                    // Add username to Firestore
                    addUsernameToFirestore(user?.uid, username)

                    sendVerificationEmail()
                    displayToast("Registration successful")
                } else {
                    // Registration failed, display error message
                    displayToast("Registration failed: ${task.exception?.message}")
                }
            }
    }
    private fun addUsernameToFirestore(userId: String?, username: String) {
        val userMap = hashMapOf(
            "username" to username
        )

        userId?.let {
            firestore.collection("users").document(it)
                .set(userMap)
                .addOnSuccessListener {
                    // Username added to Firestore successfully
                }
                .addOnFailureListener { e ->
                    // Handle failure to add username to Firestore
                }
        }
    }

    private fun sendVerificationEmail() {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    displayToast("Verification email sent. Please check your email.")
                } else {
                    displayToast("Failed to send verification email: ${task.exception?.message}")
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check condition
        if (requestCode == 100) {
            // When request code is equal to 100 initialize task
            val signInAccountTask: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            // check condition
            if (signInAccountTask.isSuccessful) {
                // When google sign in successful initialize string
                val s = "Google sign in successful"
                // Display Toast
                displayToast(s)
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                            googleSignInAccount.idToken, null
                        )
                        // Check credential
                        firebaseAuth.signInWithCredential(authCredential)
                            .addOnCompleteListener(this) { task ->
                                // Check condition
                                if (task.isSuccessful) {
                                    // When task is successful redirect to profile activity
                                    startActivity(
                                        Intent(
                                            this@LoginActivity,
                                            ProfileActivity::class.java
                                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                    // Display Toast
                                    displayToast("Firebase authentication successful")
                                } else {
                                    // When task is unsuccessful display Toast
                                    displayToast(
                                        "Authentication Failed :" + task.exception!!.message
                                    )
                                }
                            }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun displayToast(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }
}
         */