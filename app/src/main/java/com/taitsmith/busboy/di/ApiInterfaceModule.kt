package com.taitsmith.busboy.di

import com.taitsmith.busboy.api.ApiInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import javax.inject.Qualifier

@Qualifier
annotation class AcTransitApiInterface

@Qualifier
annotation class MapsApiInterface

@Module
@InstallIn(ViewModelComponent::class)
object ApiInterfaceModule {

    @AcTransitApiInterface
    @Provides
    fun provideAcTransitInterface(@AcTransitRetrofit acTransitRetrofit: Retrofit
    ): ApiInterface {
        return acTransitRetrofit.create(ApiInterface::class.java)
    }

    @MapsApiInterface
    @Provides
    fun provideMapsInterface(@MapsRetrofit mapsRetrofit: Retrofit): ApiInterface {
        return mapsRetrofit.create(ApiInterface::class.java)
    }
}