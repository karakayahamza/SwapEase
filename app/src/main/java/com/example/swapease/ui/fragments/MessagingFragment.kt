package com.example.swapease.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.data.models.Message
import com.example.swapease.databinding.FragmentMessagingBinding
import com.example.swapease.ui.adapters.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "chatId"
class MessagingFragment : Fragment() {
    private var _binding: FragmentMessagingBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatId: String
    private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // MessageAdapter'ı oluştur
        messageAdapter = MessageAdapter()

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
        }


        val messageList = mutableListOf<Pair<MessageAdapter.MessageType, Message>>()

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        val otherUserId = "WM03Q5LdJ1QnUZ7US7jFSOC9TL03"

        val result = splitChatIdTwoParts(param1)

        result?.let { (first, second) ->
            println("Current User ID: $first")
            println("Other User ID: $second")
        } ?: run {
            println("Veri iki parçaya ayrılamadı veya hata oluştu.")
        }


        chatId = if (userId!! < otherUserId) "$userId-$otherUserId" else "$otherUserId-$userId"






        val db = FirebaseFirestore.getInstance()

// Kullanıcının sohbet koleksiyonunu oluştur (Eğer yoksa)
        val userChatsCollection = db.collection("users").document(userId).collection("chats").document(chatId)
        userChatsCollection.set(hashMapOf<String, Any>()) // Boş bir belge ekleyebilirsiniz

// Diğer kullanıcının sohbet koleksiyonunu oluştur (Eğer yoksa)
        val otherUserChatsCollection = db.collection("users").document(otherUserId).collection("chats").document(chatId)
        otherUserChatsCollection.set(hashMapOf<String, Any>()) // Boş bir belge ekleyebilirsiniz

// Sohbet koleksiyonunun içine mesaj koleksiyonunu oluştur (Eğer yoksa)
        val chatMessagesCollection = userChatsCollection.collection("messages")
        chatMessagesCollection.add(hashMapOf<String, Any>()) // Boş bir belge ekleyebilirsiniz

// Diğer kullanıcının sohbet koleksiyonunun içine mesaj koleksiyonunu oluştur (Eğer yoksa)
        val otherChatMessagesCollection = otherUserChatsCollection.collection("messages")
        otherChatMessagesCollection.add(hashMapOf<String, Any>()) // Boş bir belge ekleyebilirsiniz



        // Yeni bir mesaj eklemek için fonksiyon
        fun addNewMessage(message: Message) {
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
        }

        firestore.collection("users")
            .document(userId)
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Hata durumunda işlemleri burada ele alabilirsiniz.
                    println(e)
                    return@addSnapshotListener
                }

                val messageList = mutableListOf<Pair<MessageAdapter.MessageType, Message>>() // Tüm mesajları içeren liste

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            // Yeni bir mesaj eklenirse
                            val message = dc.document.toObject(Message::class.java)
                            println("Message1: ${message.text}")

                            if (message.senderUid == userId) {
                                messageList.add(MessageAdapter.MessageType.ME to message)
                            } else {
                                println("Other")
                                messageList.add(MessageAdapter.MessageType.OTHER to message)
                            }
                        }
                        // Diğer durumlar da ele alınabilir (modified, removed, moved)
                        DocumentChange.Type.MODIFIED -> TODO()
                        DocumentChange.Type.REMOVED -> TODO()
                    }
                }

                // Zaman damgasına göre mesajları sırala
                messageList.sortBy { it.second.timestamp }

                // Sıralanmış mesajları adaptera ekle
                for (messageItem in messageList) {
                    messageAdapter.addMessage(messageItem.first, messageItem.second)
                }

                // RecyclerView'yi güncelle
                binding.chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
            }

        binding.sendMessageButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val newMessage = Message(userId, otherUserId, messageText, System.currentTimeMillis())

                // Yeni mesajı eklemek için fonksiyonu çağır
                addNewMessage(newMessage)

                binding.messageEditText.text.clear()
            }
        }


    }



    fun sortAllMessages(){

    }

    fun splitChatIdTwoParts(data: String?): Pair<String, String>? {
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/*
firestore.collection("users")
    .document(userId)
    .collection("chats")
    .document(chatId)
    .collection("messages")
    .orderBy("timestamp")  // Zaman damgasına göre sırala
    .addSnapshotListener { snapshots, e ->
        // Geri kalan kod
    }
 */

/*
       firestore = FirebaseFirestore.getInstance()
       auth = FirebaseAuth.getInstance()

       val userId = auth.currentUser?.uid
       val otherUserId = "FOzD6Y2Ml7dhhpuTueVhhQG9Cyh1"
       chatId = if (userId!! < otherUserId) "$userId-$otherUserId" else "$otherUserId-$userId"

       messageAdapter = MessageAdapter()
       val layoutManager = LinearLayoutManager(requireContext())
       binding.chatRecyclerView.layoutManager = layoutManager
       binding.chatRecyclerView.adapter = messageAdapter

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
                           println("Message1: ${message.text}")

                           /*if(message.senderUid == userId){
                               messageAdapter.addMessage(MessageAdapter.MessageType.ME, message)
                           }
                           else{*/
                               messageAdapter.addMessage(MessageAdapter.MessageType.OTHER, message)
                               binding.chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                          // }

                           binding.chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                       }
                       // Diğer durumlar da ele alınabilir (modified, removed, moved)
                       DocumentChange.Type.MODIFIED -> TODO()
                       DocumentChange.Type.REMOVED -> TODO()
                   }
               }
           }

       /*
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
                           println("Message2: ${message.text}")
                           if(message.senderUid == otherUserId){
                               messageAdapter.addMessage(MessageAdapter.MessageType.OTHER, message)
                           }
                           binding.chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                       }
                       // Diğer durumlar da ele alınabilir (modified, removed, moved)
                       DocumentChange.Type.MODIFIED -> TODO()
                       DocumentChange.Type.REMOVED -> TODO()
                   }
               }
           }*/

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

        */