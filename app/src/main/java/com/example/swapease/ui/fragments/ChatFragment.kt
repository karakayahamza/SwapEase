package com.example.swapease.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.ChatListAdapter
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        chatListAdapter = ChatListAdapter(object : ChatListAdapter.OnItemClickListener {
            override fun onItemClick(chatBox: ChatBox) {
                Log.d("ChatBoId",chatBox.chatBoxId)
                val action = ChatFragmentDirections.actionChatFragmentToMessagingFragment(chatBoxId = chatBox.chatBoxId)
                view.findNavController().navigate(action)
            }
        })

        binding.chats.layoutManager = LinearLayoutManager(requireContext())
        binding.chats.adapter = chatListAdapter

        getUserDataAndChats(auth.currentUser?.uid.toString())
    }

    private fun getUserDataAndChats(userId:String){

        val userChatsCollection = db.collection("users").document(userId).collection("chats")

        userChatsCollection.get()
            .addOnSuccessListener { documents ->
                Log.d("Firestore", "Success! Chat Documents: ${documents.size()}")
                for (document in documents) {
                    // Sohbet belgesinin ID'sini al
                    val chatId = document.id
                    Log.d("Firestore", "Chat ID: $chatId")

                    // Sohbet belgesinin içeriğini al
                    //val chatData = document.toObject(ChatModel::class.java)
                    Log.d("Firestore", "Chat Data: ${document.id}")


                    val result = splitChatIdTwoParts(document.id)

                    result?.let { (first, second) ->
                        println("Current User ID: $first")
                        println("Other User ID: $second")
                    } ?: run {
                        println("Veri iki parçaya ayrılamadı veya hata oluştu.")
                    }

                    chatListAdapter.addChatBox(ChatBox(chatId))
                }
                chatListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting chat documents: ", exception)
                // Hata durumunda yapılacak işlemler
            }
    }

    private fun splitChatIdTwoParts(data: String?): Pair<String, String>? {
        var currentUserId = ""
        var otherUserId = ""

        val parts = data?.split("-")

        if (parts != null) {
            if (parts.size == 2) {
                val firstPart = parts[0]
                val secondPart = parts[1]

                println("First Part: $firstPart")
                println("Second Part: $secondPart")

                if (firstPart == auth.currentUser!!.uid) {
                    currentUserId = firstPart
                    otherUserId = secondPart
                } else {
                    currentUserId = secondPart
                    otherUserId = firstPart
                }

                // Burada Pair'ı oluşturup döndür
                return Pair(currentUserId, otherUserId)
            } else {
                println("Veri iki parçaya ayrılamadı.")
            }
        }

        // Veri iki parçaya ayrılamazsa veya hata olursa null döndür
        return null
    }
}