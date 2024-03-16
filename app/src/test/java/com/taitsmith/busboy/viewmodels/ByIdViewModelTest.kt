package com.taitsmith.busboy.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.taitsmith.busboy.MainDispatchRule
import com.taitsmith.busboy.api.ApiRepository
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

    private lateinit var byIdViewModel: ByIdViewModel
    private lateinit var mainViewModel: MainActivityViewModel
    private lateinit var mockedBus: Bus

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockedPredictions = mutableListOf<Prediction>()
    private val mockedAlerts = mutableListOf<ServiceAlert>()

    @Before
    fun setup() {
        openMocks(this)

        createPredictions()
        createMockedBus()
        createServiceAlerts()

        byIdViewModel = ByIdViewModel(databaseRepository, apiRepository)
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

        alert1.nm = "Test Alert Please Ignore"
        alert1.sbj = "This is just a test no reason to panic"
        alert1.sbj = "Test"
        alert1.dtl = "A long, detailed message about this service alert"

        alert1.prty = "Medium"
        alert1.cse = "Squid in road"
        alert1.efct = "Discount calamari"

        alert1.srvc = arrayListOf(
            ServiceAlert.Srvc(
                rt = "51A",
                rtdir = "NB",
                stpid = "55555",
                stpnm = "Downtown Berkeley"
            )
        )
        mockedAlerts.add(alert1)
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
        `when`(apiRepository.getServiceAlertsForStop("55555")).thenReturn(mockedAlerts)
        byIdViewModel.getStopPredictions("55555", null)

        val returnedAlerts = apiRepository.getServiceAlertsForStop("55555")
        assertEquals(mockedAlerts, returnedAlerts)
        assertEquals(returnedAlerts, byIdViewModel.alerts.getOrAwaitValue())
    }

}