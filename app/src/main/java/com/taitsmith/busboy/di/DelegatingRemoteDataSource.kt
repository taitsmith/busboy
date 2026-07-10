package com.taitsmith.busboy.di

import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.api.BustimeResponse
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.api.StopDestinationResponse
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Stop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Provider

/**
 * The RemoteDataSource everything downstream injects. It owns no logic of its own; it picks the
 * concrete provider (AC Transit / CTA) for the currently selected [Agency] and forwards each call.
 *
 * The active provider is resolved at *collection* time — inside each flow builder / on each suspend
 * call — not at construction. That is what lets a mid-session city switch take effect on the next
 * request without recreating ViewModels.
 */
class DelegatingRemoteDataSource @Inject constructor(
    private val sources: Map<Agency, @JvmSuppressWildcards Provider<RemoteDataSource>>,
    private val settingsRepository: SettingsRepository
) : RemoteDataSource {

    private fun active(): RemoteDataSource =
        sources.getValue(settingsRepository.selectedAgencyState.value).get()

    override fun predictions(s: String, r: String?): Flow<BustimeResponse> =
        flow { emitAll(active().predictions(s, r)) }

    override fun serviceAlerts(stpid: String): Flow<ServiceAlertResponse> =
        flow { emitAll(active().serviceAlerts(stpid)) }

    override fun nearbyStops(latLng: LatLng, distance: Int, route: String?): Flow<List<Stop>> =
        flow { emitAll(active().nearbyStops(latLng, distance, route)) }

    override fun linesServedByStop(stops: List<Stop>): Flow<StopDestinationResponse> =
        flow { emitAll(active().linesServedByStop(stops)) }

    override fun vehicleLocation(vid: String): Flow<Bus> =
        flow { emitAll(active().vehicleLocation(vid)) }

    override suspend fun getDetailedBusInfo(vid: String): Bus =
        active().getDetailedBusInfo(vid)

    override suspend fun getBusRouteWaypoints(routeName: String): List<LatLng> =
        active().getBusRouteWaypoints(routeName)

    override suspend fun getDirectionsToStop(start: String, stop: String): List<LatLng> =
        active().getDirectionsToStop(start, stop)
}
