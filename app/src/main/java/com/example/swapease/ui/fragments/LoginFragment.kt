package com.example.swapease.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.swapease.R
import com.example.swapease.ui.activities.DashboardActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.swapease.databinding.FragmentLoginBinding
import com.example.swapease.ui.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel by viewModels<LoginViewModel>()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(binding.etEmail)
            }
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(binding.etPassword)
            }
        }

        binding.goToRegister.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            view.findNavController().navigate(action)
        }

        binding.singIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginViewModel.signInWithEmailAndPassword(email, password, {
                    startDashboardActivity()
                    displayToast(it)
                }, {
                    displayToast(it)
                })
            } else {
                displayToast("Email or password cannot be empty")
            }
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        binding.btSignIn.setOnClickListener {
            val intent: Intent = googleSignInClient.signInIntent
            startActivityForResult(intent, 100)
        }

        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            startDashboardActivity()
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
                    loginViewModel.signInWithGoogle(googleSignInAccount, {
                        startDashboardActivity()
                        displayToast(it)
                    }, {
                        displayToast(it)
                    })
                }
            } else {
                displayToast("Invalid e-mail or password")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun startDashboardActivity() {
        startActivity(Intent(requireActivity(), DashboardActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        requireActivity().finish()
    }

    private fun displayToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
