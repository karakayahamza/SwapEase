package com.example.swapease.ui.fragments

import MessageAdapter
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swapease.data.models.Product
import com.example.swapease.databinding.FragmentMessagingBinding
import com.example.swapease.ui.viewmodels.MessagingViewModel

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