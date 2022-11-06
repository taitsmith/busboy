package com.taitsmith.busboy.di

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationProviderClient: FusedLocationProviderClient
) {

    private val callback = Callback()

    private val _lastLocation = MutableStateFlow<Location?>(null)
    val lastLocation =  _lastLocation.asStateFlow()

    private val _isReceivingUpdates = MutableStateFlow(false)
    val isReceivingLocationUpdates = _isReceivingUpdates.asStateFlow()

    //this can't be called without permission so ignore lint
    @SuppressLint("MissingPermission")
    fun startUpdates() {
        val request = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
            interval = 300_000
        }
        locationProviderClient.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        )
        _isReceivingUpdates.value = true
    }

    fun stopUpdates() {
        locationProviderClient.removeLocationUpdates(callback)
        _lastLocation.value = null
        _isReceivingUpdates.value = false
    }

    private inner class Callback: LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            _lastLocation.value = p0.lastLocation
        }
    }
}