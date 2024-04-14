package com.taitsmith.busboy.di

import com.google.android.gms.maps.model.LatLng
import com.slack.eithernet.ApiResult.Failure
import com.slack.eithernet.ApiResult.Success
import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.api.BustimeResponse
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.api.StopDestinationResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Stop
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class AcTransitRemoteDataSourceImpl @Inject constructor (@AcTransitApiInterface
                                                    private val acTransitApiInterface: ApiInterface
) : AcTransitRemoteDataSource {
    //if you've got the app open on the by id screen we'll update it once per minute.
    private val refreshIntervalMillis: Long = 60000

    override fun predictions(s: String, r: String?): Flow<BustimeResponse> = flow {
        while (true) {
            when (val response = acTransitApiInterface.getStopPredictionList(s, r)) {
                is Success -> {
                    emit(response.value.bustimeResponse)
                    delay(refreshIntervalMillis)
                }
                is Failure.ApiFailure -> throw Exception("404")
                is Failure.HttpFailure -> throw Exception("404")
                is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
                is Failure.UnknownFailure -> throw Exception("UNKNOWN")
            }
        }
    }

    override fun serviceAlerts(stpid: String): Flow<ServiceAlertResponse> = flow {
        when (val response = acTransitApiInterface.getServiceAlertsForStop(stpid)) {
            is Success -> emit(response.value)
            is Failure.ApiFailure -> throw Exception("404")
            is Failure.HttpFailure -> throw Exception("404")
            is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
            is Failure.UnknownFailure -> throw Exception("UNKNOWN")
        }
    }

    override fun nearbyStops(latLng: LatLng, distance: Int, route: String?): Flow<List<Stop>> = flow {
        when (val response = acTransitApiInterface.getNearbyStops(
            latLng.latitude,
            latLng.longitude,
            distance,
            true,
            route
        )) {
            is Success -> emit(response.value)
            is Failure.ApiFailure -> throw Exception("404")
            is Failure.HttpFailure -> throw Exception("404")
            is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
            is Failure.UnknownFailure -> throw Exception("UNKNOWN")
        }
    }

    override fun linesServedByStop(stops: List<Stop>): Flow<StopDestinationResponse> = flow {
        stops.forEach { stop ->
            when (val response = acTransitApiInterface.getStopDestinations(stop.stopId)) {
                is Success -> {
                    response.value.stopName = stop.name
                    emit(response.value)
                }
                is Failure.ApiFailure -> throw Exception("404")
                is Failure.HttpFailure -> throw Exception("404")
                is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
                is Failure.UnknownFailure -> throw Exception("UNKNOWN")
            }
        }
    }

    //leave the map open to update the bus location every $refreshIntervalMillis
    override fun vehicleLocation(vid: String): Flow<Bus> = flow {
        while (true) {
            when (val response = acTransitApiInterface.getVehicleInfo(vid)) {
                is Success -> {
                    emit(response.value)
                    delay(refreshIntervalMillis)
                }
                is Failure.ApiFailure -> throw Exception("404")
                is Failure.HttpFailure -> throw Exception("404")
                is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
                is Failure.UnknownFailure -> throw Exception("UNKNOWN")
            }
        }
    }
}