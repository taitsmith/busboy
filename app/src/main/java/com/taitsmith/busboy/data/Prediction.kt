package com.taitsmith.busboy.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * the ac transit api has changed again, and now returns more information than what is documented here.
 * the link to this specific endpoint on their docs page is also broken, so we're left to figure out
 * what things mean on our own. for the time being however, everything we need is here
 */
data class Prediction(
    @SerializedName("stpnm")
    @Expose //stop name (Broadway & 25th St)
    val stpnm: String? = null,

    @SerializedName("vid")
    @Expose //vehicle id of upcoming bus
    val vid: String? = null,

    @SerializedName("rt")
    @Expose //route (51A)
    val rt: String? = null,

    @SerializedName("rtdir")
    @Expose //route direction (to Fruitvale BART)
    val rtdir: String? = null,

    @SerializedName("des")
    @Expose //destination (Fruitvale BART)
    val des: String? = null,

    @SerializedName("prdtm")
    @Expose //predicted time of arrival (YYYMMDD HH:MM)
    val prdtm: String? = null,

    @SerializedName("dyn")
    @Expose //not entirely sure. non-zero dyn means the bus is not stopping for some reason
    val dyn: Int? = null,

    @SerializedName("prdctdn")
    @Expose //not entirely sure
    var prdctdn: String? = null
): Serializable