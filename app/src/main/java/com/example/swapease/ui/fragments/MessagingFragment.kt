package com.example.swapease.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swapease.R
import com.example.swapease.data.models.Message
import com.example.swapease.databinding.FragmentMessagingBinding
import com.example.swapease.databinding.FragmentProductDetailsBinding
import com.example.swapease.ui.adapters.MessageAdapter

private const val ARG_PARAM1 = "param1"


class MessagingFragment : Fragment() {
    private var _binding: FragmentMessagingBinding? = null
    private val binding get() = _binding!!
    private var param1: String? = null
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentMessagingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.recyclerViewMessages
        messageAdapter = MessageAdapter()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messageAdapter
            // İhtiyaca göre RecyclerView'a diğer özellikler ekleyebilirsiniz.
        }

        // Örnek veri ekleme
        val messages = listOf(
            Message("senderId", "receiverId", "Example Message 1", System.currentTimeMillis() / 1000),
            Message("receiverId", "senderId", "Example Message 2", System.currentTimeMillis() / 1000 + 100),
            // Daha fazla örnek veri...
        )

        messageAdapter.submitList(messages)

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MessagingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}