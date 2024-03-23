package com.taitsmith.busboy.api

import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.di.AcTransitApiInterface
import com.taitsmith.busboy.di.MapsApiInterface
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class RemoteDataSource @Inject constructor (@AcTransitApiInterface
                                            private val acTransitApiInterface: ApiInterface,
                                            @MapsApiInterface
                                            private val mapsApiInterface: ApiInterface
) {
    private val refreshIntervalMillis: Long = 60000

    val predictions: Flow<List<Prediction>> = flow {
        while (true) {
            val response = acTransitApiInterface.getStopPredictionList(stopId, rt)
            emit(response.bustimeResponse!!.prd!!)
            delay(refreshIntervalMillis)
        }
    }

    companion object {
        fun setStopInfo(s: String, r: String?) {
            stopId = s
            rt = r
        }

        private lateinit var stopId: String
        private var rt: String? = null
    }
}