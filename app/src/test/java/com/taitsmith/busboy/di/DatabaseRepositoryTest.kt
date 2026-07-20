package com.taitsmith.busboy.di

import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.data.RouteDestinationDao
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.data.StopDao
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Pins the headline 2.0 favorites guarantee at the repository layer: writes stamp the active agency
 * and reads filter by it, so the same numeric stop id can't collide across AC Transit and CTA. (The
 * composite-primary-key enforcement itself is exercised end-to-end in RoomTest / MigrationTest.)
 */
class DatabaseRepositoryTest {

    private val stopDao: StopDao = mock()
    private val routeDao: RouteDestinationDao = mock()

    private fun repo(agency: Agency) =
        DatabaseRepository(stopDao, routeDao, FakeSettingsRepository(agency))

    @Test
    fun `addStops stamps the active agency onto each saved stop`() {
        repo(Agency.CTA).addStops(Stop(stopId = "1926", name = "Belmont"))

        verify(stopDao).insertAll(Stop(stopId = "1926", agency = "CTA", name = "Belmont"))
    }

    @Test
    fun `getAllStops reads only the active agency`() {
        whenever(stopDao.getAll(any())).thenReturn(flowOf(emptyList()))

        repo(Agency.CTA).getAllStops()

        verify(stopDao).getAll(Agency.CTA.name)
    }

    @Test
    fun `the same stop id is stamped distinctly under each agency`() {
        repo(Agency.AC_TRANSIT).addStops(Stop(stopId = "1926"))
        repo(Agency.CTA).addStops(Stop(stopId = "1926"))

        verify(stopDao).insertAll(Stop(stopId = "1926", agency = "AC_TRANSIT"))
        verify(stopDao).insertAll(Stop(stopId = "1926", agency = "CTA"))
    }
}
