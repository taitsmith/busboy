package com.taitsmith.busboy.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(application: Application,
                                             private val databaseRepository: DatabaseRepository
                                            ): AndroidViewModel(application) {

    fun getFavoriteStops(): Flow<List<Stop>> = databaseRepository.getAllStops()

    companion object {
        lateinit var favoriteStops: MutableList<Stop>
    }

    fun deleteStop(stop: Stop) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.deleteStop(stop)
        }
//        mutableStatusMessage.value = "STOP_DELETED"
    }

    init {
        favoriteStops = ArrayList()
    }
}