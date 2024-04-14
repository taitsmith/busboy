package com.taitsmith.busboy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.di.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val statusRepository: StatusRepository
)  : ViewModel() {

    private val _uiState = MutableStateFlow<LoadingState>(LoadingState.Success)
    val uiState: StateFlow<LoadingState> = _uiState

    init {
        viewModelScope.launch {
            statusRepository.state.collect {
                _uiState.value = it
            }
        }
    }

    sealed class LoadingState {
        data object Loading : LoadingState()
        data object Success : LoadingState()
        data class StatusUpdate(val msg: String) : LoadingState()
    }
}