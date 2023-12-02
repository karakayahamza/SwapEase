package com.example.swapease.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.swapease.data.models.Product
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.example.swapease.databinding.ProducRecyclerviewItemBinding

class ProductListAdapter : ListAdapter<Product, ProductListAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProducRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = getItem(position)
        holder.bind(currentProduct)
    }

    class ProductViewHolder(private val binding: ProducRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                // Set the product details to the UI elements
                // For example:
                textViewProductName.text = product.productName
                textViewDescription.text = product.description
                Glide.with(imageViewProduct.context)
                    .load(product.imageUrl)
                    .into(imageViewProduct)


                // Load the image using an image loading library like Picasso, Glide, etc.
                // For example with Picasso:
                // Picasso.get().load(product.imageUrl).into(imageViewProduct)
            }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
