package com.layrin.smsclassification.ui.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.layrin.smsclassification.databinding.FragmentContactBinding
import com.layrin.smsclassification.ui.common.BindingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactFragment :
    BindingFragment<FragmentContactBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentContactBinding::inflate

    private val viewModel by viewModels<ContactViewModel>()

    private lateinit var contactAdapter: ContactAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })
    }

    private fun setUpRecyclerView() {
        binding.rvContact.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            contactAdapter = ContactAdapter()
            adapter = contactAdapter
        }
        getContact()
        binding.etSearchContact.editText?.doOnTextChanged { text, _, _, _ ->
            text?.let { query ->
                getContact(query.toString())
            }
        }
        binding.etSearchContact.setEndIconOnClickListener {
            binding.etSearchContact.editText?.text = null
        }
        contactAdapter.setOnClickListener { item ->
            findNavController()
                .navigate(ContactFragmentDirections.actionContactFragmentToMessageFragment(item.contactPhoneNumber))
        }
    }

    private fun getContact(query: String = "") {
        viewModel.onEvent(
            ContactStateEvent.SearchContactEvent(query.trim())
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contactFlow.collectLatest { pagingData ->
                contactAdapter.submitData(pagingData)
            }
        }
        contactAdapter.notifyDataSetChanged()
    }
}

