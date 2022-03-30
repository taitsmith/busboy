package com.taitsmith.busboy.di

import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier
annotation class MapsRetrofit

@Module
@InstallIn(ViewModelComponent::class)
object MapsRetrofitModule {

    @MapsRetrofit
    @Provides
    fun provideMapsRetrofit(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}