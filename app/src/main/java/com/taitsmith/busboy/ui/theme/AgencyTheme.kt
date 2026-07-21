package com.taitsmith.busboy.ui.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.taitsmith.busboy.R
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.di.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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
 * Resolves the theme for the persisted agency, synchronously.
 *
 * Blocking is deliberate and load-bearing. `setTheme()` must happen before
 * `super.onCreate()` so the window background resolved during attach matches the
 * rest of the UI, which rules out both Hilt field injection and any coroutine.
 * Reading [SettingsRepository.selectedAgencyState] instead would not work either:
 * it seeds eagerly with `AC_TRANSIT` and hydrates from DataStore asynchronously,
 * so a CTA user would get a frame of green at every cold start.
 *
 * The cost is one small DataStore read, which is served from memory on every call
 * after the first.
 */
@StyleRes
fun Context.persistedAgencyThemeRes(): Int {
    val settings = EntryPointAccessors
        .fromApplication(applicationContext, SettingsEntryPoint::class.java)
        .settingsRepository()
    return runBlocking { settings.selectedAgency.first() }.themeRes
}
