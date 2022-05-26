package com.taitsmith.busboy.utils

interface OnItemLongClickListener {
    fun onNearbyLongClick(position: Int)
    fun onIdLongClick(position: Int)
    fun onFavoriteLongClick(position: Int)
}