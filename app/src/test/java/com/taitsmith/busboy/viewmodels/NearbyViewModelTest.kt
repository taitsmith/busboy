package com.taitsmith.busboy.viewmodels

import android.app.Application
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.taitsmith.busboy.MainDispatchRule
import com.taitsmith.busboy.api.FakeApiRepository
import com.taitsmith.busboy.di.StatusRepository
import com.taitsmith.busboy.getOrAwaitValue
import com.taitsmith.busboy.viewmodels.NearbyViewModel.ListLoadingState
import com.taitsmith.busboy.viewmodels.NearbyViewModel.NearbyStopsState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations.openMocks

@RunWith(JUnit4::class)
class NearbyViewModelTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatchRule()

    @Mock
    private lateinit var location: Location
    @Mock
    private lateinit var application: Application
    private lateinit var statusRepository: StatusRepository

    private lateinit var nearbyViewModel: NearbyViewModel
    private lateinit var mainActivityViewModel: MainActivityViewModel

    private val locationRepository = FakeLocationRepository()

    @Before
    fun setup() {
        openMocks(this)
        statusRepository = StatusRepository()

        mainActivityViewModel = MainActivityViewModel(statusRepository)
        nearbyViewModel = NearbyViewModel(application, FakeApiRepository(), statusRepository, locationRepository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test nearby stops`() = runTest {
        val statusJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            statusRepository.state.collect {}
        }
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            nearbyViewModel.nearbyStopsState.collect {}
        }
        `when`(location.longitude).thenReturn(1.1)
        `when`(location.latitude).thenReturn(1.1)

        nearbyViewModel.setLocation(location)

        nearbyViewModel.getNearbyStops()

        val nss = nearbyViewModel.nearbyStopsState.value

        statusRepository.state.value.shouldBeTypeOf<MainActivityViewModel.LoadingState.Loading>()
        nss.shouldBeTypeOf<NearbyStopsState.Loading>()
        nss.loadState.shouldBe(ListLoadingState.PARTIAL)

        nearbyViewModel.getNearbyStopsWithLines(nss.stopList)

        //once the lines-served stream finishes, the state settles on COMPLETE carrying the enriched
        //stops (the fake emits lines for one stop, so one enriched stop is accumulated).
        val completed = nearbyViewModel.nearbyStopsState.value.shouldBeTypeOf<NearbyStopsState.Loading>()
        completed.loadState.shouldBe(ListLoadingState.COMPLETE)
        completed.stopList.size.shouldBe(1)

        collectJob.cancel()
        statusJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test reset returns state to initial`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            nearbyViewModel.nearbyStopsState.collect {}
        }
        `when`(location.longitude).thenReturn(1.1)
        `when`(location.latitude).thenReturn(1.1)

        nearbyViewModel.setLocation(location)
        nearbyViewModel.setIsUsingLocation(false)
        nearbyViewModel.enableSearchButton.value.shouldBe(true)
        NearbyViewModel.currentLocation.latitude.shouldBe(1.1)

        nearbyViewModel.reset()

        nearbyViewModel.enableSearchButton.value.shouldBe(false)
        nearbyViewModel.isUsingLocation.shouldBe(false)
        NearbyViewModel.currentLocation.latitude.shouldBe(0.0)
        val nss = nearbyViewModel.nearbyStopsState.value.shouldBeTypeOf<NearbyStopsState.Loading>()
        nss.loadState.shouldBe(ListLoadingState.START)
        nss.stopList.size.shouldBe(0)

        collectJob.cancel()
    }

    @Test
    fun `test is updated`() = runTest {
        nearbyViewModel.setIsUpdated(true)

        assertEquals(
            true,
            nearbyViewModel.isUpdated.getOrAwaitValue()
        )
    }

    @Test
    fun `test get directions to stop`() = runTest {
        nearbyViewModel.getDirectionsToStop("start","stop")
        val directions = nearbyViewModel.directionPolylineCoords.getOrAwaitValue()

        assertEquals(
            1,
            directions.size,
        )
        assertEquals(
            1.1,
            directions[0].latitude,
            0.0
        )
    }
}