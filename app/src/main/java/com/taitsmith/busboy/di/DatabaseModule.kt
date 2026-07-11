package com.taitsmith.busboy.di

import android.content.Context
import androidx.room.Room
import com.taitsmith.busboy.data.BusboyDatabase
import com.taitsmith.busboy.data.CtaCatalogDatabase
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
                        ).addMigrations(BusboyDatabase.MIGRATION_3_4)
                        .build()

    @Singleton
    @Provides
    fun provideStopDao(db: BusboyDatabase) = db.stopDao()

    @Singleton
    @Provides
    fun provideRouteDao(db: BusboyDatabase) = db.routeDao()

    @Singleton
    @Provides
    fun provideCtaCatalogDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
                            context,
                            CtaCatalogDatabase::class.java,
                            "cta_catalog_database"
                        ).build()

    @Singleton
    @Provides
    fun provideCtaStopDao(db: CtaCatalogDatabase) = db.ctaStopDao()
}