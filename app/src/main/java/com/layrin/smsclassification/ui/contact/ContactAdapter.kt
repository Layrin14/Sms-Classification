package com.layrin.smsclassification.ui.contact

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.QuickContactBadge
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.layrin.smsclassification.R
import com.layrin.smsclassification.data.provider.ContactProvider
import com.layrin.smsclassification.databinding.LayoutContactBinding
import kotlin.random.Random

class ContactAdapter :
    PagingDataAdapter<ContactUiState, ContactAdapter.ContactViewHolder>(diffCallback) {

    private var onClickListener: ((ContactUiState) -> Unit)? = null

    inner class ContactViewHolder(
        private val binding: LayoutContactBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ContactUiState) = with(itemView) {
            setOnClickListener {
                onClickListener?.let { listener -> listener(data) }
            }
            binding.apply {
                tvContactName.text = data.contactName
                tvContactNumber.text = data.contactPhoneNumber
                setContactPhoto(data.contactId.toLong(), this@with.context, qcbContact)
            }
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
    }

    fun setOnClickListener(listener: (ContactUiState) -> Unit) {
        onClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            LayoutContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { item -> holder.bind(item) }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ContactUiState>() {
            override fun areItemsTheSame(
                oldItem: ContactUiState,
                newItem: ContactUiState,
            ): Boolean {
                return oldItem.areItemsTheSame(newItem)
            }

            override fun areContentsTheSame(
                oldItem: ContactUiState,
                newItem: ContactUiState,
            ): Boolean {
                return oldItem.areContentsTheSame(newItem)
            }
        }
    }
}