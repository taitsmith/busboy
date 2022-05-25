package com.taitsmith.busboy.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.MapsApiInterface
import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.api.StopDestinationResponse
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.ui.MainActivity
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.Companion.mutableErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(application: Application,
                                             @AcTransitApiInterface private val acTransitApiInterface: ApiInterface,
                                             @MapsApiInterface private val mapsApiInterface: ApiInterface,
                                             private val databaseRepository: DatabaseRepository
                        ): AndroidViewModel(application) {

    private lateinit var favoriteLinesList: List<StopDestinationResponse.RouteDestination>
    val stopList: MutableLiveData<List<Stop>> by lazy {
        MutableLiveData<List<Stop>>()
    }

    fun getFavoriteStops() {
        viewModelScope.launch(Dispatchers.IO) {
            val stops = databaseRepository.getAllStops()
            if (stops.isEmpty()) mutableErrorMessage.postValue("NO_FAVORITE_STOPS")
            else stopList.postValue(stops)
        }
    }

    fun getFavoriteLines() {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteLinesList = databaseRepository.getAllLines()
        }
    }

    companion object {
        lateinit var favoriteStops: MutableList<Stop>
    }

    init {
        favoriteStops = ArrayList()
    }
}