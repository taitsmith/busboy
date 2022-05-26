package com.taitsmith.busboy.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.api.StopDestinationResponse
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.Companion.mutableErrorMessage
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.Companion.mutableStatusMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(application: Application,
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

    companion object {
        lateinit var favoriteStops: MutableList<Stop>
        lateinit var stopToDelete: MutableLiveData<Stop>
    }

    fun deleteStop(stop: Stop) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.deleteStop(stop)
        }
        mutableStatusMessage.value = "STOP_DELETED"
    }

    init {
        favoriteStops = ArrayList()
        stopToDelete = MutableLiveData()
    }
}