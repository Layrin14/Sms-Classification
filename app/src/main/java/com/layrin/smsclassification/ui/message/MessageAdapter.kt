package com.layrin.smsclassification.ui.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.layrin.smsclassification.R
import com.layrin.smsclassification.databinding.LayoutMessageReceiveBinding
import com.layrin.smsclassification.databinding.LayoutMessageSendBinding
import com.layrin.smsclassification.util.DateTimeUtil
import com.layrin.smsclassification.util.updateMessageReadStatus

class MessageAdapter(
    private val callback: SelectionMode? = null,
    private val dateTimeUtil: DateTimeUtil
) : PagingDataAdapter<MessageUiState, RecyclerView.ViewHolder>(diffCallback) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.messageType == ITEM_VIEW_TYPE_RECEIVE) 1
        else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_SEND -> {
                MessageSendViewHolder(
                    LayoutMessageSendBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ),
                    callback,
                    dateTimeUtil
                )
            }
            ITEM_VIEW_TYPE_RECEIVE -> {
                MessageReceiveViewHolder(
                    LayoutMessageReceiveBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ),
                    callback,
                    dateTimeUtil
                )
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { item ->
            when (holder) {
                is MessageSendViewHolder -> {
                    holder.bind(item)
                }
                is MessageReceiveViewHolder -> {
                    holder.bind(item)
                }
                else -> return@let
            }
        }
    }

    fun getSelectedItem(selectedItem: List<Int>): List<MessageUiState> {
        return mutableListOf<MessageUiState>().apply {
            for (item in selectedItem)
                add(this@MessageAdapter.getItem(item) ?: continue)
        }
    }

    inner class MessageSendViewHolder(
        private val binding: LayoutMessageSendBinding,
        private val callback: SelectionMode?,
        private val dateTimeUtil: DateTimeUtil
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MessageUiState) = with(itemView) {
            setOnClickListener {
                callback?.onItemClick(this, absoluteAdapterPosition)
            }
            setOnLongClickListener {
                callback?.onItemLongClick(this, absoluteAdapterPosition)
                true
            }
            binding.apply {
                tvMessageText.text = data.messageText
                tvMessageTime.text = dateTimeUtil.getCondensedTime(data.messageTime)
                tvSentStatus.text = this@with.context.getText(R.string.sent)
                tvSentStatus.visibility = View.VISIBLE
            }
            callback?.setItemBackgroundColor(this, absoluteAdapterPosition)
            this.context.updateMessageReadStatus(data.id)
        }
    }

    inner class MessageReceiveViewHolder(
        private val binding: LayoutMessageReceiveBinding,
        private val callback: SelectionMode?,
        private val dateTimeUtil: DateTimeUtil
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MessageUiState) = with(itemView) {
            setOnClickListener {
                callback?.onItemClick(this, absoluteAdapterPosition)
            }
            setOnLongClickListener {
                callback?.onItemLongClick(this, absoluteAdapterPosition)
                true
            }
            binding.apply {
                tvMessageText.text = data.messageText
                tvMessageTime.text = dateTimeUtil.getCondensedTime(data.messageTime)
            }
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_SEND = 0
        const val ITEM_VIEW_TYPE_RECEIVE = 1
        val diffCallback = object : DiffUtil.ItemCallback<MessageUiState>() {
            override fun areItemsTheSame(
                oldItem: MessageUiState,
                newItem: MessageUiState,
            ): Boolean {
                return oldItem.areItemsTheSame(newItem)
            }

            override fun areContentsTheSame(
                oldItem: MessageUiState,
                newItem: MessageUiState,
            ): Boolean {
                return oldItem.areContentsTheSame(newItem)
            }
        }
    }

    interface SelectionMode {
        fun onItemClick(itemView: View, position: Int)
        fun onItemLongClick(itemView: View, position: Int)
        fun setItemBackgroundColor(itemView: View, position: Int)
    }
}