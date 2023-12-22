package com.example.swapease.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.ui.adapters.ChatListAdapter
import com.example.swapease.data.models.ChatBox
import com.example.swapease.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatListAdapter: ChatListAdapter
    private val db = FirebaseFirestore.getInstance()
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using view binding
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        // Initialize Firestore and FirebaseAuth instances
        firestore = db
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (currentUserId != null) {
            chatListAdapter = ChatListAdapter(object : ChatListAdapter.OnItemClickListener {
                override fun onItemClick(chatBox: ChatBox) {
                    Log.d("ChatBoxId", chatBox.chatBoxId)

                    val action = ChatFragmentDirections.actionChatFragmentToMessagingFragment(chatBoxId = chatBox.chatBoxId)
                    findNavController().navigate(action)
                }
            })

            binding.chats.layoutManager = LinearLayoutManager(requireContext())
            binding.chats.adapter = chatListAdapter

            // Call getUserDataAndChats only if currentUserId is not null
            getUserDataAndChats(currentUserId!!)
        } else {
            Log.e(TAG, "Current user ID is null.")
        }
    }


    /**
     * Fetches user data and chats.
     * @param userId User's identifier
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun getUserDataAndChats(userId: String) {
        // Creating Firebase Firestore connection
        val userChatsCollection = db.collection("users").document(userId).collection("chats")

        // Fetching documents of user's chats
        userChatsCollection.get()
            .addOnSuccessListener { documents ->
                // Actions to be taken when the query is successful

                Log.d(TAG, "Success! Chat Documents: ${documents.size()}")

                // Process each chat document
                for (document in documents) {
                    // Get the ID of the chat document
                    val chatId = document.id
                    Log.d(TAG, "Chat ID: $chatId")

                    // Get the content of the chat document
                    //val chatData = document.toObject(ChatModel::class.java)
                    Log.d(TAG, "Chat Data: ${document.id}")

                    // Add a new chat box to the chat list
                    chatListAdapter.addChatBox(ChatBox(chatId))
                }

                // Update the chat list
                chatListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Actions to be taken when the query fails
                Log.e(TAG, "Error getting chat documents: ", exception)
            }
    }
    companion object {
        private const val TAG = "ChatFragment"
    }
}