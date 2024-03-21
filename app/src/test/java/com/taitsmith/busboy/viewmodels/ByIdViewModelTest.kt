package com.taitsmith.busboy.viewmodels

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.maps.model.LatLng
import com.taitsmith.busboy.MainDispatchRule
import com.taitsmith.busboy.api.ApiRepository
import com.taitsmith.busboy.api.BustimeResponse
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.data.ServiceAlert
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations.openMocks

@RunWith(JUnit4::class)
class ByIdViewModelTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatchRule()

    @Mock
    private lateinit var apiRepository: ApiRepository
    @Mock
    private lateinit var databaseRepository: DatabaseRepository
    @Mock
    val app = Application()

    private lateinit var byIdViewModel: ByIdViewModel
    private lateinit var mainActivityViewModel: MainActivityViewModel

    private lateinit var mockedBus: Bus
    private lateinit var mockedServiceAlertResponse: ServiceAlertResponse

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockedPredictions = mutableListOf<Prediction>()
    private val mockedAlerts = mutableListOf<ServiceAlert>()
    private val mockedWaypoints = mutableListOf<LatLng>()

    @Before
    fun setup() {
        openMocks(this)

        createPredictions()
        createMockedBus()
        createServiceAlerts()
        createMockedWaypoints()

        byIdViewModel = ByIdViewModel(databaseRepository, apiRepository)
        mainActivityViewModel = MainActivityViewModel(app)
    }

    private fun createMockedWaypoints() {
        mockedWaypoints.add(0, LatLng(23.2, 42.1))
        mockedWaypoints.add(1, LatLng(123.5, 23.2))
    }

    private fun createMockedBus() {
        mockedBus = Bus()
        mockedBus.vehicleId = 1529
        mockedBus.currentTripId = 2837
        mockedBus.hasAC = false
        mockedBus.hasWiFi = false
        mockedBus.latitude = 84.23
        mockedBus.longitude = -122.23
    }

    private fun createPredictions() {
        val prediction1 = Prediction(
            "Broadway & 25th St",
            "1745",
            "51A",
            "to Fruitvale BART",
            "Fruitvale BART",
            "20230420 16:20"
            )

        val prediction2 = Prediction(
            "Broadway & 25th St",
            "1745",
            "51A",
            "to Fruitvale BART",
            "Fruitvale BART",
            "20230420 16:40"
        )

        mockedPredictions.add(0, prediction1)
        mockedPredictions.add(1, prediction2)
    }

    private fun createServiceAlerts() {
        val alert1 = ServiceAlert()

        val btr = BustimeResponse()
        val sar = ServiceAlertResponse()

        alert1.nm = "Test Alert Please Ignore"
        alert1.sbj = "This is just a test no reason to panic"
        alert1.sbj = "Test"
        alert1.dtl = "A long, detailed message about this service alert"

        alert1.prty = "Medium"
        alert1.cse = "Squid in road"
        alert1.efct = "Discount calamari"

        alert1.srvc = arrayListOf(
            ServiceAlert.ImpactedServices(
                rt = "51A",
                rtdir = "NB",
                stpid = "55555",
                stpnm = "Downtown Berkeley"
            )
        )

        mockedAlerts.add(alert1)
        sar.bustimeResponse = btr
        btr.sb = mockedAlerts
        mockedServiceAlertResponse = sar
    }

    @After
    fun teardown() {
        testDispatcher.cancel()

    }
    @Test
    fun `test predictions added to livedata`() = runTest(testDispatcher) {
        `when`(apiRepository.getStopPredictions("50825", null)).thenReturn(mockedPredictions)
        byIdViewModel.getStopPredictions("50825", null)
        val returnedPredictions = apiRepository.getStopPredictions("50825", null)

        assertEquals(returnedPredictions, mockedPredictions)
        assertEquals(returnedPredictions, byIdViewModel.stopPredictions.getOrAwaitValue())
        assertEquals("50825", byIdViewModel.stopId.getOrAwaitValue())
    }

    @Test
    fun `test bus added to livedata`() = runTest(testDispatcher) {
        `when`(apiRepository.getDetailedBusInfo("1529")).thenReturn(mockedBus)
        byIdViewModel.getBusDetails("1529")
        val returnedBus = apiRepository.getDetailedBusInfo("1529")
        assertEquals(mockedBus, returnedBus)
        assertEquals(returnedBus, byIdViewModel.bus.getOrAwaitValue())
        assertEquals(false, byIdViewModel.isUpdated.getOrAwaitValue())
    }

    @Test
    fun getBusLocation() = runTest(testDispatcher) {
        `when`(apiRepository.getBusLocation("1529")).thenReturn(mockedBus)
        byIdViewModel.getBusLocation("1529")
        val returnedBus = apiRepository.getBusLocation("1529")
        assertEquals(mockedBus, returnedBus)
        assertEquals(returnedBus, byIdViewModel.bus.getOrAwaitValue())
    }

    @Test
    fun `test service alerts added to livedata`() = runTest(testDispatcher) {
        `when`(apiRepository.getServiceAlertsForStop("55555")).thenReturn(mockedServiceAlertResponse)
        byIdViewModel.getStopPredictions("55555", null)

        val returnedAlerts = apiRepository.getServiceAlertsForStop("55555")
        assertEquals(mockedServiceAlertResponse, returnedAlerts)
        assertEquals(mockedServiceAlertResponse, byIdViewModel.alerts.getOrAwaitValue())
    }

    @Test
    fun `test get waypoints updates live data`() = runTest(testDispatcher) {
        `when`(apiRepository.getBusRouteWaypoints("51A")).thenReturn(mockedWaypoints)
        `when`(apiRepository.getBusRouteWaypoints("51B")).thenThrow(RuntimeException("empty_response"))
        byIdViewModel.getWaypoints("51B")
        byIdViewModel.getWaypoints("51A")

        val returnedWaypoints = apiRepository.getBusRouteWaypoints("51A")
        assertEquals(mockedWaypoints, returnedWaypoints)
        assertEquals(mockedWaypoints, byIdViewModel.busRouteWaypoints.getOrAwaitValue())
        assertEquals("NO_WAYPOINTS", MainActivityViewModel.mutableErrorMessage.getOrAwaitValue())
    }

    @Test
    fun `test api 404 error updates status message`() = runTest(testDispatcher) {
       `when`(apiRepository.getStopPredictions("55555", null)).thenThrow(RuntimeException("no_data"))
        byIdViewModel.getStopPredictions("55555", null)

        assertEquals("404", MainActivityViewModel.mutableErrorMessage.getOrAwaitValue())
    }

    @Test
    fun `test no service error updates status message`() = runTest(testDispatcher) {
        `when`(apiRepository.getStopPredictions("55555", null)).thenThrow(RuntimeException("no_service"))
        byIdViewModel.getStopPredictions("55555", null)

        assertEquals("CALL_FAILURE", MainActivityViewModel.mutableErrorMessage.getOrAwaitValue())
    }

    @Test
    fun `test null bus coordinates updates error message`() = runTest(testDispatcher) {
        `when`(apiRepository.getBusLocation("234")).thenThrow(RuntimeException("null_coords"))
        byIdViewModel.getBusLocation("234")
        assertEquals("NULL_BUS_COORDS", MainActivityViewModel.mutableErrorMessage.getOrAwaitValue())
    }

    @Test
    fun `test is updated is updated`() = runTest(testDispatcher) {
        byIdViewModel.setIsUpdated(true)
        byIdViewModel.getBusDetails("54")
        assertEquals(false, byIdViewModel.isUpdated.getOrAwaitValue())
    }

}