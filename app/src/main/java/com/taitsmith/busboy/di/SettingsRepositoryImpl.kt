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

    //TODO this catch terminates the flow instead of recovering it. DataStore.data completes
    //with its exception, so a single transient IOException emits emptyPreferences() once and
    //then ends the stream — which also completes the stateIn sharing coroutine below, freezing
    //selectedAgencyState at AC_TRANSIT for the rest of the process. Everything downstream then
    //silently serves the wrong agency (DelegatingRemoteDataSource routes to AC Transit,
    //DatabaseRepository filters favorites by it) while the stored value still says CTA, and
    //re-picking the agency in Settings writes to disk but can never re-emit. Should retryWhen
    //the IO case and surface a status code on give-up rather than pretending "read failed"
    //means "fresh install". Pre-existing; see the theming PR review.
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
