package com.taitsmith.busboy.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.taitsmith.busboy.data.Agency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed persistence of the selected transit [Agency]. Defaults to [Agency.AC_TRANSIT]
 * when nothing is stored or the value is unreadable, so the app behaves exactly as before for a
 * fresh install. [selectedAgencyState] is an eagerly-shared StateFlow so [DelegatingRemoteDataSource]
 * can read the current agency synchronously at collection time.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @AppCoroutineScope private val scope: CoroutineScope
) : SettingsRepository {

    override val selectedAgency: Flow<Agency> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[AGENCY_KEY].toAgency() }

    override val selectedAgencyState: StateFlow<Agency> =
        selectedAgency.stateIn(scope, SharingStarted.Eagerly, Agency.AC_TRANSIT)

    override suspend fun setAgency(agency: Agency) {
        dataStore.edit { it[AGENCY_KEY] = agency.name }
    }

    private fun String?.toAgency(): Agency =
        this?.let { runCatching { Agency.valueOf(it) }.getOrNull() } ?: Agency.AC_TRANSIT

    companion object {
        private val AGENCY_KEY = stringPreferencesKey("selected_agency")
    }
}
