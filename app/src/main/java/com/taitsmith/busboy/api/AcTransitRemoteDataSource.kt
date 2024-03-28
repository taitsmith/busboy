package com.taitsmith.busboy.api

import com.google.android.gms.maps.model.LatLng
import com.slack.eithernet.ApiResult.Failure
import com.slack.eithernet.ApiResult.Success
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.AcTransitApiInterface
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class AcTransitRemoteDataSource @Inject constructor (@AcTransitApiInterface
                                                    private val acTransitApiInterface: ApiInterface
) {
    //if you've got the app open on the by id screen we'll update it once per minute.
    private val refreshIntervalMillis: Long = 60000

    val predictions: Flow<List<Prediction>> = flow {
        while (true) {
            when (val response = acTransitApiInterface.getStopPredictionList(stopId, rt)) {
                is Success -> {
                    if (!response.value.bustimeResponse.error.isNullOrEmpty()) {
                        if (response.value.bustimeResponse.error!![0].msg.equals("No service scheduled"))
                            throw Exception("NO_SERVICE_SCHEDULED")
                        else throw Exception("UNKNOWN")
                    }
                    else emit(response.value.bustimeResponse.prd!!)
                    delay(refreshIntervalMillis)
                }
                is Failure.ApiFailure -> throw Exception("404")
                is Failure.HttpFailure -> throw Exception("404")
                is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
                is Failure.UnknownFailure -> throw Exception("UNKNOWN")
            }

        }
    }

    val serviceAlerts: Flow<ServiceAlertResponse> = flow {
        when (val response = acTransitApiInterface.getServiceAlertsForStop(stopId)) {
            is Success -> emit(response.value)
            is Failure.ApiFailure -> throw Exception("404")
            is Failure.HttpFailure -> throw Exception("404")
            is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
            is Failure.UnknownFailure -> throw Exception("UNKNOWN")
        }
    }

    val nearbyStops: Flow<List<Stop>> = flow {
        when (val response = acTransitApiInterface.getNearbyStops(latLng.latitude,
            latLng.longitude,
            1000,
            true,
            rt)) {
            is Success -> {
                stopList = response.value
                emit(response.value)
            }
            is Failure.ApiFailure -> throw Exception("404")
            is Failure.HttpFailure -> throw Exception("404")
            is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
            is Failure.UnknownFailure -> throw Exception("UNKNOWN")
        }
    }

    val nearbyLinesServed: Flow<Stop> = flow {
        stopList.forEach { stop ->
            when (val destinations = acTransitApiInterface.getStopDestinations(stop.stopId)) {
                is Success -> {
                    val currentStop = destinations.value
                    val sb = StringBuilder()
                   currentStop.routeDestinations?.forEach {
                        sb.append(it.routeId)
                            .append(" ")
                            .append(it.destination)
                            .append("\n")
                        stop.linesServed = sb.toString()
                   }
                }
                is Failure.ApiFailure -> throw Exception("404")
                is Failure.HttpFailure -> throw Exception("404")
                is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
                is Failure.UnknownFailure -> throw Exception("UNKNOWN")
            }
            emit(stop)
        }
    }

    companion object {
        fun setStopInfo(s: String, r: String?) {
            stopId = s
            rt = r
        }

        fun setNearbyInfo(lat: Double, lng: Double, d: Int, r: String?) {
            latLng = LatLng(lat, lng)
            distance = d
            rt = r
        }

        private lateinit var stopId: String
        private lateinit var latLng: LatLng
        private lateinit var stopList: List<Stop>
        private var distance: Int = 500
        private var rt: String? = null
    }
}