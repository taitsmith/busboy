package com.taitsmith.busboy.di

import com.taitsmith.busboy.data.Agency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory settings backing. Defaults to [Agency.AC_TRANSIT] so behavior is unchanged until the
 * settings UI lands. Phase 2 replaces this with a DataStore-backed implementation that persists
 * the choice across launches; the interface stays the same.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor() : SettingsRepository {
    private val _selectedAgency = MutableStateFlow(Agency.AC_TRANSIT)

    override val selectedAgency: StateFlow<Agency> = _selectedAgency.asStateFlow()
    override val selectedAgencyState: StateFlow<Agency> = _selectedAgency.asStateFlow()

    override suspend fun setAgency(agency: Agency) {
        _selectedAgency.value = agency
    }
}
