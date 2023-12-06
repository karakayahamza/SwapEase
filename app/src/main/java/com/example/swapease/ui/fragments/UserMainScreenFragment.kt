package com.example.swapease.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.FragmentUserMainScreenBinding
import com.example.swapease.ui.adapters.ProductListAdapter
import com.google.firebase.firestore.FirebaseFirestore

class UserMainScreenFragment : Fragment() {

    private var _binding: FragmentUserMainScreenBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProductListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserMainScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        // Tüm ürünleri al ve RecyclerView'e bağla
        getAllProducts()
    }
    private fun setupRecyclerView() {
        adapter = ProductListAdapter()
        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = adapter
    }

    private fun getAllProducts() {
        db.collection("products")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val products = mutableListOf<Product>()
                for (document in querySnapshot) {
                    val product = document.toObject(Product::class.java)
                    products.add(product)
                }
                adapter.submitList(products)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting all products", e)
            }
    }

    companion object {
        const val TAG = "AllProductsFragment"
    }
}