package com.example.swapease.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.swapease.ui.activities.DashboardActivity
import com.example.swapease.R
import com.example.swapease.data.models.User
import com.example.swapease.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.goToRegister.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            view.findNavController().navigate(action)
        }

        binding.singIn.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Successful sign-in, redirect to DashboardActivity
                            startActivity(
                                Intent(
                                    requireActivity(),
                                    DashboardActivity::class.java
                                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            displayToast("Email sign-in successful")
                            requireActivity().finish()
                        } else {
                            // Sign-in failed, display error message
                            displayToast("Sign-in failed: ${task.exception?.message}")
                        }
                    }
            } else {
                // Warn the user if email or password is empty
                displayToast("Email or password cannot be empty")
            }
        }

        // Initialize sign in options the client-id is copied form google-services.json file
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
        binding.btSignIn.setOnClickListener { // Initialize sign in intent
            val intent: Intent = googleSignInClient.signInIntent
            // Start activity for result
            startActivityForResult(intent, 100)
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
                    requireActivity(),
                    DashboardActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            requireActivity().finish()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val signInAccountTask: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            if (signInAccountTask.isSuccessful) {
                val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)
                if (googleSignInAccount != null) {
                    val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                        googleSignInAccount.idToken, null
                    )

                    firebaseAuth.signInWithCredential(authCredential)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                val userId = firebaseAuth.currentUser?.uid ?: ""
                                val username = googleSignInAccount.displayName ?: ""
                                val email = googleSignInAccount.email ?: ""
                                val newUser = User(uid = userId, username = username, email = email,null)

                                // Firestore'a kullanıcı bilgilerini ekle
                                firestore.collection("users").document(userId)
                                    .set(newUser)
                                    .addOnSuccessListener {
                                        startActivity(
                                            Intent(
                                                requireActivity(),
                                                DashboardActivity::class.java
                                            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                        displayToast("Firebase authentication successful")
                                        requireActivity().finish()
                                    }
                                    .addOnFailureListener {
                                        displayToast("Failed to add user to Firestore: ${it.message}")
                                    }
                            } else {
                                displayToast("Authentication Failed: ${task.exception?.message}")
                            }
                        }
                }
            } else {
                Toast.makeText(requireContext(), "Invalid e-mail or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayToast(s: String) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
    }
}