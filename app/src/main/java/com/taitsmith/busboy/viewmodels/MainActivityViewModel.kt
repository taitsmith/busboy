package com.taitsmith.busboy.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import javax.inject.Inject
import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.api.DirectionResponse
import com.taitsmith.busboy.api.WaypointResponse
import com.google.android.gms.maps.model.LatLng
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.R
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.MapsApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

@HiltViewModel
class MainActivityViewModel @Inject constructor(application: Application,
                                                @AcTransitApiInterface private val acTransitApiInterface: ApiInterface,
                                                @MapsApiInterface private val mapsApiInterface: ApiInterface,
                                                private val apiRepository: ApiRepository) : AndroidViewModel(application) {



    companion object {
        lateinit var mutableStatusMessage: MutableLiveData<String>
        lateinit var mutableErrorMessage: MutableLiveData<String>
    }

    init {
        mutableStatusMessage = MutableLiveData()
        mutableErrorMessage = MutableLiveData()
    }
}