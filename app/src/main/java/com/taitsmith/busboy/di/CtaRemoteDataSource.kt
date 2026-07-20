package com.taitsmith.busboy.di

import com.google.android.gms.maps.model.LatLng
import com.slack.eithernet.ApiResult.Failure
import com.slack.eithernet.ApiResult.Success
import com.taitsmith.busboy.api.ApiInterface
import com.taitsmith.busboy.api.BustimeResponse
import com.taitsmith.busboy.api.CtaApiInterface
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.api.toServiceAlert
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Stop
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * CTA implementation of [RemoteDataSource]. Maps CTA BusTracker responses into the same domain types
 * (Prediction/Bus/ServiceAlertResponse/LatLng) the AC Transit source produces, so nothing downstream
 * changes. Uses the identical EitherNet failure -> string-code contract.
 *
 * nearbyStops/linesServedByStop are served from the bundled GTFS catalog in Phase 4; until then they
 * return nothing so the Nearby screen degrades gracefully rather than erroring. Walking directions
 * reuse the provider-independent Google Maps interface.
 */
class CtaRemoteDataSource @Inject constructor(
    private val ctaApiInterface: CtaApiInterface,
    @MapsApiInterface
    private val mapsApiInterface: ApiInterface,
    private val ctaStopCatalog: CtaStopCatalog
) : RemoteDataSource {
    private val refreshIntervalMillis: Long = 60000
    //the Nearby screen's search distance is entered in feet; convert to meters for the geo query
    private val feetToMeters: Double = 0.3048

    override fun predictions(s: String, r: String?): Flow<BustimeResponse> = flow {
        while (true) {
            when (val response = ctaApiInterface.getPredictions(s, r)) {
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
        //Phase 3: unfiltered — all active detours. Phase 4 filters to the stop's routes via the catalog.
        when (val response = ctaApiInterface.getDetours(null)) {
            is Success -> {
                val alerts = response.value.bustimeResponse.dtrs.orEmpty().map { it.toServiceAlert() }
                emit(ServiceAlertResponse(BustimeResponse().apply { sb = alerts }))
            }
            is Failure.ApiFailure -> throw Exception("404")
            is Failure.HttpFailure -> throw Exception("404")
            is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
            is Failure.UnknownFailure -> throw Exception("UNKNOWN")
        }
    }

    override fun nearbyStops(latLng: LatLng, distance: Int, route: String?): Flow<List<Stop>> = flow {
        val stops = ctaStopCatalog
            .nearbyStops(latLng.latitude, latLng.longitude, distance * feetToMeters, route)
            .map {
                Stop(
                    stopId = it.stopId,
                    name = it.name,
                    latitude = it.lat,
                    longitude = it.lon,
                    linesServed = it.linesServed
                )
            }
        emit(stops)
    }

    override fun linesServedByStop(stops: List<Stop>): Flow<Stop> = flow {
        //catalog stops already carry linesServed; enrich any that don't (e.g. a favorite added from
        //the By-ID screen) by looking the stop up in the catalog. One emission per input stop.
        stops.forEach { stop ->
            val lines = stop.linesServed ?: ctaStopCatalog.linesServedFor(stop.stopId)
            emit(stop.copy(linesServed = lines))
        }
    }

    override fun vehicleLocation(vid: String): Flow<Bus> = flow {
        while (true) {
            when (val response = ctaApiInterface.getVehicles(vid)) {
                is Success -> {
                    val bus = response.value.bustimeResponse.vehicle?.firstOrNull()
                        ?: throw Exception("NULL_BUS_COORDS")
                    emit(bus.toBus())
                    delay(refreshIntervalMillis)
                }
                is Failure.ApiFailure -> throw Exception("404")
                is Failure.HttpFailure -> throw Exception("404")
                is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
                is Failure.UnknownFailure -> throw Exception("UNKNOWN")
            }
        }
    }

    override suspend fun getDetailedBusInfo(vid: String): Bus {
        //CTA has no vehicle-characteristics endpoint, so reuse the basic getvehicles result.
        return when (val response = ctaApiInterface.getVehicles(vid)) {
            is Success -> response.value.bustimeResponse.vehicle?.firstOrNull()?.toBus()
                ?: throw Exception("NULL_BUS_COORDS")
            is Failure.ApiFailure -> throw Exception("404")
            is Failure.HttpFailure -> throw Exception("404")
            is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
            is Failure.UnknownFailure -> throw Exception("UNKNOWN")
        }
    }

    override suspend fun getBusRouteWaypoints(routeName: String): List<LatLng> {
        return when (val response = ctaApiInterface.getPatterns(routeName)) {
            is Success -> {
                val patterns = response.value.bustimeResponse.ptr.orEmpty()
                if (patterns.isEmpty()) throw Exception("empty_response")
                //draw the longest pattern (most points), ordered by point sequence
                val points = patterns.maxByOrNull { it.pt?.size ?: 0 }?.pt.orEmpty()
                    .sortedBy { it.seq ?: 0 }
                    .mapNotNull { p ->
                        val lat = p.lat
                        val lon = p.lon
                        if (lat != null && lon != null) LatLng(lat, lon) else null
                    }
                if (points.isEmpty()) throw Exception("empty_response")
                points
            }
            is Failure.ApiFailure -> throw Exception("404")
            is Failure.HttpFailure -> throw Exception("404")
            is Failure.NetworkFailure -> throw Exception("CALL_FAILURE")
            is Failure.UnknownFailure -> throw Exception("UNKNOWN")
        }
    }

    override suspend fun getDirectionsToStop(start: String, stop: String): List<LatLng> {
        val polylineCoords: MutableList<LatLng> = ArrayList()

        val directionResponse = mapsApiInterface.getNavigationToStop(start, stop, "walking")

        //firstOrNull() guards non-null but empty route/trip lists that .get(0) would throw on; an
        //empty result is handled as a DIRECTION_FAILURE upstream.
        val stepList = directionResponse.routeList?.firstOrNull()?.tripList?.firstOrNull()?.stepList

        stepList?.forEach {
            it.endCoords?.returnCoords()?.let { it1 -> polylineCoords.add(it1) }
        }

        return polylineCoords
    }
}
