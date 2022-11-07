package com.taitsmith.busboy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.taitsmith.busboy.api.StopDestinationResponse
import com.taitsmith.busboy.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.Exception

@RunWith(AndroidJUnit4::class)
class RoomRWTest {
    private lateinit var stopDao: StopDao
    private lateinit var routeDao: RouteDestinationDao
    private lateinit var db: BusboyDatabase
    private val stop = TestUtils.createStop()
    private val lines: List<StopDestinationResponse.RouteDestination> = TestUtils.createLines()
    private var stopList: List<Stop> = ArrayList()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BusboyDatabase::class.java)
            .build()
        stopDao = db.stopDao()
        routeDao = db.routeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Throws(Exception::class)
    fun writeStopAndRead() = runTest {
        val stop: Stop = TestUtils.createStop()
        stopDao.insertAll(stop)
        val returnStop = stopDao.getAll().first()[0]
        assertThat(returnStop, equalTo(stop))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Throws(Exception::class)
    fun writeStopsDeleteAndRead() = runTest {
        val stopList = TestUtils.createStops()
        stopList.forEach {
            stopDao.insertAll(it)
        }

        var returnedList: List<Stop> = stopDao.getAll().first()

        assertThat(stopList[0], equalTo(returnedList[0]))

        stopDao.delete(stopList[0])

        returnedList = stopDao.getAll().first()

        assertThat(returnedList.size, equalTo(2))
        assertThat(returnedList[0], equalTo(stopList[1]))
    }

    @Test
    @Throws(Exception::class)
    fun deleteStops() {
        stopDao.delete(stop)
        val stopList = stopDao.getAll()
    }

    @Test
    @Throws(Exception::class)
    fun writeLineAndRead() {
        routeDao.insertAll(lines[0], lines[1],  lines[2])
        assertThat(routeDao.getAll()[1], equalTo(lines[1]))
    }

    @Test
    @Throws(Exception::class)
    fun deleteLines() {
        routeDao.insertAll(lines[0], lines[1], lines[2])
        routeDao.delete(lines[1])
        val routeList = routeDao.getAll()
        assertThat(routeList.size, equalTo(2))
    }
}