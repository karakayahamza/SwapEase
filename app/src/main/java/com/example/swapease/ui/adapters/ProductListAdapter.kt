package com.example.swapease.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.ProductRecyclerviewItemBinding

class ProductListAdapter(private val onItemClickListener: OnItemClickListener?) :
    ListAdapter<Product, ProductListAdapter.ProductViewHolder>(ProductDiffCallback()) {

    interface OnItemClickListener {
        fun onItemClick(product: Product)
        fun onItemLongClick(product: Product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = getItem(position)
        holder.bind(currentProduct)

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(currentProduct)
        }

        holder.itemView.setOnLongClickListener {
            onItemClickListener?.onItemLongClick(currentProduct)
            true // Return true to consume the long click event
        }
    }

    class ProductViewHolder(private val binding: ProductRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            binding.apply {
                textViewProductName.text = product.productName
                binding.category.text = product.category
                binding.updatedDate.text = "Uploaded on ${product.addedDate}"

                Glide.with(imageViewProduct.context)
                    .load(product.imageUrl)
                    .into(imageViewProduct)
            }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}