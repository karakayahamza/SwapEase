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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "product"
class ProductDetailsFragment : Fragment() {
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!
    // TODO: Rename and change types of parameters
    private var param1: Product? = null

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


        binding.goToChat.setOnClickListener {
/*
            val action = ProductDetailsFragmentDirections.actionProductDetailsFragmentToChatFragment(
                param1!!
            )
            view.findNavController().navigate(action)
*/

            val action2 = ProductDetailsFragmentDirections.actionProductDetailsFragmentToMessagingFragment()
            view.findNavController().navigate(action2)

        }
        binding.textViewProductName.text = param1!!.productName
        binding.textViewDescription.text = param1!!.description
        Glide.with(requireContext())
            .load(param1!!.imageUrl)
            .into(binding.imageViewProduct)





    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProductDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Product, param2: String) =
            ProductDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                }
            }
    }
}