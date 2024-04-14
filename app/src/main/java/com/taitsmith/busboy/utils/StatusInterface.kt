package com.taitsmith.busboy.utils

interface StatusInterface {
    fun updateStatus(msg: String)
    fun isLoading(loading: Boolean)
}