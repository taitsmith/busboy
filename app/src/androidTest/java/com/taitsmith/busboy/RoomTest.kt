package com.taitsmith.busboy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.taitsmith.busboy.api.StopDestinationResponse
import com.taitsmith.busboy.data.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class RoomRWTest {
    private lateinit var stopDao: StopDao
    private lateinit var routeDao: RouteDestinationDao
    private lateinit var db: BusboyDatabase
    private val stop = TestUtils.createStop()
    private val lines: List<StopDestinationResponse.RouteDestination> = TestUtils.createLines()

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

    @Test
    @Throws(Exception::class)
    fun writeStopAndRead() {
        val stop: Stop = TestUtils.createStop()
        stopDao.insertAll(stop)
        assertThat(stopDao.getAll()[0], equalTo(stop))
    }

    @Test
    @Throws(Exception::class)
    fun deleteStops() {
        stopDao.delete(stop)
        val stopList = stopDao.getAll()
        Assert.assertTrue(stopList.isEmpty())
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
        routeDao.delete(lines[1])
        val routeList = routeDao.getAll()
        assertThat(routeList.size, equalTo(2))
    }
}