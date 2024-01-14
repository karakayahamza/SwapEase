package com.example.swapease.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.ui.adapters.ChatListAdapter
import com.example.swapease.data.models.ChatBox
import com.example.swapease.databinding.FragmentChatBinding
import com.example.swapease.ui.viewmodels.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatListAdapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using view binding
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        // Initialize RecyclerView adapter
        chatListAdapter = ChatListAdapter(ChatViewModel(), object : ChatListAdapter.OnItemClickListener {
            override fun onItemClick(chatBox: ChatBox) {
                val action = ChatFragmentDirections.actionChatFragmentToMessagingFragment(chatBoxId = chatBox.chatBoxId)
                findNavController().navigate(action)
            }
        })


        binding.chats.layoutManager = LinearLayoutManager(requireContext())
        binding.chats.adapter = chatListAdapter

        // ViewModel'i initialize et
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // Observe LiveData ve RecyclerView'u güncelle
        chatViewModel.getChatBoxesLiveData().observe(viewLifecycleOwner) { chatBoxes ->
            chatListAdapter.setChatBoxes(chatBoxes)
        }

        return binding.root
    }

    companion object {
        private const val TAG = "ChatFragment"
    }
}

