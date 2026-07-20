package com.taitsmith.busboy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.di.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val statusRepository: StatusRepository
)  : ViewModel() {

    //forward the repository's one-shot events as a SharedFlow (replay 0) so the Activity, which only
    //collects while STARTED, gets every event without conflation and without replaying a stale one.
    private val _uiState = MutableSharedFlow<LoadingState>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiState: SharedFlow<LoadingState> = _uiState

    init {
        viewModelScope.launch {
            statusRepository.state.collect {
                _uiState.emit(it)
            }
        }
    }

    sealed class LoadingState {
        data object Loading : LoadingState()
        data object Success : LoadingState()
        data class StatusUpdate(val msg: String) : LoadingState()
    }
}
