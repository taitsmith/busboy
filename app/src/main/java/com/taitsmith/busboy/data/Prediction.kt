package com.taitsmith.busboy.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Prediction(
    @SerializedName("stpnm")
    @Expose
    val stpnm: String? = null,

    @SerializedName("vid")
    @Expose
    val vid: String? = null,

    @SerializedName("rt")
    @Expose
    val rt: String? = null,

    @SerializedName("rtdir")
    @Expose
    val rtdir: String? = null,

    @SerializedName("des")
    @Expose
    val des: String? = null,

    @SerializedName("prdtm")
    @Expose
    val prdtm: String? = null,

    @SerializedName("dyn")
    @Expose
    val dyn: Int? = null,

    @SerializedName("prdctdn")
    @Expose
    var prdctdn: String? = null
): Serializable