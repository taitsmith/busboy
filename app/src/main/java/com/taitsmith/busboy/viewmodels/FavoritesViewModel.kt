package com.taitsmith.busboy.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

    private lateinit var favoriteStopsList: List<Stop>
    private lateinit var favoriteLinesList: List<StopDestinationResponse.RouteDestination>

    fun getFavoriteStops() {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteStopsList = databaseRepository.getAllStops()
            if (favoriteStopsList.isEmpty()) mutableErrorMessage.value = "NO_FAV_STOPS"
        }
    }

    fun getFavoriteLines() {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteLinesList = databaseRepository.getAllLines()
        }
    }

    init {
    }
}