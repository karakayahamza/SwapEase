package com.example.swapease.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.FragmentProductDetailsBinding
import com.example.swapease.ui.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth

private const val ARG_PARAM1 = "product"
class ProductDetailsFragment : Fragment() {
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!
    private var param1: Product? = null
    private lateinit var userViewModel: UserViewModel
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        val currentUserUid = auth.currentUser?.uid
        val publisherUid = param1?.publisherUid

        if (publisherUid == currentUserUid) {
            binding.goToChat.visibility = View.GONE
        } else {
            binding.goToChat.visibility = View.VISIBLE
        }

        binding.goToChat.setOnClickListener {
            val action = ProductDetailsFragmentDirections.actionProductDetailsFragmentToMessagingFragment(param1!!)
            view.findNavController().navigate(action)

        }

        binding.textViewProductName.text = param1!!.productName
        binding.textViewDescription.text = param1!!.description
        binding.category.text = "Category : ${param1!!.category}"
        binding.publisherName.text = param1!!.publisherName

        Log.d("TASD",param1!!.imageUrl.toString())

        Glide.with(requireContext())
            .load(param1!!.imageUrl)
            .into(binding.imageViewProduct)



        userViewModel.userImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (imageUrl != null) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(binding.profileImageView)

                Log.d("TTTTTT",imageUrl.toString())
            } else {
                println("ImageUrl bulunamadı.")
            }
        }
        userViewModel.getUserImageUrl(param1!!.publisherUid.toString())


    }
}