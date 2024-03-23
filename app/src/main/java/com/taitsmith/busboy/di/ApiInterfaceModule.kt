package com.taitsmith.busboy.di

import com.slack.eithernet.ApiResultCallAdapterFactory
import com.slack.eithernet.ApiResultConverterFactory
import com.taitsmith.busboy.api.ApiInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
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
    fun provideAcTransitInterface(): ApiInterface {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.actransit.org/transit/")
            .addConverterFactory(ApiResultConverterFactory)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ApiResultCallAdapterFactory)
            .client(client)
            .build()
        return retrofit.create(ApiInterface::class.java)
    }

    @MapsApiInterface
    @Provides
    fun provideMapsInterface(): ApiInterface {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiInterface::class.java)
    }
}