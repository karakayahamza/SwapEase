import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.swapease.data.models.Message
import com.example.swapease.databinding.ItemMessageBinding
import com.example.swapease.databinding.ReceiveItemMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val messages: MutableList<Pair<MessageType, Message>> = mutableListOf()

    enum class MessageType {
        ME, // Mesaj gönderen kişi
        OTHER // Diğer kişi
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            MessageType.ME.ordinal -> {
                val binding = ItemMessageBinding.inflate(inflater, parent, false)
                MessageViewHolder.Me(binding)
            }
            MessageType.OTHER.ordinal -> {
                val binding = ReceiveItemMessageBinding.inflate(inflater, parent, false)
                MessageViewHolder.Other(binding)
            }
            else -> throw IllegalArgumentException("Geçersiz görünüm türü")
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int = messages[position].first.ordinal

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    fun addMessage(type: MessageType, message: Message) {
        messages.add(type to message)
        Log.d("type",type.name)
        notifyItemInserted(messages.size - 1)
    }

    fun clear() {
        messages.clear()
        notifyDataSetChanged()
    }

    sealed class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(message: Pair<MessageType, Message>)


        class Me(private val binding: ItemMessageBinding) : MessageViewHolder(binding.root) {
            override fun bind(message: Pair<MessageType, Message>) {

                println(message.first.ordinal.toString())
                val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                val dayMonthString = dayMonthFormat.format(Date(message.second.timestamp))

                val hourMinuteFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val hourMinuteString = hourMinuteFormat.format(Date(message.second.timestamp))

                binding.textGchatDateMe.text = dayMonthString
                binding.textGchatMessageMe.text = message.second.text
                binding.textGchatTimestampMe.text = hourMinuteString
            }
        }

        class Other(private val binding: ReceiveItemMessageBinding) : MessageViewHolder(binding.root) {
            override fun bind(message: Pair<MessageType, Message>) {
                Log.d("KJKJKJKJKJ",message.second.text)
                val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                val dayMonthString = dayMonthFormat.format(Date(message.second.timestamp))

                val hourMinuteFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val hourMinuteString = hourMinuteFormat.format(Date(message.second.timestamp))

                binding.textGchatMessageOther.text = message.second.text
                binding.textGchatDateOther.text = dayMonthString
                binding.textGchatTimestampOther.text = hourMinuteString
                binding.textGchatUserOther.text = message.second.senderUserName
            }
        }
    }
}

 private class MessageDiffCallback : DiffUtil.ItemCallback<Pair<MessageAdapter.MessageType, Message>>() {

        override fun areItemsTheSame(
            oldItem: Pair<MessageAdapter.MessageType, Message>,
            newItem: Pair<MessageAdapter.MessageType, Message>
        ): Boolean {
            Log.d("ItemsID1", oldItem.second.messageId.toString())
            Log.d("ItemsID2", newItem.second.messageId.toString())
            Log.d("ItemsID","------------------------------")
            return oldItem.second.messageId == newItem.second.messageId
        }

        override fun areContentsTheSame(
            oldItem: Pair<MessageAdapter.MessageType, Message>,
            newItem: Pair<MessageAdapter.MessageType, Message>
        ): Boolean {
            Log.d("Items",oldItem.second.text)
            Log.d("Items",newItem.second.text)
            return oldItem == newItem
        }
 }
