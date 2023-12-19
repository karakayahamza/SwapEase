package com.example.swapease

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.data.models.Message
import com.example.swapease.data.models.Product
import com.example.swapease.data.models.User
import com.example.swapease.databinding.ActivityProfileBinding
import com.example.swapease.databinding.ActivityTestBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private val db = FirebaseFirestore.getInstance()
    private val products = mutableListOf<Product>()
    private lateinit var database: FirebaseDatabase
    private lateinit var currentUserId: String


    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        val otherUserId = "VVdbaIJi0Kd1Yf53HkZtMRv6RWT2"

        // Kullanıcılar arasında paylaşılan bir sohbet odası oluştur
        chatId = if (userId!! < otherUserId) "$userId$otherUserId" else "$otherUserId$userId"

        // Mesajları görüntülemek ve göndermek için bir adapter kullanılır
        chatAdapter = ChatAdapter(userId)
        val layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.layoutManager = layoutManager
        binding.chatRecyclerView.adapter = chatAdapter

        // Firestore veritabanındaki belirli bir sohbet odasını dinle
        firestore.collection("users")
            .document(userId)
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Hata durumunda işlemleri burada ele alabilirsiniz.
                    println(e)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            // Yeni bir mesaj eklenirse
                            val message = dc.document.toObject(Message::class.java)
                            chatAdapter.addMessage(message)
                            binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                            println("Message: ${message.text}")
                        }
                        // Diğer durumlar da ele alınabilir (modified, removed, moved)
                        DocumentChange.Type.MODIFIED -> TODO()
                        DocumentChange.Type.REMOVED -> TODO()
                    }
                }
            }

        // Diğer kullanıcının belirli bir sohbet odasındaki mesajları dinle
        firestore.collection("users")
            .document(otherUserId)
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .addSnapshotListener { receiverSnapshots, receiverException ->
                if (receiverException != null) {
                    // Hata durumunda işlemleri burada ele alabilirsiniz.
                    return@addSnapshotListener
                }

                for (receiverDc in receiverSnapshots!!.documentChanges) {
                    when (receiverDc.type) {
                        DocumentChange.Type.ADDED -> {
                            // Yeni bir mesaj eklenirse (Alıcı)
                            val message = receiverDc.document.toObject(Message::class.java)
                            chatAdapter.addMessage(message)
                            binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                        }
                        // Diğer durumlar da ele alınabilir (modified, removed, moved)
                        DocumentChange.Type.MODIFIED -> TODO()
                        DocumentChange.Type.REMOVED -> TODO()
                    }
                }
            }


        // Mesaj gönderme butonuna tıklanınca çağrılacak fonksiyon
        binding.sendMessageButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message(userId!!, otherUserId, messageText, System.currentTimeMillis())

                // Firestore veritabanına yeni mesajı gönderen belgesine ekle
                firestore.collection("users")
                    .document(userId)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(message)

                // Firestore veritabanına yeni mesajı alıcı belgesine ekle
                firestore.collection("users")
                    .document(otherUserId)
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(message)

                binding.messageEditText.text.clear()
            }
        }
    }
}