package com.example.swapease.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.swapease.R
import com.example.swapease.data.models.ChatBox
import com.example.swapease.databinding.ItemChatBoxBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatListAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<ChatListAdapter.ChatBoxViewHolder>() {

    private val chatBoxes = mutableListOf<ChatBox>()
    private var auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    interface OnItemClickListener {
        fun onItemClick(chatBox: ChatBox)
    }

    fun addChatBox(chatBox: ChatBox) {
        chatBoxes.add(chatBox)
        notifyItemInserted(chatBoxes.size - 1)
    }

    fun clearChatBoxes() {
        chatBoxes.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatBoxViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChatBoxBinding.inflate(inflater, parent, false)
        return ChatBoxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatBoxViewHolder, position: Int) {
        val chatBox = chatBoxes[position]
        holder.bind(chatBox)
    }

    override fun getItemCount(): Int = chatBoxes.size

    inner class ChatBoxViewHolder(private val binding: ItemChatBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedChatBox = chatBoxes[position]
                    onItemClickListener.onItemClick(clickedChatBox)
                }
            }
        }

        fun bind(chatBox: ChatBox) {
            val result = splitChatIdTwoParts(chatBox.chatBoxId)
            val otherUser = result?.second ?: ""

            // Reference the document containing user information
            val userDocRef = db.collection("users").document(otherUser)

            // Retrieve the user document
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // If the user document exists, retrieve the username
                        documentSnapshot.getString("username")?.apply {
                            Log.d(TAG, "UserId: $otherUser, Username: $this")
                            binding.userName.text = this
                        } ?: run {
                            Log.e(TAG, "Username field is null")
                        }
                    } else {
                        Log.e(TAG, "User document does not exist")
                    }
                }
                .addOnFailureListener { e ->
                    // Handle the failure to retrieve the user document
                    Log.e(TAG, "Error getting user document", e)
                }

            // Retrieve the Firestore document of the user
            val userDocument = db.collection("users").document(otherUser)
            userDocument.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Retrieve the userProfileImage field of the user
                        documentSnapshot.getString("userProfileImage")?.let { userProfileImage ->
                            // Use the userProfileImage value from Firestore
                            Glide.with(binding.root.context)
                                .load(userProfileImage)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.error_placeholder)
                                .centerCrop()
                                .into(binding.publisherImage)
                        } ?: run {
                            // Handle the case where userProfileImage field is not found
                            Log.e(TAG, "userProfileImage not found")
                        }
                    } else {
                        // Handle the case where the user document is not found
                        Log.e(TAG, "User document not found")
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error situation
                    Log.e(TAG, "Firestore Error: $exception")
                }
        }
    }

    /**
     * Splits the chat ID into two parts and determines the user's ID.
     * @param data Chat ID
     * @return Pair<String, String>? Two separated IDs, or null if unable to split.
     */
    private fun splitChatIdTwoParts(data: String?): Pair<String, String>? {
        // Split the chat ID into two parts based on the '-' character
        val parts = data?.split("-")

        // If there are two parts
        if (parts?.size == 2) {
            // Get the first and second parts
            val (firstPart, secondPart) = parts

            // If the first part is equal to the current user's ID
            val (currentUserId, otherUserId) = if (firstPart == auth.currentUser?.uid) {
                Pair(firstPart, secondPart)
            } else {
                Pair(secondPart, firstPart)
            }

            // Return the two-part IDs
            return Pair(currentUserId, otherUserId)
        } else {
            // If unable to split into two parts or an error occurs
            println("Data could not be split into two parts.")
        }
        return null
    }

    companion object {
        private const val TAG = "ChatListAdapter"
    }
}