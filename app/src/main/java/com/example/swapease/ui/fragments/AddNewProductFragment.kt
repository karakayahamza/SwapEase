package com.example.swapease.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.swapease.R
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.FragmentAddNewProductBinding
import com.example.swapease.ui.viewmodels.AddNewProductViewModel

class AddNewProductFragment : Fragment() {
    private var _binding: FragmentAddNewProductBinding? = null
    private val binding get() = _binding!!
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private val viewModel: AddNewProductViewModel by viewModels()
    private lateinit var categories: Array<String>
    lateinit var selectedCategory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categories = resources.getStringArray(R.array.category_array)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewProductBinding.inflate(inflater, container, false)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategories.adapter = adapter

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
                showToastMessage("Please select an image")
            }
        }

        binding.spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Get the selected category and notify the user
                selectedCategory = categories[position]
                //Toast.makeText(requireContext(), "Selected Category: $selectedCategory", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                selectedCategory = "Other"
            }
        }

    }

    // Function to handle image selection
    private fun onImageSelected(uri: Uri) {
        // Upload image to Firebase Storage
        viewModel.uploadImageToFirebaseStorage(uri,
            onSuccess = { imageUrl ->
                // Create a Product object with the necessary information
                val product = Product(
                    productId = null, // Bu değeri null olarak bıraktım, çünkü Firestore'da belirli bir belgenin ID'si genellikle belge eklenirken otomatik olarak atanır
                    publisherUid = null,
                    publisherName = null, // Bu değeri değiştirmeniz gerekiyor
                    productName = binding.productNameText.text.toString(),
                    description = binding.productDescription.text.toString(),
                    category = selectedCategory,
                    imageUrl = imageUrl
                )

                // Add item to Firestore
                viewModel.addItemToDatabase(product,
                    onSuccess = {
                        // Actions to be performed in case of successful addition
                        showToastMessage("Item added successfully")
                        binding.productNameText.text?.clear()
                        binding.productDescription.text?.clear()
                        findNavController().navigate(R.id.action_addNewProductFragment_to_userMainScreenFragment)
                    },
                    onFailure = {
                        // Actions to be performed in case of failure
                        showToastMessage("Error adding item")
                    }
                )
            },
            onFailure = {
                // Handle image upload failure
                showToastMessage("Error uploading image")
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

    private fun showToastMessage(message:String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
