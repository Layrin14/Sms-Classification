package com.layrin.smsclassification.ui.conversation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.layrin.smsclassification.R
import com.layrin.smsclassification.databinding.FragmentConversationBinding
import com.layrin.smsclassification.ui.common.BindingFragment
import com.layrin.smsclassification.util.DateTimeUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConversationFragment :
    BindingFragment<FragmentConversationBinding>(),
    ActionMode.Callback,
    ConversationAdapter.SelectionMode,
    MenuProvider {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentConversationBinding::inflate

    private val viewModel by viewModels<ConversationViewModel>()

    private val selectedItemColor = R.color.selected_background
    private val defaultItemColor = R.color.normal_background

    private var actionMode: ActionMode? = null

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var dateTimeUtil: DateTimeUtil

    override fun onResume() {
        super.onResume()

        when {
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED
            -> viewModel.updateContactData()

            requireActivity().packageName == Telephony.Sms.getDefaultSmsPackage(requireContext())
            -> viewModel.updateMessageData()
        }
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
        resetSelectionMode()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setUpRecyclerView()
        setUpUiEventFlow()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllConversation().collectLatest { pagingData ->
                conversationAdapter.submitData(pagingData)
            }
        }

        binding.fabNewMsg.setOnClickListener {
            findNavController()
                .navigate(R.id.action_conversationFragment_to_contactFragment)
        }
    }

    private fun setUpUiEventFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventFlow.collect { showSnackBar ->
                    val event = showSnackBar as ConversationUiEvent.ShowSnackBar
                    Snackbar.make(
                        binding.root,
                        event.message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.rvConversation.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            smoothScrollToPosition(0)
            dateTimeUtil = DateTimeUtil(requireContext())
            conversationAdapter = ConversationAdapter(this@ConversationFragment, dateTimeUtil)
            adapter = conversationAdapter
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.conversation_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_settings -> {
                val action =
                    ConversationFragmentDirections.actionConversationFragmentToSettingsFragment()
                findNavController().navigate(action)
                true
            }
            else -> false
        }
    }

    override fun onItemClick(itemView: View, position: Int) {
        if (viewModel.isMultiSelectionModeActive) {
            toggleSelectedItem(position)
            if (isConversationSelected(position))
                itemView.setBackgroundColor(requireActivity().getColor(selectedItemColor))
            else
                itemView.setBackgroundColor(requireActivity().getColor(defaultItemColor))
        } else {
            val currentItem = conversationAdapter.getCurrentItem(position)
            currentItem?.let { item ->
                findNavController().navigate(
                    ConversationFragmentDirections
                        .actionConversationFragmentToMessageFragment(item.contactPhoneNumber)
                )
            }
        }
    }

    override fun setItemBackgroundColor(itemView: View, position: Int) {
        if (isConversationSelected(position)) itemView.setBackgroundColor(requireActivity().getColor(
            selectedItemColor))
        else itemView.setBackgroundColor(requireActivity().getColor(defaultItemColor))
    }

    override fun onItemLongClick(itemView: View, position: Int) {
        toggleSelectedItem(position)
        if (isConversationSelected(position))
            itemView.setBackgroundColor(requireActivity().getColor(selectedItemColor))
        else
            itemView.setBackgroundColor(requireActivity().getColor(defaultItemColor))
    }

    private fun toggleMultiSelectionMode(size: Int) {
        if (actionMode == null) actionMode = requireActivity().startActionMode(this)

        if (size > 0) actionMode?.title = size.toString()
        else actionMode?.finish()
    }

    private fun toggleSelectedItem(position: Int) {
        viewModel.toggleSelectedItem(position) { size ->
            toggleMultiSelectionMode(size)
        }
    }

    private fun isConversationSelected(position: Int): Boolean {
        return viewModel.isItemSelected(position)
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.conversation_multi_selection, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val selectedItem = conversationAdapter.getSelectedItem(viewModel.selectedConversation)
        val alertDialog = MaterialAlertDialogBuilder(requireActivity())
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        return when (item?.itemId) {
            R.id.action_delete -> {
                val title =
                    resources.getQuantityString(R.plurals.delete_conversation, selectedItem.size)
                alertDialog.setTitle(title)
                    .setPositiveButton(R.string.action_delete) { dialog, _ ->
                        val conversations = selectedItem.map { conversationUiState ->
                            ConversationUiState.toConversation(conversationUiState)
                        }
                        viewModel.onEvent(
                            ConversationStateEvent.DeleteConversationEvent(conversations)
                        )
                        dialog.dismiss()
                        actionMode?.finish()
                        resetSelectionMode()
                        mode?.finish()
                    }.show()
                true
            }
            R.id.action_move -> {
                val labelItems = arrayOf(
                    LabelType.Normal,
                    LabelType.Fraud,
                    LabelType.StatusAds
                )
                var selectedLabel = labelItems[0]
                val title =
                    resources.getQuantityString(R.plurals.change_conversation_label,
                        selectedItem.size)
                alertDialog.setTitle(title).setSingleChoiceItems(
                    labelItems.map { it.toString() }.toTypedArray(),
                    0
                ) { _, selected ->
                    selectedLabel = labelItems[selected]
                }.setPositiveButton(R.string.action_change) { dialog, _ ->
                    val numbers = selectedItem.map { conversationUiState ->
                        ConversationUiState.toConversation(conversationUiState).contactPhoneNumber
                    }
                    viewModel.onEvent(
                        ConversationStateEvent.ChangeConversationLabelEvent(
                            selectedLabel,
                            numbers
                        )
                    )
                    dialog.dismiss()
                    actionMode?.finish()
                    resetSelectionMode()
                    mode?.finish()
                }.show()
                true
            }
            android.R.id.home -> {
                actionMode?.finish()
                resetSelectionMode()
                mode?.finish()
                true
            }
            else -> {
                actionMode?.finish()
                resetSelectionMode()
                mode?.finish()
                false
            }
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        if (viewModel.isMultiSelectionModeActive) resetSelectionMode()
        actionMode = null
    }

    private fun resetSelectionMode() {
        val data = viewModel.clearSelectedConversation()
        for (item in data) conversationAdapter.notifyItemChanged(item)
    }
}