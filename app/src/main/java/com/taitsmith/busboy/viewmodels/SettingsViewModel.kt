package com.taitsmith.busboy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.di.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val selectedAgency: StateFlow<Agency> = settingsRepository.selectedAgencyState

    //TODO unguarded write. dataStore.edit throws IOException on a full or read-only disk, and
    //from a bare launch that reaches the uncaught handler and crashes the app. Even short of
    //that, the RadioGroup checks itself synchronously on tap, before the write is attempted —
    //so on failure the radio reads CTA while the label beside it still reads "Showing AC
    //Transit", no recreate() fires, and the collector never corrects the radio back because
    //the StateFlow value never changed. Should catch IOException specifically (letting
    //CancellationException propagate), report a status code, and re-sync the RadioGroup from
    //the persisted value. Pre-existing; see the theming PR review.
    fun setAgency(agency: Agency) {
        viewModelScope.launch { settingsRepository.setAgency(agency) }
    }
}
