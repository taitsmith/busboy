package com.taitsmith.busboy.data

/**
 * The transit systems Busboy can talk to. The selected agency (persisted via
 * SettingsRepository) drives which RemoteDataSource the DelegatingRemoteDataSource routes to.
 */
enum class Agency {
    AC_TRANSIT,
    CTA
}
