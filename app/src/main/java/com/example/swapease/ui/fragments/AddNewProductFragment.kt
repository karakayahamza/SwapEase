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
import com.example.swapease.databinding.FragmentAddNewProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddNewProductFragment : Fragment() {
    private var _binding: FragmentAddNewProductBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

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
                uploadImageToFirebaseStorage(selectedImageUri!!)
            } else {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebaseStorage(uri: Uri) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            // Upload the image to Firebase Storage
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef =
                storageRef.child("images/${currentUserUid}_${System.currentTimeMillis()}")
            val uploadTask = imageRef.putFile(uri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                // If the image upload is successful, get the download URL of the image
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val imageUrl = task.result.toString()
                    // After the image is uploaded, add other product details to the database
                    addItemToDatabase(
                        binding.editTextProductName.text.toString(),
                        binding.editTextDescription.text.toString(),
                        imageUrl
                    )
                } else {
                    // If image upload fails, inform the user
                    Toast.makeText(
                        requireContext(),
                        "Error uploading image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error uploading image", e)
                Toast.makeText(
                    requireContext(),
                    "Error uploading image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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

    private fun addItemToDatabase(productName: String, description: String, imageUrl: String) {
        // Get the user's UID
        val currentUserUid = auth.currentUser?.uid

        // If the user's UID is not null, add the item to the database
        if (currentUserUid != null) {
            val product = hashMapOf(
                "sellerUid" to currentUserUid,
                "productName" to productName,
                "description" to description,
                "imageUrl" to imageUrl
            )

            db.collection("products")
                .add(product)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Item added with ID: ${documentReference.id}")
                    // Actions to be performed in case of successful addition
                    Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT)
                        .show()

                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding item", e)
                    // Actions to be performed in case of failure
                    Toast.makeText(requireContext(), "Error adding item", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If the user is not authenticated, show an error message
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "AddItemActivity"
    }
}
