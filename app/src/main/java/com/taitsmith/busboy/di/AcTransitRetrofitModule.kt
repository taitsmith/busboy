package com.taitsmith.busboy.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier

@Qualifier
annotation class AcTransitRetrofit

@InstallIn(ViewModelComponent::class)
@Module
object AcTransitRetrofitModule {

    @AcTransitRetrofit
    @Provides
    fun provideAcTransitRetrofit(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.actransit.org/transit/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

}