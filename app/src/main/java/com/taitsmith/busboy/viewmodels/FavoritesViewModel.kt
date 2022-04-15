package com.taitsmith.busboy.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.MapsApiInterface
import com.taitsmith.busboy.api.ApiInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(application: Application,
                                             @AcTransitApiInterface private val acTransitApiInterface: ApiInterface,
                                             @MapsApiInterface private val mapsApiInterface: ApiInterface,
                        ): AndroidViewModel(application) {

    init {
    }
}