package com.taitsmith.busboy.di

import com.taitsmith.busboy.api.StopDestinationResponse.RouteDestination
import com.taitsmith.busboy.data.RouteDestinationDao
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.data.StopDao
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val stopDao: StopDao,
    private val routeDao: RouteDestinationDao,
    private val settingsRepository: SettingsRepository
) {

    //favorites are scoped to the active agency: reads filter by it and writes stamp it, so the same
    //numeric stop id never collides across AC Transit and CTA. Read at call time (like the delegate),
    //which is enough because switching agencies resets navigation and re-queries.
    fun getAllStops() = stopDao.getAll(settingsRepository.selectedAgencyState.value.name)

    fun addStops(vararg stops: Stop) {
        val agency = settingsRepository.selectedAgencyState.value.name
        stopDao.insertAll(*stops.map { it.copy(agency = agency) }.toTypedArray())
    }

    fun deleteStop(stop: Stop) = stopDao.delete(stop)
    //scope to the active agency so clearing favorites can't also wipe the other agency's saved stops.
    fun deleteAll() = stopDao.deleteAll(settingsRepository.selectedAgencyState.value.name)

    fun getAllLines() = routeDao.getAll()
    fun addLines(vararg lines: RouteDestination) = routeDao.insertAll(*lines)
    fun deleteLine(line: RouteDestination) = routeDao.delete(line)
}
