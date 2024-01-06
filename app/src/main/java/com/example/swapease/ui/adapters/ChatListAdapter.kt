package com.example.swapease.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.swapease.R
import com.example.swapease.data.models.ChatBox
import com.example.swapease.databinding.ItemChatBoxBinding
import com.example.swapease.ui.fragments.ChatFragment
import com.example.swapease.ui.viewmodels.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatListAdapter(
    private val viewModel: ChatViewModel,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ChatListAdapter.ChatBoxViewHolder>() {

    private val chatBoxes = mutableListOf<ChatBox>()

    interface OnItemClickListener {
        fun onItemClick(chatBox: ChatBox)
    }

    fun setChatBoxes(chatBoxes: List<ChatBox>) {
        this.chatBoxes.clear()
        this.chatBoxes.addAll(chatBoxes)
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
            val result = viewModel.splitChatIdTwoParts(chatBox.chatBoxId)
            val otherUser = result?.second ?: ""

            viewModel.getUserData(otherUser, { username, userProfileImage ->
                // Başarıyla tamamlandığında kullanıcı adını ve profil resmini güncelle
                binding.userName.text = username
                Glide.with(binding.root.context)
                    .load(userProfileImage)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_placeholder)
                    .centerCrop()
                    .into(binding.publisherImage)

                Log.d(TAG,userProfileImage.toString())
            }, { exception ->
                // Hata durumunda işlem yap
                Log.e(TAG, "Error getting user data", exception)
            })
        }
    }

    companion object {
        private const val TAG = "ChatListAdapter"
    }
}