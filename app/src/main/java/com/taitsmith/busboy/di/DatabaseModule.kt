package com.taitsmith.busboy.di

import android.content.Context
import androidx.room.Room
import com.taitsmith.busboy.obj.BusboyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
                            context,
                            BusboyDatabase::class.java,
                            "busboy_database"
                        ).build()

    @Singleton
    @Provides
    fun provideStopDao(db: BusboyDatabase) = db.stopDao()

    @Singleton
    @Provides
    fun provideRouteDao(db: BusboyDatabase) = db.routeDao()
}