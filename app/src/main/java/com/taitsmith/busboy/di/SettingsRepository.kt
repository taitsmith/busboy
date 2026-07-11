package com.taitsmith.busboy.di

import com.taitsmith.busboy.data.Agency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * App-wide user settings. Currently just the selected transit [Agency], which drives provider
 * routing in [DelegatingRemoteDataSource]. [selectedAgencyState] is the synchronous handle the
 * delegate reads at collection time so a mid-session switch takes effect on the next call.
 */
interface SettingsRepository {
    val selectedAgency: Flow<Agency>
    val selectedAgencyState: StateFlow<Agency>
    suspend fun setAgency(agency: Agency)
}
