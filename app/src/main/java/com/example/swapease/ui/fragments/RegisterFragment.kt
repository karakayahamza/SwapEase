package com.example.swapease.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.swapease.ui.activities.DashboardActivity
import com.example.swapease.R
import com.example.swapease.databinding.FragmentRegisterBinding
import com.example.swapease.ui.viewmodels.RegisterViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {
    private val RC_SIGN_IN = 123
    private val viewModel by viewModels<RegisterViewModel>()

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


        binding.signUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            registerWithEmailAndPassword(email, password)
        }
        binding.btSignIn.setOnClickListener {
            signInWithGoogle()
        }

        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { message ->
                displayToast(message)
                startDashboardActivity()
            }.onFailure { exception ->
                displayToast("Registration failed: ${exception.message}")
            }
        }

        viewModel.googleSignInResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { message ->
                displayToast(message)
                startDashboardActivity()
            }.onFailure { exception ->
                displayToast("Google Sign-In failed: ${exception.message}")
            }
        }

    }

    private fun registerWithEmailAndPassword(email: String, password: String) {
        val username = binding.etusername.text.toString()
        viewModel.registerWithEmailAndPassword(email, password, username)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
        with(binding) {
            etusername.addTextChangedListener(textWatcher)
            etEmail.addTextChangedListener(textWatcher)
            etPassword.addTextChangedListener(textWatcher)
            etConfirmPassword.addTextChangedListener(textWatcher)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val username = validateUsername(binding.etusername.text.toString())
            val email = validateEmail(binding.etEmail.text.toString())
            val password = isPasswordValid(binding.etPassword.text.toString())
            val confirmPassword = isConfirmPassword(binding.etPassword.text.toString(), binding.etConfirmPassword.text.toString())

            binding.signUp.isEnabled = email && password && username && confirmPassword
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (signInAccountTask.isSuccessful) {
                val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)
                if (googleSignInAccount != null) {
                    val idToken = googleSignInAccount.idToken
                    viewModel.signInWithGoogle(idToken,data)
                }
            } else {
                displayToast("Invalid e-mail or password")
            }
        }
    }

    private fun startDashboardActivity() {
        startActivity(
            Intent(requireContext(), DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        requireActivity().finish()
    }

    private fun displayToast(s: String) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
    }

    private fun isPasswordValid(password: String): Boolean {
        val is8char = password.length >= 8
        binding.card1.setCardBackgroundColor(getCardColor(is8char))

        val hasNum = password.contains(Regex(".*[0-9].*"))
        binding.card2.setCardBackgroundColor(getCardColor(hasNum))

        val hasUpper = password.contains(Regex(".*[A-Z].*"))
        binding.card3.setCardBackgroundColor(getCardColor(hasUpper))

        return is8char && hasNum && hasUpper
    }

    private fun getCardColor(isValid: Boolean): Int {
        return getColor(if (isValid) R.color.green else R.color.default_border_color)
    }

    private fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")
        return updateBoxStrokeColor(binding.emailTextInputLayout, email.matches(emailRegex))
    }

    private fun validateUsername(username: String): Boolean {
        return updateBoxStrokeColor(binding.usernameTextInputLayout, username.length >= 5)
    }

    private fun isConfirmPassword(password: String, confirmPassword: String): Boolean {
        val isConfirm = password == confirmPassword && password.isNotEmpty()
        binding.card4.setCardBackgroundColor(getCardColor(isConfirm))
        return isConfirm
    }

    private fun updateBoxStrokeColor(textInputLayout: TextInputLayout, isValid: Boolean): Boolean {
        textInputLayout.boxStrokeColor = getColor(if (isValid) R.color.green else R.color.red)
        return isValid
    }

    private fun getColor(@ColorRes colorResId: Int): Int {
        return ContextCompat.getColor(requireContext(), colorResId)
    }

}