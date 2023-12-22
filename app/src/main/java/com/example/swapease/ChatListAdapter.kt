package com.example.swapease

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

    override fun getItemCount(): Int {
        return chatBoxes.size
    }

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
            /*binding.userName.text = chatBox.publisherName
            Glide.with(binding.root.context)
                .load(chatBox.publisherProfileImage)
                .into(binding.publisherImage)*/

            var otherUser = ""
            val result = splitChatIdTwoParts(chatBox.chatBoxId)
            result?.let { (first, second) ->
                println("Current User ID: $first")
                println("Other User ID: $second")
                otherUser = second
                println("Second : $second")
            } ?: run {
                println("Veri iki parçaya ayrılamadı veya hata oluştu.")
            }

            // Kullanıcı bilgilerini içeren belgeye referans alın
            val userDocRef = db.collection("users").document(otherUser)

// Kullanıcı belgesini al
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Kullanıcı belgesi mevcut ise, username'i al
                        val username = documentSnapshot.getString("username")
                        if (username != null) {
                            // Kullanıcı adı alındı, burada yapmak istediğiniz işlemleri gerçekleştirin
                            Log.d("Firestore", "UserId: $otherUser, Username: $username")
                            binding.userName.text = username
                        } else {
                            Log.e("Firestore", "Kullanıcı adı alanı null")
                        }
                    } else {
                        Log.e("Firestore", "Kullanıcı belgesi mevcut değil")
                    }
                }
                .addOnFailureListener { e ->
                    // İstek başarısız oldu, hata durumunu ele al
                    Log.e("Firestore", "Kullanıcı belgesini alma hatası", e)
                }

            // Kullanıcının Firestore belgesini al
            val userDocument = db.collection("users").document(otherUser)
            userDocument.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Kullanıcının userProfileImage alanını al
                        val userProfileImage = documentSnapshot.getString("userProfileImage")

                        if (!userProfileImage.isNullOrEmpty()) {
                            // Firestore'dan gelen userProfileImage değerini kullanabilirsiniz
                            println("userProfileImage: $userProfileImage")

                            Glide.with(binding.root.context)
                                .load(userProfileImage)
                                .into(binding.publisherImage)
                        } else {
                            // userProfileImage alanı bulunamadıysa
                            println("userProfileImage Bulunamadı")
                        }
                    } else {
                        // Kullanıcı belgesi bulunamadı
                        println("Kullanıcı Belgesi Bulunamadı")
                    }
                }
                .addOnFailureListener { exception ->
                    // Hata durumunda işlemleri burada ele alabilirsiniz
                    println("Firestore Hata: $exception")
                }


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
