package com.example.swapease.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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


class UserDashBoardFragment : Fragment() {
    private var _binding: FragmentUserDashBoardBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductListAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val products: MutableList<Product> = mutableListOf()

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
            val profileImageUrl: String? = auth.currentUser?.photoUrl?.toString()

            setupRecyclerView()

        if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(profileImageUrl)
                    .into(binding.profileImageView)
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

                    val product = Product(productId, publisherUid,publisherName, productName, description, imageUrl)
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

    companion object {
        const val TAG = "AddItemActivity"
    }

}