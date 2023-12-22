package com.example.swapease.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.FragmentUserDashBoardBinding
import com.example.swapease.ui.activities.LoginActivity
import com.example.swapease.ui.adapters.ProductListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class UserDashBoardFragment : Fragment() {
    private var _binding: FragmentUserDashBoardBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductListAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val products: MutableList<Product> = mutableListOf()
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserDashBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            setupRecyclerView()
            loadUserProfileImage()

        binding.profileImageView.setOnClickListener {
            startImageSelection()
        }

            binding.logoutButton.setOnClickListener {
                FirebaseAuth.getInstance().signOut()

                val loginIntent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(loginIntent)

                requireActivity().finish()
            }
    }
    private fun getAllProducts() {
        // Kullanıcının UID'sini al
        val currentUserUid = auth.currentUser?.uid
        binding.userName.text = auth.currentUser?.displayName

        // Kullanıcının kendi ürünlerini görüntüleme sorgusu
        db.collection("products")
            .whereEqualTo("publisherUid", currentUserUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val productList = mutableListOf<Product>()

                for (document in querySnapshot.documents) {
                    val productId = document.id
                    val publisherName = auth.currentUser?.displayName
                    val publisherUid = document.getString("publisherUid") ?: ""
                    val productName = document.getString("productName") ?: ""
                    val description = document.getString("description") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""

                    val product = Product(productId, publisherUid,publisherName, productName, description,null, imageUrl)
                    productList.add(product)
                }

                // RecyclerView için adapter'a veriyi gönder
                adapter.submitList(productList)

                // Kullanıcı UID'sini göster
                binding.uid.text = currentUserUid
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents", e)
                // Hata durumunda işlemleri gerçekleştir
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteProduct(productId: String) {
        db.collection("products")
            .document(productId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Product deleted successfully")
                // Ürün başarıyla silindiyse yapılacak işlemler
                getAllProducts()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting product", e)
                // Ürün silme başarısız olduysa yapılacak işlemler
            }
    }


    private fun setupRecyclerView() {
        adapter = ProductListAdapter(object : ProductListAdapter.OnItemClickListener {
            override fun onItemClick(product: Product) {

            }
            override fun onItemLongClick(product: Product) {
                showDeleteConfirmationDialog(product)
            }
        })

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = adapter

        getAllProducts()
    }

    private fun loadUserProfileImage(profileImageUrl: String) {
        Glide.with(requireContext())
            .load(profileImageUrl)
            .into(binding.profileImageView)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteConfirmationDialog(product: Product) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Delete") { _, _ ->
                products.remove(product)
                adapter.notifyDataSetChanged()
                deleteProduct(product.productId.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun loadUserProfileImage() {
        // Get the current user and user UID
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserUid = currentUser?.uid

        // Check if the current user has a photo URL
        currentUser?.photoUrl?.let { photoUrl ->
            // If a photo URL exists, load the user profile image
            loadUserProfileImage(photoUrl.toString())
        } ?: run {
            // If the user doesn't have a photo URL, check the Firestore database for a custom profile image
            val userDocRef = db.collection("users").document(currentUserUid ?: "")

            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Retrieve the custom profile image URL from Firestore
                        val profileImageUrl = documentSnapshot.getString("userProfileImage")
                        profileImageUrl?.let {
                            // If a custom profile image URL exists, load the user profile image
                            loadUserProfileImage(it)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle errors when fetching the profile image URL from Firestore
                    Log.e(TAG, "Error fetching profile image", e)
                }
        }
    }

    private fun updateUserProfileImage(imageUrl: String) {
        val userDocRef = db.collection("users").document(auth.currentUser?.uid ?: "")
        userDocRef.update("userProfileImage", imageUrl)
            .addOnSuccessListener {
                Log.d(TAG, "Profil resmi güncelleme başarılı")
                loadUserProfileImage(imageUrl)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Profil resmi güncelleme hatası", e)
            }
    }

    private fun startImageSelection() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data

            // Seçilen resmi Firestore Storage'a yükle

            selectedImageUri?.let { uploadImageToFirebaseStorage(it) }
        }
    }
    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef =
            storageRef.child("profile_images/${FirebaseAuth.getInstance().currentUser?.uid}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Resim başarıyla yüklendiğinde yapılacak işlemleri buraya ekleyebilirsiniz
                Log.d(TAG, "Resim yükleme başarılı: ${taskSnapshot.metadata?.path}")

                // Yüklendikten sonra resmin URL'sini alabilir ve kullanıcı profilini güncelleyebilirsiniz
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    updateUserProfileImage(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                // Yükleme başarısız olduğunda yapılacak işlemleri buraya ekleyebilirsiniz
                Log.e(TAG, "Resim yükleme hatası", e)
            }
    }
    companion object {
        const val TAG = "AddItemActivity"
    }

}