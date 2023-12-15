package com.example.swapease.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.swapease.databinding.ItemMessageBinding
import com.example.swapease.data.models.Message
import com.example.swapease.databinding.ReceiveItemMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {
    // Define constants for view types
    private val VIEW_TYPE_ME = 1
    private val VIEW_TYPE_OTHER = 2

    override fun getItemViewType(position: Int): Int {
        val currentMessage = getItem(position)
        return if (currentMessage.senderUid == "my_uid") VIEW_TYPE_ME else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ME -> {
                val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MessageViewHolderMe(binding)
            }
            VIEW_TYPE_OTHER -> {
                val binding = ReceiveItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MessageViewHolderOther(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = getItem(position)
        when (holder.itemViewType) {
            VIEW_TYPE_ME -> {
                val viewHolderMe = holder as MessageViewHolderMe
                viewHolderMe.bind(currentMessage)
            }
            VIEW_TYPE_OTHER -> {
                val viewHolderOther = holder as MessageViewHolderOther
                viewHolderOther.bind(currentMessage)
            }
        }
    }

    class MessageViewHolderMe(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {

            val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            val dayMonthString = dayMonthFormat.format(Date(message.timestamp))

            val hourMinuteFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val hourMinuteString = hourMinuteFormat.format(Date(message.timestamp))

            binding.textGchatDateMe.text = dayMonthString
            binding.textGchatMessageMe.text = message.text
            binding.textGchatTimestampMe.text = hourMinuteString

        }
    }

    class MessageViewHolderOther(private val binding: ReceiveItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            val dayMonthString = dayMonthFormat.format(Date(message.timestamp))

            val hourMinuteFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val hourMinuteString = hourMinuteFormat.format(Date(message.timestamp))

            binding.textGchatMessageOther.text = message.text
            binding.textGchatDateOther.text = dayMonthString
            binding.textGchatTimestampOther.text = hourMinuteString
            binding.textGchatUserOther.text = message.senderUid

            // ... bind other views as needed ...
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
