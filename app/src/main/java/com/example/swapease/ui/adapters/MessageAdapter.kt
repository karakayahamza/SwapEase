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
        ME, // Message sent by the user
        OTHER // Message sent by the other user
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
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int = messages[position].first.ordinal

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    fun addMessage(type: MessageType, message: Message) {
        messages.add(type to message)
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
                // Bind data for messages sent by the user
                val dayMonthString = formatDate(message.second.timestamp, "dd MMM")
                val hourMinuteString = formatDate(message.second.timestamp, "HH:mm")

                binding.textGchatDateMe.text = dayMonthString
                binding.textGchatMessageMe.text = message.second.text
                binding.textGchatTimestampMe.text = hourMinuteString
            }
        }

        class Other(private val binding: ReceiveItemMessageBinding) : MessageViewHolder(binding.root) {
            override fun bind(message: Pair<MessageType, Message>) {
                // Bind data for messages sent by the other user
                val dayMonthString = formatDate(message.second.timestamp, "dd MMM")
                val hourMinuteString = formatDate(message.second.timestamp, "HH:mm")

                binding.textGchatMessageOther.text = message.second.text
                binding.textGchatDateOther.text = dayMonthString
                binding.textGchatTimestampOther.text = hourMinuteString
                binding.textGchatUserOther.text = message.second.senderUserName
            }
        }
        protected fun formatDate(timestamp: Long, format: String): String {
            // Helper function to format date based on provided format
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            return dateFormat.format(Date(timestamp))
        }
    }
}
