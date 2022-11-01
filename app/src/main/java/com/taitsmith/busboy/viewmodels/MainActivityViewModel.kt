package com.taitsmith.busboy.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import javax.inject.Inject
import androidx.lifecycle.MutableLiveData

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    application: Application) : AndroidViewModel(application) {

    companion object {
        lateinit var mutableStatusMessage: MutableLiveData<String>
        lateinit var mutableErrorMessage: MutableLiveData<String>
    }

    init {
        mutableStatusMessage = MutableLiveData()
        mutableErrorMessage = MutableLiveData()
    }
}