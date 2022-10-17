package com.layrin.smsclassification.ui.start_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layrin.smsclassification.data.repository.StartScreenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject constructor(
    private val repository: StartScreenRepository,
) : ViewModel() {
    private var done = 0

    private var timeTaken = 0L

    private val _progressPercent = MutableStateFlow(0F)
    val progressPercent = _progressPercent.asStateFlow()

    fun getContactList() = viewModelScope.launch {
        repository.getContactList()
    }

    fun loadAndClassify() {
        repository.loadAndClassify(viewModelScope, done, timeTaken) { size, total ->
            val startTime = repository.startTime
            done += size
            timeTaken = System.currentTimeMillis() - startTime
            val progress = done * 100F / total
            _progressPercent.update { progress }
        }
    }
}