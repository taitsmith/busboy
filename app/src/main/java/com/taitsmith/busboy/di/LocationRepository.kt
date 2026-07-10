package com.taitsmith.busboy.di

import android.location.Location
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val lastLocation: StateFlow<Location?>

    fun startUpdates()

    fun stopUpdates()
}
