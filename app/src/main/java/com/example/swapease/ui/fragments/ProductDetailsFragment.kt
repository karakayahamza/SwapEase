package com.example.swapease.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.swapease.R
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.FragmentChatBinding
import com.example.swapease.databinding.FragmentProductDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "product"
class ProductDetailsFragment : Fragment() {
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!
    private var param1: Product? = null
    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getParcelable(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserUid = auth.currentUser?.uid
        val publisherUid = param1?.publisherUid?.toString()

        if (publisherUid == currentUserUid) {
            binding.goToChat.visibility = View.GONE
        } else {
            binding.goToChat.visibility = View.VISIBLE
        }

        binding.goToChat.setOnClickListener {

            val action = ProductDetailsFragmentDirections.actionProductDetailsFragmentToMessagingFragment(param1!!)
            view.findNavController().navigate(action)
            //view.findNavController().popBackStack()
        }
        binding.textViewProductName.text = param1!!.productName
        binding.textViewDescription.text = param1!!.description
        Glide.with(requireContext())
            .load(param1!!.imageUrl)
            .into(binding.imageViewProduct)
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: Product) =
            ProductDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                }
            }
    }
}