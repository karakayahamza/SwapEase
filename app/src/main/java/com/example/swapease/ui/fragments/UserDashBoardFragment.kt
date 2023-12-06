package com.example.swapease.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.swapease.databinding.FragmentUserDashBoardBinding
import com.example.swapease.ui.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class UserDashBoardFragment : Fragment() {
    private var _binding: FragmentUserDashBoardBinding? = null
    private val binding get() = _binding!!
    val firebaseAuth: FirebaseAuth = Firebase.auth
    val currentUser: FirebaseUser? = firebaseAuth.currentUser
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
        binding.logoutButton.setOnClickListener {
            val profileImageUrl: String? = currentUser?.photoUrl?.toString()

            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(profileImageUrl)
                    .into(binding.profileImageView)
            }

            binding.logoutButton.setOnClickListener {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)

                requireActivity().finish()
            }
        }

    }
}