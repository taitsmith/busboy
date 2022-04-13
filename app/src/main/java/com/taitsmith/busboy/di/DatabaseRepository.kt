package com.taitsmith.busboy.di

import com.taitsmith.busboy.obj.RouteDestinationDao
import com.taitsmith.busboy.obj.Stop
import com.taitsmith.busboy.obj.StopDao
import com.taitsmith.busboy.obj.StopDestinationResponse.RouteDestination
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val stopDao: StopDao, private val routeDao: RouteDestinationDao) {

    fun getAllStops() = stopDao.getAll()
    fun addStops(vararg stops: Stop) = stopDao.insertAll(*stops)
    fun deleteStop(stop: Stop) = stopDao.delete(stop)

    fun getAllLines() = routeDao.getAll()
    fun addLines(vararg lines: RouteDestination) = routeDao.insertAll(*lines)
    fun deleteLine(line: RouteDestination) = routeDao.delete(line)
}