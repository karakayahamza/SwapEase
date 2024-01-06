package com.example.swapease.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.FragmentUserMainScreenBinding
import com.example.swapease.ui.adapters.ProductListAdapter
import com.example.swapease.ui.viewmodels.UserMainScreenViewModel
class UserMainScreenFragment : Fragment() {

    private var _binding: FragmentUserMainScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductListAdapter
    private val viewModel: UserMainScreenViewModel by viewModels()

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
        // Observe products data
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }

        // Observe error events
        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { exception ->
                // Show a toast message with the error details
                Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
        // Fetch products from ViewModel
        viewModel.getAllProducts()
    }
    private fun setupRecyclerView() {
        adapter = ProductListAdapter(object : ProductListAdapter.OnItemClickListener {
            override fun onItemClick(product: Product) {
                val action = UserMainScreenFragmentDirections.actionUserMainScreenFragmentToProductDetailsFragment(product)
                view?.findNavController()?.navigate(action)
                //view?.findNavController()?.popBackStack()
            }

            override fun onItemLongClick(product: Product) {

            }
        })

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}