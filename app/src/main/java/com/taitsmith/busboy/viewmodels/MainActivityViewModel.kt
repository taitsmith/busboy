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

    /**
     * Message to show once the Activity has been rebuilt for a new agency.
     *
     * Lives here because switching agencies calls `recreate()`, so a Snackbar shown
     * before that call is torn down before it ever renders. The ViewModelStore does
     * survive `recreate()`, so the message can be parked here and picked up by the
     * new Activity instance.
     *
     * Holds an already-resolved, locale-baked String, and survives `recreate()` but not
     * process death. MainActivity also gates its navigation reset on this being present.
     * That is safe rather than lucky: a switch always originates from Settings, so if the
     * process is reclaimed mid-switch the restored destination is Settings — which is
     * agency-agnostic — and every per-screen ViewModel died with the process, so there is
     * no stale agency data left to reset away from.
     */
    private var pendingAgencyMessage: String? = null

    fun setPendingAgencyMessage(message: String) {
        pendingAgencyMessage = message
    }

    /** Returns the parked message, if any, and clears it so it shows only once. */
    fun consumePendingAgencyMessage(): String? =
        pendingAgencyMessage.also { pendingAgencyMessage = null }

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