package com.taitsmith.busboy.data

import com.google.gson.annotations.SerializedName

/**
 * class containing response data from ac transit's service alert api
 * https://api.actransit.org/transit/Help/Api/GET-actrealtime-servicebulletin_rt_rtdir_stpid_callback
 */
class ServiceAlert {

    //name ("detours throughout oakland march 17")
    @SerializedName("nm")
    var nm: String? = null

    //subject, essentially the same as name
    @SerializedName("sbj")
    var sbj: String? = null

    //date and time in effect, due to, detailed description in multiple languages
    @SerializedName("dtl")
    var dtl: String? = null

    //brief description?
    @SerializedName("brf")
    var brf: String? = null

    //cause (usually vague or 'other')
    @SerializedName("cse")
    var cse: String? = null

    //effect (detour, stop closure, cancellation etc)
    @SerializedName("efct")
    var efct: String? = null

    //priority (high / med / low)
    @SerializedName("prty")
    var prty: String? = null

    @SerializedName("rtpidatafeed")
    var rtpidatafeed: String? = null

    @SerializedName("srvc")
    var srvc: ArrayList<Srvc> = arrayListOf()

    //timestamp of last modification YYYMMDD HH:MM:SS
    @SerializedName("mod")
    var mod: String? = null

    //lines impacted by alert
    data class Srvc(

        @SerializedName("rt")
        var rt: String? = null,

        @SerializedName("rtdir")
        var rtdir: String? = null,

        @SerializedName("stpid")
        var stpid: String? = null,

        @SerializedName("stpnm")
        var stpnm: String? = null
    )
}


