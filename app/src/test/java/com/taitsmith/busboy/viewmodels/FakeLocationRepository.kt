package com.taitsmith.busboy.viewmodels

import android.location.Location
import android.location.LocationManager
import com.taitsmith.busboy.di.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLocationRepository : LocationRepository {

    private val _lastLocation = MutableStateFlow(fakeLocation())
    override val lastLocation = _lastLocation.asStateFlow()

    override fun startUpdates() {
    }

    override fun stopUpdates() {
        TODO("Not yet implemented")
    }

    private fun fakeLocation(): Location {
        val loc = Location(LocationManager.GPS_PROVIDER)
        loc.latitude = 1.1
        loc.latitude = 1.1
        return loc
    }
}