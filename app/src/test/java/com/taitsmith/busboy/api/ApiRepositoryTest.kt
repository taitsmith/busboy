package com.taitsmith.busboy.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.MainDispatchRule
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.di.ApiRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.MockitoAnnotations.openMocks

@RunWith(JUnit4::class)
class ApiRepositoryTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatchRule()

    private lateinit var apiRepository: ApiRepositoryImpl

    @Before
    fun setup() {
        openMocks(this)
        apiRepository = ApiRepositoryImpl(FakeRemoteDataSource())
    }

    @Test
    fun `test predictions`() = runTest {
        val prediction = apiRepository.stopPredictions("good", null).first()
        assertEquals(
            "Broadway & 25th St",
            prediction[0].stpnm,)
        try {
            apiRepository.stopPredictions("bad", null).first()
        } catch (e: Exception) {
            assertEquals(
                "NO_SERVICE_SCHEDULED",
                e.message
            )
        }
    }

    @Test
    fun `test service alerts`() = runTest {
        val alert = apiRepository.serviceAlerts("23").first()
        assertEquals(alert.bustimeResponse.sb?.get(0)?.nm, "fake service alert")
    }

    @Test
    fun `test nearby stops`() = runTest {
        val stop = apiRepository.getNearbyStops(LatLng(1.1, 1.1), 10, "51A").first()
        assertEquals(stop[0].stopId, "55555")
    }

    @Test
    fun `test nearby lines served`() = runTest {
        val stopList = listOf(
            Stop(name = "good"),
            Stop(name = "good"),
            Stop(name = "good"),
            Stop(name = "bad"),
            Stop(name = "good"),
        )

        val returnList = apiRepository.getLinesServedByStops(stopList).toList()

        assertEquals(
            4,
            returnList.size
        )

        assertEquals(
            "51A to good destination",
            returnList[0].linesServed?.trim()
        )
    }

    @Test
    fun `test vehicle location`() = runTest {
        val goodBus = apiRepository.vehicleLocation("1234").first()
        assertEquals(
            1529,
            goodBus.vehicleId
        )

        try {
            val badBus = apiRepository.vehicleLocation("4321").first()
        } catch (e: Exception) {
            assertEquals(
                "NULL_BUS_COORDS",
                e.message
            )
        }
    }

    @Test
    fun `test detailed bus info`() = runTest {
        val bus = apiRepository.getDetailedBusInfo("1234")
        assertEquals(
            1529,
            bus.vehicleId
        )
    }

    @Test
    fun `test bus route waypoints`() = runTest {
        val waypoints = apiRepository.getBusRouteWaypoints("51A")
        assertEquals(
            1,
            waypoints.size
        )
        assertEquals(
            1.1,
            waypoints[0].latitude,
            0.0
        )
    }

    @Test
    fun `test directions to stop`() = runTest {
        val directions = apiRepository.getDirectionsToStop("start", "stop")
        assertEquals(
            1,
            directions.size
        )
        assertEquals(
            1.1,
            directions[0].latitude,
            0.0
        )
    }
}