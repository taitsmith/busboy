package com.taitsmith.busboy.ui.theme

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.taitsmith.busboy.R
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.di.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

private const val TAG = "AgencyTheme"

/** Ceiling on the blocking read below, so a slow disk degrades instead of hanging the launch. */
private const val SETTINGS_READ_TIMEOUT_MS = 250L

/**
 * The XML theme each agency is skinned with. Deliberately an extension declared in
 * `ui/` rather than a field on the enum, so `data/Agency` stays free of any
 * dependency on `R`.
 */
@get:StyleRes
val Agency.themeRes: Int
    get() = when (this) {
        Agency.AC_TRANSIT -> R.style.AppTheme_AcTransit
        Agency.CTA        -> R.style.AppTheme_Cta
    }

/** Human-readable agency name, for anything user-facing. */
@get:StringRes
val Agency.displayNameRes: Int
    get() = when (this) {
        Agency.AC_TRANSIT -> R.string.agency_ac_transit
        Agency.CTA        -> R.string.agency_cta
    }

/**
 * Pulls [SettingsRepository] straight out of the singleton graph.
 *
 * Needed because Hilt field injection into an `@AndroidEntryPoint` Activity runs
 * *inside* `super.onCreate()`, but the theme has to be chosen before that call.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface SettingsEntryPoint {
    fun settingsRepository(): SettingsRepository
}

/**
 * Reads the persisted agency synchronously, for choosing the Activity theme.
 *
 * Blocking is deliberate. The theme has to be set before `super.onCreate()`, because
 * AppCompat's delegate caches theme-derived state there and the FragmentManager restores
 * fragments there; everything inflated afterwards — including the decor and window
 * background at `setContentView()` — then resolves against the agency theme. That timing
 * rules out both Hilt field injection and any coroutine.
 *
 * Reading [SettingsRepository.selectedAgencyState] instead would not work: it seeds
 * eagerly with `AC_TRANSIT` and hydrates from DataStore asynchronously, so a CTA user
 * would get a frame of green whenever the seed won the race.
 *
 * Cost is one DataStore read. Later calls are served from the singleton's in-memory
 * cache, but the first read in each process is blocking disk I/O on the main thread
 * during Activity creation — hence the timeout.
 *
 * Failure is never fatal: theming is cosmetic, and crashing here would happen before
 * `super.onCreate()`, bricking every launch with no in-app escape. Falling back to
 * AC_TRANSIT self-corrects, because MainActivity reconciles the agency it themed with
 * against [SettingsRepository.selectedAgencyState] and rebuilds on a mismatch.
 */
fun Context.persistedAgency(): Agency {
    val settings = EntryPointAccessors
        .fromApplication(applicationContext, SettingsEntryPoint::class.java)
        .settingsRepository()
    return try {
        runBlocking { withTimeout(SETTINGS_READ_TIMEOUT_MS) { settings.selectedAgency.first() } }
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "settings read exceeded ${SETTINGS_READ_TIMEOUT_MS}ms; using default theme", e)
        Agency.AC_TRANSIT
    } catch (e: Exception) {
        Log.e(TAG, "could not resolve persisted agency; using default theme", e)
        Agency.AC_TRANSIT
    }
}
