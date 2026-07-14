package com.taitsmith.busboy.api

import com.google.gson.annotations.SerializedName
import com.taitsmith.busboy.data.ServiceAlert

/**
 * A single active detour from CTA's getdetours response. CTA has no service-bulletin equivalent, so
 * detours stand in for AC Transit's service alerts. [toServiceAlert] maps a detour into the existing
 * [ServiceAlert] shape so the shared alert UI can render it unchanged.
 */
class CtaDetour {
    @SerializedName("id")      var id: String? = null
    @SerializedName("ver")     var ver: Int? = null
    @SerializedName("st")      var st: Int? = null
    @SerializedName("desc")    var desc: String? = null
    @SerializedName("rtdirs")  var rtdirs: List<RtDir>? = null
    @SerializedName("startdt") var startdt: String? = null
    @SerializedName("enddt")   var enddt: String? = null

    class RtDir {
        @SerializedName("rt")  var rt: String? = null
        @SerializedName("dir") var dir: String? = null
    }
}

fun CtaDetour.toServiceAlert(): ServiceAlert = ServiceAlert().also { alert ->
    alert.nm = desc
    alert.dtl = buildString {
        append(desc.orEmpty())
        if (!startdt.isNullOrBlank() && !enddt.isNullOrBlank()) {
            append("\n\nIn effect: ").append(startdt).append(" – ").append(enddt)
        }
    }
    alert.cse = "Detour"
    alert.efct = if (st == 0) "Canceled" else "Active"
    alert.srvc = ArrayList(
        rtdirs.orEmpty().map { ServiceAlert.ImpactedServices(rt = it.rt, rtdir = it.dir) }
    )
}
