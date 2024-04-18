package com.taitsmith.busboy.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindRemoteDataSource(
        remoteDataSource: RemoteDataSourceImpl
    ) : RemoteDataSource

    @Binds
    abstract fun bindApiRepository(
        apiRepository: ApiRepositoryImpl
    ) : ApiRepository

    @Binds
    abstract fun bindLocationRepository(
        locationRepository: LocationRepositoryImpl
    ) : LocationRepository
}