package com.example.swapease.ui.adapters

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
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

class MessageAdapter: ListAdapter<Pair<MessageAdapter.MessageType, Message>, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    enum class MessageType {
        ME, // Mesaj gönderen kişi
        OTHER // Diğer kişi
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MessageType.ME.ordinal -> {
                val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context.applicationContext), parent, false)
                MessageViewHolderMe(binding)
            }
            MessageType.OTHER.ordinal -> {
                val binding = ReceiveItemMessageBinding.inflate(LayoutInflater.from(parent.context.applicationContext), parent, false)
                MessageViewHolderOther(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }
    override fun getItemViewType(position: Int): Int {
        return getItem(position).first.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (messageType, currentMessage) = getItem(position)
        when (holder.itemViewType) {
            MessageType.ME.ordinal -> {
                val viewHolderMe = holder as MessageViewHolderMe
                viewHolderMe.bind(currentMessage)
            }
            MessageType.OTHER.ordinal -> {
                val viewHolderOther = holder as MessageViewHolderOther
                viewHolderOther.bind(currentMessage)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    fun addMessage(type: MessageType, message: Message) {
        submitList(currentList + listOf(type to message))
    }

    fun clear(){
        currentList.clear()
    }

    class MessageViewHolderMe(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            // Bind the views for messages sent by the current user (VIEW_TYPE_ME)
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
            // Bind the views for messages sent by other users (VIEW_TYPE_OTHER)
            val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            val dayMonthString = dayMonthFormat.format(Date(message.timestamp))

            val hourMinuteFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val hourMinuteString = hourMinuteFormat.format(Date(message.timestamp))


            println("-- ${message.text} --")
            binding.textGchatMessageOther.text = message.text
            binding.textGchatDateOther.text = dayMonthString
            binding.textGchatTimestampOther.text = hourMinuteString
            binding.textGchatUserOther.text = message.senderUid
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Pair<MessageType, Message>>() {

        /*override fun areItemsTheSame(oldItem: Pair<MessageAdapter.MessageType, Message>, newItem: Pair<MessageAdapter.MessageType, Message>): Boolean {
            return oldItem.second.messageId == newItem.second.messageId
        }*/

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: Pair<MessageType, Message>,
            newItem: Pair<MessageType, Message>
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: Pair<MessageType, Message>,
            newItem: Pair<MessageType, Message>,
        ): Boolean {
            return true
        }
    }
}
