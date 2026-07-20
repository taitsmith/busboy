package com.taitsmith.busboy.di

import com.taitsmith.busboy.data.Agency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Shared test double for [SettingsRepository]. Holds the selected agency in a StateFlow so tests can
 * flip it mid-run (used by the delegating-source routing tests and the favorites-stamping tests).
 */
internal class FakeSettingsRepository(initial: Agency) : SettingsRepository {
    private val stateFlow = MutableStateFlow(initial)
    override val selectedAgency: StateFlow<Agency> = stateFlow
    override val selectedAgencyState: StateFlow<Agency> = stateFlow
    override suspend fun setAgency(agency: Agency) { stateFlow.value = agency }
}
