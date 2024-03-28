package com.taitsmith.busboy.di

import com.taitsmith.busboy.utils.StatusInterface
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.LoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepo @Inject constructor(): StatusInterface {

    var state: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState.Success)

    override fun updateStatus(msg: String) {
        state.update {
            LoadingState.StatusUpdate(msg)
        }
    }

    override fun isLoading(loading: Boolean) {
        if (loading) state.update { LoadingState.Loading }
        else state.update { LoadingState.Success }
    }
}