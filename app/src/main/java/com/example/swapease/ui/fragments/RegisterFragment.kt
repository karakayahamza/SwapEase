package com.example.swapease.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.swapease.ui.activities.DashboardActivity
import com.example.swapease.R
import com.example.swapease.databinding.FragmentRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signUp.isClickable = false

        binding.goToRegister.setOnClickListener {
            val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            view.findNavController().navigate(action)

        }

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                binding.signUp.isClickable = isPasswordValid(editable.toString())
            }
        })

        // Initialize sign in options the client-id is copied form google-services.json file
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)


        binding.signUp.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            registerWithEmailAndPassword(email, password)
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
                    requireContext(),
                    DashboardActivity::class.java
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            requireActivity().finish()
        }

    }
    private fun registerWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Registration successful, redirect to profile activity
                    startActivity(
                        Intent(
                            requireContext(),
                            DashboardActivity::class.java
                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    sendVerificationEmail()
                    displayToast("Registration successful")
                    requireActivity().finish()
                } else {
                    // Registration failed, display error message
                    displayToast("Registration failed: ${task.exception?.message}")
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


    @SuppressLint("ResourceType")
    private fun isPasswordValid(password: String): Boolean {
        val condition = false
       //val name = binding.etusername.text.toString()
       //val email = binding.etEmail.text.toString()
        // val password = binding.etPassword.text.toString()

        /*if (name.isEmpty()) {
            binding.etusername.error = "Please Enter Full name"
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Please Enter Email"
        }*/

        // 8 character
        val is8char: Boolean = password.length >= 8
        binding.card1.setCardBackgroundColor(
            if (is8char) {
                Color.parseColor(getString(R.color.green))
            } else {
                Color.parseColor(getString(R.color.gray))
            }
        )

        // number
        val hasNum: Boolean = password.contains(Regex(".*[0-9].*"))
        binding.card2.setCardBackgroundColor(
            if (hasNum) {
                Color.parseColor(getString(R.color.green))
            } else {
                Color.parseColor(getString(R.color.gray))
            }
        )

        // upper case

        val hasUpper: Boolean = password.contains(Regex(".*[A-Z].*"))
        binding.card3.setCardBackgroundColor(
            if (hasUpper) {
                Color.parseColor(getString(R.color.green))
            } else {
                Color.parseColor(getString(R.color.gray))
            }
        )

        return is8char && hasNum && hasUpper
    }


    private fun displayToast(s: String) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
    }
}