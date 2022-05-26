package com.taitsmith.busboy.api

import com.taitsmith.busboy.di.ApiRepository
import com.taitsmith.busboy.api.StopPredictionResponse.BustimeResponse.Prediction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RemoteDataSource @Inject constructor(apiRepository: ApiRepository) {

}