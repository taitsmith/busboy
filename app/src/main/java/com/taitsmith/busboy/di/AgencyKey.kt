package com.taitsmith.busboy.di

import com.taitsmith.busboy.data.Agency
import dagger.MapKey

/** Hilt map key so RemoteDataSource implementations can be multibound per Agency. */
@MapKey
annotation class AgencyKey(val value: Agency)
