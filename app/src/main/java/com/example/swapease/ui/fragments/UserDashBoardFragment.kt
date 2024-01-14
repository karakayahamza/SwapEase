package com.example.swapease.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.swapease.R
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.FragmentUserDashBoardBinding
import com.example.swapease.ui.activities.LoginActivity
import com.example.swapease.ui.adapters.ProductListAdapter
import com.example.swapease.ui.viewmodels.UserDashBoardViewModel
import com.example.swapease.utils.ThemePreferenceUtil
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth


class UserDashBoardFragment : Fragment() {

    private var _binding: FragmentUserDashBoardBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleSignInClient: GoogleApiClient
    private val viewModel: UserDashBoardViewModel by viewModels()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var adapter: ProductListAdapter
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private val themePreferenceUtil by lazy { ThemePreferenceUtil(requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDashBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDarkModeState()
        setupRecyclerView()
        observeViewModel()
        loadUserProfile()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleApiClient.Builder(requireContext())
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        googleSignInClient.connect()

        binding.profileImageView.setOnClickListener {
            startImageSelection()
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveDarkModeState(isChecked)
            setAppTheme(isChecked)
        }

    }


    private fun logout() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account != null) {
            Auth.GoogleSignInApi.revokeAccess(googleSignInClient).setResultCallback { status ->
                if (status.isSuccess) {
                    FirebaseAuth.getInstance().signOut()
                    //Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    //.makeText(requireContext(), "Logout failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            FirebaseAuth.getInstance().signOut()
            //Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val loginIntent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(loginIntent)
        requireActivity().finish()
    }

    private fun setupRecyclerView() {
        adapter = ProductListAdapter(object : ProductListAdapter.OnItemClickListener {
            override fun onItemClick(product: Product) {
                // Handle item click
            }

            override fun onItemLongClick(product: Product) {
                showDeleteConfirmationDialog(product)
            }
        })

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner, Observer { productList ->
            binding.userName.text = auth.currentUser?.displayName
            adapter.submitList(productList)
        })

        viewModel.statusMessage.observe(viewLifecycleOwner, Observer { message ->
            // Handle status messages, e.g., show a Toast
            //Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun deleteProduct(productId: String) {
        viewModel.deleteProduct(productId)
    }

    private fun loadUserProfile() {
        viewModel.userProfileImage.observe(viewLifecycleOwner, Observer { imageUrl ->
            Glide.with(requireContext())
                .load(imageUrl)
                .into(binding.profileImageView)
        })

        viewModel.user.observe(viewLifecycleOwner, Observer {

            Log.d(TAG,"Swapes : ${it.completedSwapes.toString()} Rate: ${it.rating.toString()}")
            binding.competedSwapes.text = it.completedSwapes.toString()
            binding.rating.text = it.rating.toString()


        })

    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProduct(product.productId.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    @SuppressLint("IntentReset")
    private fun startImageSelection() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data

            selectedImageUri?.let { viewModel.uploadImageToFirebaseStorage(it) }
        }
    }

    private fun saveDarkModeState(isDarkModeEnabled: Boolean) {
        themePreferenceUtil.setThemeMode(if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun loadDarkModeState() {
        val isDarkModeEnabled = themePreferenceUtil.getThemeMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.themeSwitch.isChecked = isDarkModeEnabled
        setAppTheme(isDarkModeEnabled)
    }

    private fun setAppTheme(isDarkModeEnabled: Boolean) {
        val themeMode = if (isDarkModeEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        themePreferenceUtil.setThemeMode(themeMode)

        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "UserDashboardFragment"
    }
}