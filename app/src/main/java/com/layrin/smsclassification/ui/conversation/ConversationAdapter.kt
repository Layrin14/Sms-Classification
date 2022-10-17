package com.layrin.smsclassification.ui.conversation

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.QuickContactBadge
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.layrin.smsclassification.R
import com.layrin.smsclassification.data.provider.ContactProvider
import com.layrin.smsclassification.databinding.LayoutConversationBinding
import com.layrin.smsclassification.util.DateTimeUtil
import kotlin.random.Random

class ConversationAdapter(
    private val callback: SelectionMode? = null,
    private val dateTimeUtil: DateTimeUtil,
) : PagingDataAdapter<ConversationUiState, ConversationAdapter.ConversationViewHolder>(diffCallback) {

    inner class ConversationViewHolder(
        private val callback: SelectionMode?,
        val binding: LayoutConversationBinding,
        private val dateTimeUtil: DateTimeUtil,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ConversationUiState) = with(itemView) {
            setOnClickListener {
                callback?.onItemClick(this, absoluteAdapterPosition)
            }
            setOnLongClickListener {
                callback?.onItemLongClick(this, absoluteAdapterPosition)
                true
            }

            binding.apply {
                tvSender.text = data.contactName ?: data.contactPhoneNumber
                tvLastMsg.text = setLatestMessageStyle(
                    data.lastMessage,
                    data.conversationReadStatus,
                    this@with.context
                )
                tvTime.text = setLatestMessageStyle(
                    dateTimeUtil.getCondensedTime(data.conversationTime),
                    data.conversationReadStatus,
                    this@with.context
                )
                tvLabel.apply {
                    val labels = context.resources.getStringArray(R.array.conversation_labels)
                    text = labels[data.conversationLabel]

                    val colors = context.resources.getIntArray(R.array.labels_bg_color)
                    setBackgroundColor(colors[data.conversationLabel])
                }
                setContactPhoto(data.contactId?.toLong(), this@with.context, qcbContact)
            }

            callback?.setItemBackgroundColor(this, absoluteAdapterPosition)
        }

        private fun setContactPhoto(
            id: Long?,
            context: Context,
            quickContactBadge: QuickContactBadge,
        ) {
            val photo =
                if (id != null) ContactProvider.getContactPhoto(id, context)
                else null
            if (photo != null) Glide.with(context).load(photo).into(quickContactBadge)
            else {
                val bgColor = context.resources.getIntArray(R.array.quick_contact_background)
                val random = Random.nextInt(bgColor.size)
                quickContactBadge.setBackgroundColor(bgColor[random])
                quickContactBadge.setImageResource(R.drawable.ic_personal)
            }
        }

        private fun setLatestMessageStyle(
            lastMessage: String,
            readStatus: Boolean,
            context: Context,
        ): SpannableString {
            return SpannableString(lastMessage.replace("\n", " ")).apply {
                if (!readStatus) {
                    setSpan(StyleSpan(Typeface.BOLD),
                        0,
                        length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(ForegroundColorSpan(context.getColor(R.color.primaryTextColor)),
                        0,
                        length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    fun getCurrentItem(position: Int): ConversationUiState? = getItem(position)

    fun getSelectedItem(selected: List<Int>): List<ConversationUiState> {
        return mutableListOf<ConversationUiState>().apply {
            for (position in selected)
                add(this@ConversationAdapter.getItem(position) ?: continue)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        return ConversationViewHolder(
            callback,
            LayoutConversationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            dateTimeUtil
        )
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { item -> holder.bind(item) }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<ConversationUiState>() {
            override fun areItemsTheSame(
                oldItem: ConversationUiState,
                newItem: ConversationUiState,
            ): Boolean {
                return oldItem.areItemsTheSame(newItem)
            }

            override fun areContentsTheSame(
                oldItem: ConversationUiState,
                newItem: ConversationUiState,
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