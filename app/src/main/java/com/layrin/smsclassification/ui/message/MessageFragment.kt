package com.layrin.smsclassification.ui.message

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.layrin.smsclassification.R
import com.layrin.smsclassification.data.model.Message
import com.layrin.smsclassification.databinding.FragmentMessageBinding
import com.layrin.smsclassification.ui.common.BindingFragment
import com.layrin.smsclassification.util.DateTimeUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MessageFragment :
    BindingFragment<FragmentMessageBinding>(),
    ActionMode.Callback,
    MessageAdapter.SelectionMode {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentMessageBinding::inflate

    private val navArgs by navArgs<MessageFragmentArgs>()
    private val viewModel by viewModels<MessageViewModel>()

    private var actionMode: ActionMode? = null
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var dateTimeUtil: DateTimeUtil
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMenuVisibility(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settings = Settings().apply {
            useSystemSending = true
            deliveryReports = true
            sendLongAsMms = false
        }

        subscriptionManager =
            requireActivity().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        setUpFragmentTitle()
        setUpRecyclerView()
        setUpSendMessageButtonListener()
        setUpUiEventFlow()
        setSimCardSelection()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messageFlow.collectLatest { pagingData ->
                messageAdapter.submitData(pagingData)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })
    }

    private fun setUpFragmentTitle() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(500L)
                viewModel.senderName.collect { name ->
                    (activity as AppCompatActivity).supportActionBar?.title =
                        name ?: navArgs.contactPhoneNumber
                }
            }
        }
    }

    private fun setUpSendMessageButtonListener() {
        binding.ibSendMsg.setOnClickListener {
            if (binding.etMessageText.text.isNotBlank() ||
                binding.etMessageText.text.isNotEmpty()
            ) {
                viewModel.onEvent(
                    MessageStateEvent.SendMessageEvent
                )
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        view: View,
        menuInfo: ContextMenu.ContextMenuInfo?,
    ) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val inflater = requireActivity().menuInflater
        inflater.inflate(R.menu.sim_selection_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sim_1 -> {
                sharedPreferences.edit()
                    .putInt(
                        SELECTED_SIM_KEY,
                        subscriptionManager.activeSubscriptionInfoList[0].subscriptionId
                    ).apply()
                Toast.makeText(requireContext(), "Sim 1 selected", Toast.LENGTH_SHORT).show()
                binding.ibSwitchSim.setBackgroundResource(R.drawable.bg_round)
                val icon = requireContext().getDrawable(R.drawable.ic_sim_1)
                binding.ibSwitchSim.setImageDrawable(icon)
                true
            }
            R.id.action_sim_2 -> {
                sharedPreferences.edit()
                    .putInt(
                        SELECTED_SIM_KEY,
                        subscriptionManager.activeSubscriptionInfoList[1].subscriptionId
                    ).apply()
                Toast.makeText(requireContext(), "Sim 2 selected", Toast.LENGTH_SHORT).show()
                binding.ibSwitchSim.setBackgroundResource(R.drawable.bg_round)
                val icon = requireContext().getDrawable(R.drawable.ic_sim_2)
                binding.ibSwitchSim.setImageDrawable(icon)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun sendMessage(number: String, text: String) {
        settings.apply {
            subscriptionId = if (subscriptionManager.activeSubscriptionInfoCount == 2) {
                sharedPreferences.getInt(
                    SELECTED_SIM_KEY,
                    subscriptionManager.activeSubscriptionInfoList[0].subscriptionId
                )
            } else subscriptionManager.activeSubscriptionInfoList[0].subscriptionId
        }

        val currentTime = System.currentTimeMillis()
        val transaction = Transaction(requireContext(), settings)

        val data = Message(
            contactPhoneNumber = number,
            messageReadStatus = true,
            messageType = 0,
            messageTime = currentTime,
            messageText = text
        )
        viewModel.updateConversationData(data)
        val message = com.klinker.android.send_message.Message(
            text,
            number.filter { it != ' ' }
        )
        transaction.sendNewMessage(message, Transaction.NO_THREAD_ID)
    }

    private fun setSimCardSelection() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (subscriptionManager.activeSubscriptionInfoCount != 2) binding.ibSwitchSim.visibility =
            View.GONE
        binding.ibSwitchSim.setOnClickListener {
            registerForContextMenu(it)
            it.showContextMenu()
        }
        binding.ibSwitchSim.setOnLongClickListener {
            unregisterForContextMenu(it)
            false
        }
    }

    private fun setUpUiEventFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventFlow.collect { event ->
                    when (event) {
                        is MessageUiEvent.ShowSnackBar -> {
                            Snackbar.make(
                                binding.root,
                                event.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        is MessageUiEvent.SendMessage -> {
                            val number = navArgs.contactPhoneNumber
                            val text = binding.etMessageText.text
                            sendMessage(
                                number,
                                text.toString()
                            )
                            Toast.makeText(
                                requireContext(),
                                "Sending...",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.etMessageText.text = null
                        }
                    }
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.rvMessage.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
                reverseLayout = true
            }
            dateTimeUtil = DateTimeUtil(requireContext())
            messageAdapter = MessageAdapter(this@MessageFragment, dateTimeUtil)
            adapter = messageAdapter
        }
    }

    private fun toggleMultiSelectionMode(size: Int) {
        if (actionMode == null) actionMode = requireActivity().startActionMode(this)

        if (size > 0) actionMode?.title = size.toString()
        else actionMode?.finish()
    }

    private fun toggleSelectedMessage(position: Int) {
        viewModel.toggleSelectedItem(position) { size ->
            toggleMultiSelectionMode(size)
        }
    }

    private fun isMessageSelected(position: Int): Boolean =
        viewModel.isItemSelected(position)

    override fun setItemBackgroundColor(itemView: View, position: Int) {
        if (viewModel.isItemSelected(position)) itemView.setBackgroundColor(requireActivity().getColor(
            R.color.selected_background))
        else itemView.setBackgroundColor(requireActivity().getColor(R.color.normal_background))
    }

    override fun onItemClick(itemView: View, position: Int) {
        if (viewModel.isMultiSelectionModeActive) {
            toggleSelectedMessage(position)
            if (isMessageSelected(position))
                itemView.setBackgroundColor(requireActivity().getColor(R.color.selected_background))
            else
                itemView.setBackgroundColor(requireActivity().getColor(R.color.normal_background))
        }
    }

    override fun onItemLongClick(itemView: View, position: Int) {
        toggleSelectedMessage(position)
        if (isMessageSelected(position))
            itemView.setBackgroundColor(requireActivity().getColor(R.color.selected_background))
        else
            itemView.setBackgroundColor(requireActivity().getColor(R.color.normal_background))
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.message_multi_selection, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, menuItem: MenuItem?): Boolean {
        val selectedItem = messageAdapter.getSelectedItem(viewModel.selectedMessage)
        val alertDialog = MaterialAlertDialogBuilder(requireActivity())
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        return when (menuItem?.itemId) {
            R.id.action_delete -> {
                val title =
                    resources.getQuantityString(R.plurals.delete_message, selectedItem.size)
                alertDialog.setTitle(title)
                    .setPositiveButton(R.string.action_delete) { dialog, _ ->
                        val messages = selectedItem.map { messageUiState ->
                            MessageUiState.toMessage(messageUiState)
                        }
                        viewModel.onEvent(
                            MessageStateEvent.DeleteMessageEvent(messages)
                        )
                        dialog.dismiss()
                        actionMode?.finish()
                        resetSelectionMode()
                        mode?.finish()
                    }.show()
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
        val data = viewModel.clearSelectedMessages()
        for (message in data) messageAdapter.notifyItemChanged(message)
    }

    companion object {
        const val SELECTED_SIM_KEY = "selected_sim"
    }
}