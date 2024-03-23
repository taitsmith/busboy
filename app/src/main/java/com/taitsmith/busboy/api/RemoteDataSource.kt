package com.taitsmith.busboy.api

import com.slack.eithernet.ApiResult.Failure
import com.slack.eithernet.ApiResult.Success
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
    //if you've got the app open on the by id screen we'll update it once per minute.
    private val refreshIntervalMillis: Long = 60000

    val predictions: Flow<List<Prediction>> = flow {
        while (true) {
            when (val response = acTransitApiInterface.getStopPredictionList(stopId, rt)) {
                is Success -> {
                    if (!response.value.bustimeResponse.error.isNullOrEmpty()) throw Exception(
                        response.value.bustimeResponse.error!![0].msg)
                    else emit(response.value.bustimeResponse.prd!!)
                    delay(refreshIntervalMillis)
                }
                is Failure.ApiFailure -> throw Exception("api_failure")
                is Failure.HttpFailure -> throw Exception("http_failure")
                is Failure.NetworkFailure -> throw Exception("no_data")
                is Failure.UnknownFailure -> throw Exception("unknown")
            }

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