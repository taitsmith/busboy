package com.taitsmith.busboy.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import com.taitsmith.busboy.MainDispatchRule
import com.taitsmith.busboy.api.FakeApiRepository
import com.taitsmith.busboy.api.ServiceAlertResponse
import com.taitsmith.busboy.di.ApiRepository
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.di.StatusRepository
import com.taitsmith.busboy.getOrAwaitValue
import com.taitsmith.busboy.viewmodels.ByIdViewModel.BusState
import com.taitsmith.busboy.viewmodels.ByIdViewModel.PredictionState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations.openMocks

@RunWith(JUnit4::class)
class ByIdViewModelTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatchRule()

    @Mock
    private lateinit var databaseRepository: DatabaseRepository
    @Mock
    private lateinit var statusRepository: StatusRepository
    @Mock
    private lateinit var mockedApiRep: ApiRepository


    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val apiRepository = FakeApiRepository()
    private lateinit var mockedServiceAlertResponse: ServiceAlertResponse

    private lateinit var byIdViewModel: ByIdViewModel
    private lateinit var mockedViewModel: ByIdViewModel


    @Before
    fun setup() {
        openMocks(this)
        mockedServiceAlertResponse = apiRepository.createServiceAlerts()
        byIdViewModel = ByIdViewModel(databaseRepository, apiRepository, statusRepository)
        mockedViewModel = ByIdViewModel(databaseRepository, mockedApiRep, statusRepository)
    }

    @After
    fun teardown() {
        testDispatcher.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test vm updates predictions flow`() = runTest {
        byIdViewModel.predictions.value.shouldBeTypeOf<PredictionState.Loading>()

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            byIdViewModel.predictions.collect {}
        }

        byIdViewModel.getPredictions("55555", null)
        val predValue = byIdViewModel.predictions.value

        predValue.shouldBeTypeOf<PredictionState.Success>()
        predValue.predictions.size.shouldBe(2)

        val stopValue = byIdViewModel.stop.value
        stopValue?.stopId.shouldBe("55555")

        collectJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test api 404 updates status message`() = runTest(testDispatcher) {
        mockedViewModel.predictions.value.shouldBeTypeOf<PredictionState.Loading>()

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            mockedViewModel.predictions.collect {}
        }

        mockedViewModel.getPredictions("404", null)
        val predValue = byIdViewModel.predictions.value

        predValue.shouldBeTypeOf<PredictionState.Error>()

        collectJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBusLocation() = runTest {
        byIdViewModel.bus.value.shouldBeTypeOf<BusState.Loading>()

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            byIdViewModel.bus.collect {}
        }

        byIdViewModel.getBusLocation("5", "3")
        val busValue = byIdViewModel.bus.value

        busValue.shouldBeTypeOf<BusState.Initial>()
        busValue.bus.latitude.shouldBe(84.23)
        busValue.bus.vehicleId.shouldBe(1529)

        collectJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test detailed bus info updates live data`() = runTest {
        byIdViewModel.bus.value.shouldBeTypeOf<BusState.Loading>()

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            byIdViewModel.bus.collect {}
        }

        byIdViewModel.getBusDetails("5")
        val busValue = byIdViewModel.bus.asLiveData().getOrAwaitValue()

        busValue.shouldBeTypeOf<BusState.Detail>()
        busValue.bus.latitude.shouldBe(84.23)
        busValue.bus.vehicleId.shouldBe(1529)

        collectJob.cancel()
    }

    @Test
    fun `test service alerts flow`() = runTest(testDispatcher) {
        byIdViewModel.setStopId("55555")
        byIdViewModel.getAlerts()

        val returnedAlerts = apiRepository.serviceAlerts("55555")

        assertEquals(
            mockedServiceAlertResponse.first().bustimeResponse.sb?.get(0)?.nm,
            returnedAlerts.first().bustimeResponse.sb?.get(0)?.nm
        )
        assertEquals(
            mockedServiceAlertResponse.first().bustimeResponse.sb?.get(0)?.nm,
            byIdViewModel.alerts.getOrAwaitValue().bustimeResponse.sb?.get(0)?.nm)
    }

    @Test
    fun `test vm updates waypoints live data`() = runTest(testDispatcher) {
        byIdViewModel.getWaypoints()

        val returnedWaypoints = apiRepository.getBusRouteWaypoints("51")

        assertEquals(
            returnedWaypoints,
            byIdViewModel.busRouteWaypoints.getOrAwaitValue()
        )

        assertEquals(
            false,
            byIdViewModel.isUpdated.getOrAwaitValue()
        )
    }

    @Test
    fun `test misc utility functions`() = runTest {
        byIdViewModel.setIsUpdated(true)
        byIdViewModel.setStopId("55555")
        byIdViewModel.setAlertShown(true)

        assertEquals(
            true,
            byIdViewModel.isUpdated.getOrAwaitValue()
        )

        assertEquals(
            "55555",
            byIdViewModel.stopId.getOrAwaitValue()
        )

        assertEquals(
            true,
            byIdViewModel.alertShown.getOrAwaitValue()
        )
    }
}