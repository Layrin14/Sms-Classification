package com.layrin.smsclassification.ui.start_screen

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.viewbinding.ViewBinding
import com.layrin.smsclassification.R
import com.layrin.smsclassification.databinding.FragmentStartScreenBinding
import com.layrin.smsclassification.ui.common.BindingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


@AndroidEntryPoint
class StartScreenFragment : BindingFragment<FragmentStartScreenBinding>() {

    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentStartScreenBinding::inflate

    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel by viewModels<StartScreenViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (sharedPreferences.getBoolean(KEY_INIT, false)) {
            findNavController().navigate(R.id.action_startScreenFragment_to_conversationFragment)
            return
        }

        setProgressBar()

        binding.btnNext.setOnClickListener {
            binding.tvEta.text = ""
            load()
        }
    }

    private fun setProgressBar() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progressPercent.collect { progress ->
                    if ((progress > 0) && (progress < 100)) {
                        binding.tvProgress.text =
                            getString(R.string.progress_percent, progress.roundToInt())
                        binding.progressBar.progress = progress.roundToInt()
                        binding.tvEta.text = when (progress) {
                            in 50.0..55.0 -> getString(R.string.halfway_done_message)
                            in 90.0..100.0 -> getString(R.string.soon)
                            else -> getString(R.string.load_message)
                        }
                    } else if (progress == 0F) binding.tvEta.text =
                        getString(R.string.eta_string_start)
                    else binding.tvEta.text = ""
                }
            }
        }
    }

    private fun load() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnNext.visibility = View.INVISIBLE
        binding.tvEta.text = getString(R.string.soon)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getContactList()
            viewModel.loadAndClassify()
            withContext(Dispatchers.Main) {
                findNavController()
                    .navigate(StartScreenFragmentDirections.actionStartScreenFragmentToConversationFragment())
                sharedPreferences.edit().putBoolean(KEY_INIT, true).apply()
            }
        }
    }

    companion object {
        private const val KEY_INIT = "organized"
    }
}