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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.swapease.ui.activities.DashboardActivity
import com.example.swapease.R
import com.example.swapease.data.models.User
import com.example.swapease.databinding.FragmentRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private val binding by lazy {
        FragmentRegisterBinding.inflate(layoutInflater)
    }
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signUp.isClickable = false

        binding.goToRegister.setOnClickListener {
            val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            view.findNavController().navigate(action)
        }

        setupTextWatchers()
        initializeAuthentication()

        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        if (firebaseUser != null) {
            startDashboardActivity()
        }

        binding.signUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            registerWithEmailAndPassword(email, password)
        }
    }

    private fun initializeAuthentication() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    private fun setupTextWatchers() {
        binding.etusername.addTextChangedListener(textWatcher)
        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)
        binding.etConfirmPassword.addTextChangedListener(textWatcher)
    }

    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val username = binding.etusername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            validateUsername(username)
            validateEmail(email)
            isPasswordValid(password)
            isConfirmPassword(password, confirmPassword)

            binding.signUp.isEnabled = validateEmail(email) && isPasswordValid(password) && validateUsername(username) && isConfirmPassword(password, confirmPassword)
        }
    }

    private fun registerWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Kullanıcı başarıyla kaydedildi, Firestore'a bilgileri ekleyelim
                    val userId = firebaseAuth.currentUser?.uid ?: ""
                    val username = binding.etusername.text.toString()
                    val newUser = User(uid = userId, username = username, email = email,null)

                    // Firestore'a kullanıcı bilgilerini ekle
                    firestore.collection("users")
                        .document(userId)
                        .set(newUser)
                        .addOnSuccessListener {
                            displayToast("Registration successful")
                            sendVerificationEmail()
                        }
                        .addOnFailureListener {
                            displayToast("Failed to add user to Firestore: ${it.message}")
                        }
                } else {
                    displayToast("Registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun startDashboardActivity() {
        startActivity(
            Intent(requireContext(), DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }

    private fun sendVerificationEmail() {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    displayToast("Verification email sent. Please check your email.")
                    startDashboardActivity()
                    requireActivity().finish()
                } else {
                    displayToast("Failed to send verification email: ${task.exception?.message}")
                }
            }
    }

    private fun displayToast(s: String) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ResourceType")
    private fun isPasswordValid(password: String): Boolean {
        val is8char: Boolean = password.length >= 8
        binding.card1.setCardBackgroundColor(getCardColor(is8char))

        val hasNum: Boolean = password.contains(Regex(".*[0-9].*"))
        binding.card2.setCardBackgroundColor(getCardColor(hasNum))

        val hasUpper: Boolean = password.contains(Regex(".*[A-Z].*"))
        binding.card3.setCardBackgroundColor(getCardColor(hasUpper))

        return is8char && hasNum && hasUpper
    }

    @SuppressLint("ResourceType")
    private fun getCardColor(isValid: Boolean): Int {
        return if (isValid) {
            Color.parseColor(getString(R.color.green))
        } else {
            Color.parseColor(getString(R.color.default_border_color))
        }
    }

    @SuppressLint("ResourceType")
    private fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")
        val isValid = email.matches(emailRegex)
        binding.emailTextInputLayout.boxStrokeColor = getBoxStrokeColor(isValid)
        return isValid
    }

    @SuppressLint("ResourceType")
    private fun validateUsername(username: String): Boolean {
        val isValid: Boolean = username.length >= 5
        binding.usernameTextInputLayout.boxStrokeColor = getBoxStrokeColor(isValid)
        return isValid
    }

    @SuppressLint("ResourceType")
    private fun isConfirmPassword(password: String, confirmPassword: String): Boolean {
        val isConfirm: Boolean = password == confirmPassword && password.isNotEmpty()
        binding.card4.setCardBackgroundColor(getCardColor(isConfirm))
        return isConfirm
    }

    @SuppressLint("ResourceType")
    private fun getBoxStrokeColor(isValid: Boolean): Int {
        return if (isValid) {
            ContextCompat.getColor(requireContext(), R.color.green)
        } else {
            ContextCompat.getColor(requireContext(), R.color.red)
        }
    }
}
