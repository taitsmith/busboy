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

    fun setAgency(agency: Agency) {
        viewModelScope.launch { settingsRepository.setAgency(agency) }
    }
}
