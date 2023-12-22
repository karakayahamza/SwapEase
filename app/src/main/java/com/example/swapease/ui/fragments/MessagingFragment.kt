package com.example.swapease.ui.fragments

import MessageAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.data.models.Message
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.FragmentMessagingBinding
import com.example.swapease.ui.viewmodels.MessagingViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "product"
private const val ARG_PARAM2 = "chatBoxId"
class MessagingFragment : Fragment() {
    private var _binding: FragmentMessagingBinding? = null
    private val binding get() = _binding!!
    private var param1: Product? = null
    private var param2 : String? = null
    private lateinit var viewModel: MessagingViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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


        viewModel = ViewModelProvider(this)[MessagingViewModel::class.java]
        viewModel.init(param1, param2) // Make sure to pass the correct arguments here

        // Set up RecyclerView and Adapter
        val messageAdapter = MessageAdapter()
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
        }

        // Observe LiveData for chat messages
        viewModel.messageList.observe(viewLifecycleOwner) { messageList ->
            for (m in messageList){
                 messageAdapter.addMessage(m.first,m.second)
            }
            binding.chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
        }

        // Observe LiveData for other user's name
        viewModel.otherUserName.observe(viewLifecycleOwner) { otherUserName ->
            // Update UI with other user's name if needed
            println(otherUserName)
        }

        // Send message button click listener
        binding.sendMessageButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(messageText)
                binding.messageEditText.text.clear()
            }
        }

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