package com.taitsmith.busboy

import dagger.hilt.android.HiltAndroidApp
import android.app.Application
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class BusboyApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocationProviderClient(application: Application) =
        LocationServices.getFusedLocationProviderClient(application)
}