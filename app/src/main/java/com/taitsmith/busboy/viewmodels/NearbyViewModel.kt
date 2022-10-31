package com.taitsmith.busboy.viewmodels

import android.Manifest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.Companion.mutableErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import im.delight.android.location.SimpleLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(application: Application,
                                          private val apiRepository: ApiRepository
                                          ) : AndroidViewModel(application) {

    private val _nearbyStops = MutableLiveData<List<Stop>>()
    val nearbyStops: LiveData<List<Stop>> = _nearbyStops

    private val _locationPermGranted = MutableLiveData<Boolean>()
    var locationPermGranted: LiveData<Boolean> = _locationPermGranted

    var rt: String? = null
    var distance: Int

    fun getNearbyStops() {
        MainActivityViewModel.mutableStatusMessage.value = "LOADING"
        if (rt == null) rt = ""
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val nearbyList = apiRepository.getNearbyStops(
                    37.8096,    //emulator never gets proper coords recently, so for debug
                    -122.2685, //we'll pretend we're near 19th st bart on broadway
                    distance,
                    true,
                    rt
                )
                _nearbyStops.postValue(apiRepository.getLinesServedByStop(nearbyList))
            }.onFailure {
                it.printStackTrace()
                mutableErrorMessage.postValue("404")
            }
        }
    }

    fun checkLocationPerm() {
        if (ContextCompat.checkSelfPermission(
                getApplication<Application>().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!loc.hasLocationEnabled()) {
                mutableErrorMessage.value = "NO_LOC_ENABLED" //granted permissions, but location is disabled.
                _locationPermGranted.value = false
            } else {
                loc.beginUpdates()
                _locationPermGranted.value = true
            }
        } else {
            mutableErrorMessage.value = "NO_PERMISSION" //permissions not granted, so ask for them
        }
    }

    companion object {
        lateinit var loc: SimpleLocation
    }

    init {
        loc = SimpleLocation(application.applicationContext)
        distance = 1000
    }
}