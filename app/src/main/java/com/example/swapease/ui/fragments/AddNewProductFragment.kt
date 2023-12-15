package com.example.swapease.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.swapease.databinding.FragmentAddNewProductBinding
import com.example.swapease.ui.viewmodels.AddNewProductViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddNewProductFragment : Fragment() {
    private var _binding: FragmentAddNewProductBinding? = null
    private val binding get() = _binding!!
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private val viewModel: AddNewProductViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageViewProduct.setOnClickListener {
            // Start the image picker intent
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.buttonAddProduct.setOnClickListener {
            // Check if an image is selected
            if (selectedImageUri != null) {
                // Upload the selected image to Firebase Storage
                onImageSelected(selectedImageUri!!)
            } else {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Function to handle image selection
    private fun onImageSelected(uri: Uri) {
        // Upload image to Firebase Storage
        viewModel.uploadImageToFirebaseStorage(uri,
            onSuccess = { imageUrl ->
                // Add item to Firestore
                viewModel.addItemToDatabase(
                    binding.editTextProductName.text.toString(),
                    binding.editTextDescription.text.toString(),
                    imageUrl,
                    onSuccess = {
                        // Actions to be performed in case of successful addition
                        Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        // Actions to be performed in case of failure
                        Toast.makeText(requireContext(), "Error adding item", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onFailure = {
                // Handle image upload failure
                Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get the selected image URI
            selectedImageUri = data.data
            // Set the selected image URI to the ImageView
            binding.imageViewProduct.setImageURI(selectedImageUri)
        }
    }

    companion object {
        const val TAG = "AddItemActivity"
    }
}
