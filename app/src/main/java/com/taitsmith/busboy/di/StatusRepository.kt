package com.taitsmith.busboy.di

import com.taitsmith.busboy.utils.StatusInterface
import com.taitsmith.busboy.viewmodels.MainActivityViewModel.LoadingState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepository @Inject constructor(): StatusInterface {

    //one-shot loading/status events. a SharedFlow (not StateFlow) so identical consecutive codes
    //(e.g. two 404s in a row) and a rapid Loading -> StatusUpdate -> Success sequence are each
    //delivered, instead of being conflated/de-duplicated away and silently dropping a snackbar.
    val state: MutableSharedFlow<LoadingState> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun updateStatus(msg: String) {
        state.tryEmit(LoadingState.StatusUpdate(msg))
    }

    override fun isLoading(loading: Boolean) {
        state.tryEmit(if (loading) LoadingState.Loading else LoadingState.Success)
    }
}
