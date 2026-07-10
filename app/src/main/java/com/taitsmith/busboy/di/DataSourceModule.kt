package com.taitsmith.busboy.di

import com.taitsmith.busboy.data.Agency
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(ViewModelComponent::class)
abstract class DataSourceModule {
    // Per-agency data sources are multibound into a Map<Agency, RemoteDataSource>; the
    // DelegatingRemoteDataSource below reads that map and is the unqualified RemoteDataSource
    // that ApiRepositoryImpl injects.
    @Binds
    @IntoMap
    @AgencyKey(Agency.AC_TRANSIT)
    abstract fun bindAcTransitDataSource(
        dataSource: AcTransitRemoteDataSource
    ) : RemoteDataSource

    @Binds
    abstract fun bindRemoteDataSource(
        remoteDataSource: DelegatingRemoteDataSource
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