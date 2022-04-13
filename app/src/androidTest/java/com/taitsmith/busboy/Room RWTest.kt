package com.taitsmith.busboy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.taitsmith.busboy.obj.*
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
        assertThat(stopDao.getAllStops()[0], equalTo(stop))
    }

    @Test
    @Throws(Exception::class)
    fun writeLineAndRead() {
        val line: StopDestinationResponse.RouteDestination = TestUtils.createLine()
        routeDao.insertAll(line)
        assertThat(routeDao.getAll()[0], equalTo(line))
    }

    @Test
    @Throws(Exception::class)
    fun deleteStops() {
        val stop = TestUtils.createStop()
        stopDao.delete(stop)

        val stopList = stopDao.getAllStops()

        Assert.assertTrue(stopList.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun deleteLines() {
        val line = TestUtils.createLine()
        routeDao.delete(line)
        val lineList = routeDao.getAll()
        Assert.assertTrue(lineList.isEmpty())
    }
}